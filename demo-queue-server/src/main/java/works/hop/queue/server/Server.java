package works.hop.queue.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.queue.Connection;
import works.hop.queue.QueConsumer;
import works.hop.queue.QueEvent;
import works.hop.queue.QueObserver;
import works.hop.queue.entity.QueHandler;
import works.hop.queue.entity.QueRequestSerDe;
import works.hop.queue.entity.avro.AvroQueRequest;
import works.hop.queue.entity.avro.AvroRequestType;
import works.hop.queue.options.QueOptions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class Server {

    public static final Logger logger = LoggerFactory.getLogger(Server.class);
    public static final String POISON_PILL = "POISON_PILL";
    public static final int RECEIVING_BUFFER_SIZE = 4096;

    private final BlockingQueue<QueEvent> queue;
    private final QueConsumer consumer;
    private final QueObserver observer;
    private final ServerStats stats;
    private final Map<SelectionKey, Connection> connections = new HashMap<>();
    private Selector selector;
    private final Function<SelectionKey, Consumer<String>> onDataEvent = (selector) -> data -> reply(selector, data);

    public Server(BlockingQueue<QueEvent> queue) throws IOException {
        this.selector = Selector.open();
        this.stats = new ServerStats();
        this.queue = queue;
        this.consumer = new QueConsumer(queue);
        this.observer = new QueObserver();
        this.consumer.addObserver(observer);
        System.out.println("consumer thread has started");
    }

    public static void main(String[] args) {
        Properties props = QueOptions.applyDefaults(args);
        Server server = Server.instance();
        server.start(props, System.out::println);
    }

    public static Server instance() {
        try {
            return new Server(new LinkedBlockingQueue<>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void register(QueHandler handler) {
        this.observer.register(handler);
    }

    public void unregister(QueHandler handler) {
        this.observer.unregister(handler);
    }

    public void produce(AvroQueRequest input, Consumer<String> handler) {
        logger.info("push new payload to queue: {}", input);
        queue.add(new QueEvent(input, handler));
    }

    public void start(Properties props, Consumer<String> onStart) {
        //new Thread(stats).start();
        new Thread(consumer).start();
        new Thread(() -> {
            try {
                this.initialize(props, onStart);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).start();
    }

    protected void initialize(Properties props, Consumer<String> onStart) throws Exception {
        //get required properties
        String host = props.getProperty("host");
        Integer port = Integer.parseInt(props.getProperty("port"));
        //create server
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(false);
        //register server channel to handle OP_ACCEPT keys
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        onStart.accept(String.format("server listening on http://%s/%s (Press CTRL+C to exit)", props.getProperty("host"), props.getProperty("port")));
        try {
            while (selector.isOpen()) {
                selector.select(); //await next selection-key
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {

                    SelectionKey key = iter.next();

                    if (key.isValid() && key.isAcceptable()) {
                        onAcceptable(key);
                    }

                    if (key.isValid() && key.isReadable()) {
                        onReadable(key);
                    }

                    if (key.isValid() && key.isWritable()) {
                        sendResponse(key);
                    }

                    iter.remove();
                }
            }
        } finally {
            serverChannel.close();
            shutdown();
        }
    }

    public void shutdown() {
        try {
            stats.shutdown();
            for (SelectionKey key : connections.keySet()) {
                terminateConnection(key, connections.get(key));
            }
            selector.close();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private void onAcceptable(SelectionKey key) throws IOException {
        logger.info("handle connection request");
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);

        logger.info("creating new connection");
        Connection connection = new Connection(clientChannel, ByteBuffer.allocate(RECEIVING_BUFFER_SIZE), onDataEvent);
        SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ, connection);
        connections.put(clientKey, connection);

        logger.info("Accepted a connection");
    }

    private void onReadable(SelectionKey key) throws Exception {
        logger.info("handle read from client");
        Connection connection = (Connection) key.attachment();
        long readSize = connection.read();

        System.out.println("received bytes : " + readSize);
        if (readSize <= 0) { //should ideally only check for -1
            terminateConnection(key, connection);
        } else if (connection.hasFullMessage()) {
            AvroQueRequest input = QueRequestSerDe.instance().deserialize(connection.accumulator.toByteArray());
            connection.accumulator.reset();
            if (input.getType() == AvroRequestType.CLOSE) {
                terminateConnection(key, connection);
                System.out.println("Not accepting client messages anymore");
            }
            connection.resetHasFullMessage();
            produce(input, onDataEvent.apply(key));
        }
    }

    private void sendResponse(SelectionKey key) throws Exception {
        Connection connection = connections.get(key);
        SocketChannel clientChannel = connection.channel;
        if (clientChannel.isOpen()) {
            logger.info("send response to the client");
            connection.write(selector, key);
        }
    }

    private void reply(SelectionKey key, String data) {
        try {
            logger.info("response from observer: {}", data);
            Connection connection = (Connection) key.attachment();
            SocketChannel clientChannel = (SocketChannel) key.channel();
            if (clientChannel.isOpen()) {
                connection.outputBuffer = ByteBuffer.wrap(data.getBytes());

                logger.info("send response to the client");
                clientChannel.register(selector, SelectionKey.OP_WRITE, connection);
                connection.write(selector, key);
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void terminateConnection(SelectionKey key, Connection connection) {
        key.cancel();
        try {
            connection.channel.close();
            connections.remove(key);
            stats.decrementConnectionCounter();
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
