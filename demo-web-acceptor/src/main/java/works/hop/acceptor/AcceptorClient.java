package works.hop.acceptor;

import io.nats.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AcceptorClient {

    private final Options options;
    private final Acceptor acceptor;

    public AcceptorClient(Map<String, String> properties, String topic, AcceptorSocket socket) {
        String host = properties.get("natsHost");
        String port = properties.get("natsPort");
        Integer reconnect = Integer.parseInt(properties.get("maxReconnect"));
        this.options = new Options.Builder().server("nats://" + host + ":" + port).maxReconnects(reconnect).build();
        this.acceptor = new Acceptor(topic, socket, options);
        //start dispatcher thread
        initialize();
    }

    private void initialize() {
        new Thread(this.acceptor).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            acceptor.close();
            System.out.println("acceptor thread has been terminated");
        }));
    }

    public void publish(String topic, String message) {
        try (Connection nc = Nats.connect(this.options)) {
            nc.publish(topic, message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void accept(Message message) {
        this.acceptor.accept(message);
    }

    public static class Acceptor implements Runnable {

        private final AcceptorSocket socket;
        private final Options options;
        private final String topic;
        private final CountDownLatch latch = new CountDownLatch(1);
        private Dispatcher dispatcher;
        private Subscription subscription;

        public Acceptor(String topic, AcceptorSocket socket, Options options) {
            this.topic = topic;
            this.socket = socket;
            this.options = options;
        }

        private void subscribe() {
            //create subscription
            try (Connection nc = Nats.connect(this.options)) {
                this.dispatcher = nc.createDispatcher((msg) -> {
                });
                this.subscription = dispatcher.subscribe(this.topic, this::accept);

                this.latch.await();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(System.err);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            subscribe();
        }

        public void close() {
            this.latch.countDown();
            if (dispatcher != null && subscription != null) {
                this.dispatcher.unsubscribe(this.subscription, 100);
            }
        }

        public void accept(Message message) {
            String data = new String(message.getData(), StandardCharsets.UTF_8);
            System.out.printf("incoming data is %s%n", data);
            this.socket.broadcast(data);
        }
    }

}
