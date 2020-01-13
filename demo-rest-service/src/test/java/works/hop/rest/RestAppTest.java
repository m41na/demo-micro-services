package works.hop.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import works.hop.queue.client.QueClient;
import works.hop.todo.domain.TodoCriteria;

import static works.hop.todo.domain.TodoAction.TODO_LIST;

@Ignore("create more useful, cross-container tests")
public class RestAppTest {

    @Test
    public void testGetTodoList() throws JsonProcessingException {
        QueClient client = QueClient.start("localhost", 7079);
        ObjectMapper mapper = new ObjectMapper();
        String payload = client.sendMessage(mapper.writeValueAsString(new TodoCriteria(null, null, 10, 0, TODO_LIST)));
        System.out.println(payload);
    }
}
