package works.hop.message.client;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NatsClient {

    private final Properties properties;
    private final Options options;

    public NatsClient(String propsFile) {
        this.properties = new Properties();

        try {
            properties.load(NatsClient.class.getResourceAsStream(propsFile));
            String host = properties.getProperty("nats-host");
            Integer port = Integer.parseInt(properties.getProperty("nats-port"));
            Integer reconnect = Integer.parseInt(properties.getProperty("max.reconnect"));
            options = new Options.Builder().server("nats://" + host + ":" + port).server("nats://" + host + ":" + port).maxReconnects(reconnect).build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Connection connection() {
        try (Connection nc = Nats.connect(this.options)) {
            return nc;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void publish(String subject, byte[] message) {
        connection().publish(subject, message);
    }

    public void publish(String subject, String replyTo, byte[] message) {
        connection().publish(subject, replyTo, message);
    }

    public String request(String subject, byte[] message) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Message> incoming = connection().request(subject, message);
        Message msg = incoming.get(500, TimeUnit.MILLISECONDS);
        return new String(msg.getData(), StandardCharsets.UTF_8);
    }
}
