package works.hop.acceptor;

import io.nats.client.Message;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Ignore("enable only for integration testing")
public class AcceptorClientTest {

    private AcceptorClient client;
    private String topic = "nats-events";
    private String config = "/app-config-test.properties";

    private @Mock
    AcceptorSocket acceptor;
    private @Mock
    Message message;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String message = invocation.getArguments()[0].toString();
                return System.out.printf("broadcasting message: %s%n", message);
            }
        }).when(acceptor).broadcast(anyString());

        when(message.getData()).thenReturn("Received a message".getBytes());

        Properties properties = new Properties();
        try {
            properties.load(AcceptorClient.class.getResourceAsStream(config));
            Map<String, String> map = new HashMap<>();
            for (String key : properties.stringPropertyNames()) {
                map.put(key, properties.getProperty(key));
            }
            client = new AcceptorClient(map, topic, acceptor);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    @Test
    public void testAcceptMessage() {
        client.accept(message);
    }

    @Test
    public void sendTestMessage() {
        client.publish(topic, "Sending out a message");
    }
}
