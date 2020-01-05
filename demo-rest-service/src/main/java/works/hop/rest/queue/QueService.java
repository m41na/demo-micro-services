package works.hop.rest.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.queue.entity.QueRequestListener;
import works.hop.rest.query.TodoCriteria;
import works.hop.trace.AppTracer;
import works.hop.web.HttpConnect;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Consumer;

public class QueService implements QueRequestListener<TodoCriteria> {

    public static final Logger logger = LoggerFactory.getLogger(QueService.class);
    public static final ObjectMapper mapper = new ObjectMapper();
    private Tracer tracer = AppTracer.tracer();

    public void getTodoList(Integer offset, Integer limit, Consumer<String> callback) {
        Span span = tracer.buildSpan("get todo list request").start();
        span.log("QueService getTodoList executing");
        try {
            HttpConnect.request("http://localhost:7082/todo?start=" + offset + "&size=" + limit,
                    HttpMethod.GET, Collections.emptyMap(),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("QueService response", payload);
                        span.finish();
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Problem accessing repo", ex);
        }
    }

    public void createTodoItem(String name, Consumer<String> callback) {
        Span span = tracer.buildSpan("create todo item").start();
        span.log("QueService createTodoItem executing");
        try {
            HttpConnect.request("http://localhost:7082/todo/" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString()),
                    HttpMethod.POST, Collections.emptyMap(),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("QueService response", payload);
                        span.finish();
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Problem accessing repo", ex);
        }
    }

    public void updateTodoItem(String name, Boolean completed, Consumer<String> callback) {
        Span span = tracer.buildSpan("update todo item").start();
        span.log("QueService updateTodoItem executing");
        try {
            HttpConnect.request("http://localhost:7082/todo",
                    HttpMethod.PUT, Collections.emptyMap(),
                    String.format("{\"name\":\"%s\",\"completed\":%b}", name, completed),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("QueService response", payload);
                        span.finish();
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Problem accessing repo", ex);
        }
    }

    public void deleteTodoItem(String name, Consumer<String> callback) {
        Span span = tracer.buildSpan("delete todo item").start();
        span.log("QueService deleteTodoItem executing");
        try {
            HttpConnect.request("http://localhost:7082/todo/" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString()),
                    HttpMethod.DELETE, Collections.emptyMap(),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("QueService response", payload);
                        span.finish();
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Problem accessing repo", ex);
        }
    }

    @Override
    public TodoCriteria convert(String input) {
        try {
            return mapper.readValue(input, TodoCriteria.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(TodoCriteria todo, Consumer<String> handler) {
        switch (todo.action) {
            case "CREATE_TODO": {
                logger.info("create new todo item");
                createTodoItem(todo.name, handler);
                break;
            }
            case "UPDATE_TODO": {
                logger.info("update todo item");
                updateTodoItem(todo.name, todo.completed, handler);
                break;
            }
            case "TODO_LIST": {
                logger.info("fetch list of todo items");
                getTodoList(todo.offset, todo.limit, handler);
                break;
            }
            case "DELETE_TODO": {
                logger.info("remove todo item");
                deleteTodoItem(todo.name, handler);
                break;
            }
            default: {
                System.err.println("Unknown requestType");
                break;
            }
        }
    }
}
