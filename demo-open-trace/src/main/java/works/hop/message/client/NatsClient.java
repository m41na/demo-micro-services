package works.hop.message.client;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class NatsClient {

    public static final Logger log = LoggerFactory.getLogger(NatsClient.class);
    private final Properties properties;
    private final Options options;

    public NatsClient(String propsFile) {
        this.properties = new Properties();

        try {
            properties.load(NatsClient.class.getResourceAsStream(propsFile));
            String host = properties.getProperty("natsHost");
            Integer port = Integer.parseInt(properties.getProperty("natsPort"));
            Integer reconnect = Integer.parseInt(properties.getProperty("maxReconnect"));
            options = new Options.Builder().server("nats://" + host + ":" + port).server("nats://" + host + ":" + port).maxReconnects(reconnect).build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate NatsClient successfully", e);
        }
    }

    public static NatsClient newInstance() {
        return new NatsClient("/nats.properties");
    }

    public void publish(byte[] message) {
        try (Connection nc = Nats.connect(this.options)) {
            log.info("publishing message: {}", new String(message, StandardCharsets.UTF_8));
            nc.publish(properties.getProperty("natsTopic"), message);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Could either not create a nats connection or publish a message successfully", e);
        }
    }
}
