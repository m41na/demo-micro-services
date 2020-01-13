package works.hop.acceptor;

import io.nats.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AcceptorClient {

    private final Properties properties;
    private final Options options;
    private final Dispatcher dispatcher;
    private final String topic;
    private final AcceptorSocket acceptor;

    public AcceptorClient(String propsFile, String topic, AcceptorSocket acceptor) {
        this.properties = new Properties();
        this.topic = topic;
        this.acceptor = acceptor;
        try {
            properties.load(AcceptorClient.class.getResourceAsStream(propsFile));
            String host = properties.getProperty("nats-host");
            String port = properties.getProperty("nats-port");
            Integer reconnect = Integer.parseInt(properties.getProperty("max.reconnect"));
            this.options = new Options.Builder().server("nats://" + host + ":" + port).server("nats://" + host + ":" + port).maxReconnects(reconnect).build();
            this.dispatcher = connection().createDispatcher(this::accept);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        onShutDown();
    }

    private void onShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close();
        }));
    }

    private Connection connection() {
        try (Connection nc = Nats.connect(this.options)) {
            return nc;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        this.dispatcher.unsubscribe(this.topic);
    }

    public void accept(Message message) {
        this.acceptor.broadcast(new String(message.getData(), StandardCharsets.UTF_8));
    }
}
