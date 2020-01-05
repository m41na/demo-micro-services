package works.hop.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.queue.entity.QueRequestListener;
import works.hop.queue.entity.QueRequestSerDe;
import works.hop.queue.entity.avro.AvroClientRequest;
import works.hop.queue.entity.avro.AvroRequestType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class QueServer {

    public static final Logger logger = LoggerFactory.getLogger(QueServer.class);
    public static final String POISON_PILL = "POISON_PILL";

    private final BlockingQueue<QueEvent> queue;
    private final QueConsumer consumer;
    private final QueObserver observer;
    private Selector selector;
    private final Function<SelectionKey, Consumer<String>> onDataEvent = (selector) -> data -> sendResponse(selector, data);
    private final AtomicBoolean STOP = new AtomicBoolean(false);

    public QueServer(BlockingQueue<QueEvent> queue) {
        this.queue = queue;
        this.consumer = new QueConsumer(queue);
        this.observer = new QueObserver() {

            @Override
            public void complete() {
                consumer.removeObserver(this);
            }
        };
        consumer.addObserver(observer);

        new Thread(consumer).start();
        System.out.println("consumer thread has started");
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("host", "localhost");
        props.setProperty("port", "8089");
        QueServer.instance().start(props);
    }

    public static QueServer instance() {
        return new QueServer(new LinkedBlockingQueue<>());
    }

    public void register(QueRequestListener listener) {
        this.observer.register(listener);
    }

    public void unregister(QueRequestListener listener) {
        this.observer.unregister(listener);
    }

    public void produce(AvroClientRequest input, Consumer<String> handler) {
        logger.info("push new payload to queue: {}", input);
        queue.add(new QueEvent(input, handler));
    }

    public void start(Properties props) throws Exception {
        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(props.getProperty("host"), Integer.parseInt(props.getProperty("port"))));
        serverChannel.configureBlocking(false);
        //register server channel to handle OP_ACCEPT keys
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        try {
            while (!STOP.get()) {
                selector.select(); //await next selection-key
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {

                    SelectionKey key = iter.next();

                    if (key.isAcceptable()) {
                        onAcceptable(serverChannel);
                    }

                    if (key.isReadable()) {
                        onReadable(key);
                    }

                    iter.remove();
                }
            }
        }
        finally{
            serverChannel.close();
            selector.close();
        }
    }

    public void stop(){
        this.STOP.set(false);
    }

    private void onAcceptable(ServerSocketChannel serverChannel) throws IOException {
        logger.info("handle connection request");
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void onReadable(SelectionKey key) throws Exception {
        logger.info("handle read from client");
        ByteBuffer buffer = ByteBuffer.allocate(2048); //TODO fixme this might not be enough for a large request

        SocketChannel clientChannel = (SocketChannel) key.channel();
        int count = clientChannel.read(buffer);
        if (count > 0) {
            AvroClientRequest input = QueRequestSerDe.instance().deserialize(buffer.array());
            if (input.getType() == AvroRequestType.CLOSE) {
                clientChannel.close();
                System.out.println("Not accepting client messages anymore");
            }

            buffer.flip();
            buffer.clear();

            produce(input, onDataEvent.apply(key));
        }
    }

    private void sendResponse(SelectionKey key, String data) {
        try {
            logger.info("response from observer: {}", data);
            SocketChannel clientChannel = (SocketChannel) key.channel();
            if (clientChannel.isOpen()) {
                ByteBuffer buffer = ByteBuffer.allocate(2048); //TODO fixme this might not be enough for a large response
                buffer.put(data.getBytes());
                buffer.flip();
                buffer.rewind();

                logger.info("send response to the client");
                clientChannel.register(selector, SelectionKey.OP_WRITE);
                clientChannel.write(buffer);
                if (buffer.hasRemaining()) {
                    buffer.compact();
                } else {
                    buffer.clear();
                    clientChannel.register(selector, SelectionKey.OP_READ, buffer);
                }
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
