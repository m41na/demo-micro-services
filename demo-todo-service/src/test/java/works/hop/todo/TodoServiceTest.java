package works.hop.todo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import works.hop.queue.client.QueClient;
import works.hop.queue.server.Server;
import works.hop.todo.domain.TodoCriteria;

import java.io.IOException;
import java.util.Properties;

@Ignore("this should be configured to run only with integration testing")
public class TodoServiceTest {

    static Properties config = new Properties();
    static Server server = Server.instance();
    static TodoService todoService = new TodoService(config);

    static {
        try {
            config.load(TodoApp.class.getResourceAsStream("/app-config-test.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void setUpServer() throws IOException, InterruptedException {
        Properties props = new Properties();
        props.setProperty("host", "localhost");
        props.setProperty("port", "7079");
        server.start(props, System.out::println);
        server.register(todoService);

        //allow server to start
        Thread.sleep(2000l);
    }

    @AfterClass
    public static void shutdownServer() {
        server.shutdown();
    }

    @Test
    public void getTodoList() {
        todoService.getTodoList(0, 10, System.out::println);
    }

    @Test
    public void createTodoItem() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        QueClient client = QueClient.start("localhost", 8089);
        String res = client.sendMessage(mapper.writeValueAsString(new TodoCriteria("buy bread", false, 0, 10, "CREATE_TODO")));
        System.out.println(res);
    }

    @Test
    public void updateTodoItem() {
    }

    @Test
    public void deleteTodoItem() {
    }

    @Test
    public void convert() {
    }

    @Test
    public void handle() {
    }
}
