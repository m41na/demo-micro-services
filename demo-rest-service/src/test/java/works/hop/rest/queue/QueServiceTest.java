package works.hop.rest.queue;

import org.junit.Ignore;
import org.junit.Test;

public class QueServiceTest {

    private QueService service = new QueService();

    @Test
    @Ignore("this should be configured to run only with integration testing")
    public void getTodoList() {
        service.getTodoList(0, 10, System.out::println);
    }

    @Test
    public void createTodoItem() {
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
