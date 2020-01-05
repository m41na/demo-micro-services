package works.hop.repo;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TodoDaoTest {

    private TodoDao dao = null;

    @Before
    public void setUp() throws Exception {
        dao = new TodoDao("/test-todos-db.properties", "/test-create-tables.sql", "/test-initial-data.sql");
    }

    @Test
    public void fetchTodoItems() {
        List<TodoDao.Todo> todos = dao.fetchTodoItems(10, 0);
        assertEquals("Expecting 2 items", 2, todos.size());
    }

    @Test
    public void addTodoItem() {
        dao.addTodoItem("bake bread");
        List<TodoDao.Todo> todos = dao.fetchTodoItems(10, 0);
        assertEquals("Expecting 3 items", 3, todos.size());
    }

    @Test
    public void updateTodoItem() {
        dao.updateTodoItem(new TodoDao.Todo("read book", false));
        List<TodoDao.Todo> todos = dao.fetchTodoItems(10, 0);
        assertEquals("Expecting 2 items", 2, todos.size());
        TodoDao.Todo todo = todos.stream().filter(name -> name.completed).findFirst().get();
        assertEquals("Expecting true", true, todo.completed);
    }

    @Test
    public void removeTodoItem() {
        dao.removeTodoItem("read book");
        List<TodoDao.Todo> todos = dao.fetchTodoItems(10, 0);
        assertEquals("Expecting 1 item1", 1, todos.size());
    }
}
