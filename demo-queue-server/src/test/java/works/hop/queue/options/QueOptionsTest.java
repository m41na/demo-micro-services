package works.hop.queue.options;

import org.junit.Test;

import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class QueOptionsTest {

    private QueOptions que = new QueOptions();

    @Test
    public void handleCli() {
        String[] args = {"-p", "8090", "-h", "localhost"};
        Properties props = que.handleCli(args);
        String host = Optional.ofNullable(props.getProperty("host")).orElse("localhost");
        Integer port = Integer.parseInt(Optional.ofNullable(props.getProperty("port")).orElse("8090"));
        assertEquals("Expecting 'localhost'", "localhost", host);
        assertEquals("Expecting '8090'", 8090, port.intValue());
    }
}
