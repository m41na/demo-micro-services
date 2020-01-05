package works.hop.rest.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import works.hop.queue.QueServer;
import works.hop.queue.client.QueClient;
import works.hop.rest.query.TodoCriteria;

import java.io.IOException;
import java.util.Properties;

public class QueAppTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        //get mapper
        ObjectMapper mapper = new ObjectMapper();

        //create Server
        Properties props = new Properties();
        props.setProperty("host", "localhost");
        props.setProperty("port", "8089");

        //start server
        QueServer server = QueServer.instance();
        new Thread(() -> {
            try {
                server.start(props);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        //allow server to start
        Thread.sleep(2000l);

        //register Service
        QueApp app = new QueApp(server);
        app.initialize(new QueService());

        //create client
        QueClient client = QueClient.start("localhost", 8089);
        String res = client.sendMessage(mapper.writeValueAsString(new TodoCriteria("buy bread", false, 0, 10, "CREATE_TODO")));
        System.out.println(res);
    }
}
