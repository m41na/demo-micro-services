package works.hop.message.client;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

@Ignore("only enable for manual/integration testing")
public class NatsClientTest {

    NatsClient client = new NatsClient("/nats-test.properties");

    @Test
    public void publish() {
        String message = "Hello from nats";
        client.publish(message.getBytes(StandardCharsets.UTF_8));
    }
}
