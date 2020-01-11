package works.hop.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.queue.entity.QueHandler;
import works.hop.queue.entity.QueRequest;
import works.hop.todo.domain.TodoCriteria;
import works.hop.trace.AppTracer;
import works.hop.web.HttpConnect;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Consumer;

public class TodoService implements QueHandler<TodoCriteria> {

    public static final Logger logger = LoggerFactory.getLogger(TodoService.class);
    public static final ObjectMapper mapper = new ObjectMapper();
    private final Properties props;
    private Tracer tracer = AppTracer.tracer();

    public TodoService(Properties props) {
        this.props = props;
    }

    public String repoUrl(String context, String query) {
        String baseUrl = String.format("http://%s:%s/%s", props.getProperty("repo-host"), props.getProperty("repo-port"), context);
        logger.info("created client url '{}'", baseUrl);
        return (query != null && query.trim().length() > 0) ?
                baseUrl + "?" + query :
                baseUrl;
    }

    public void getTodoList(Integer offset, Integer limit, Consumer<String> callback) {
        Span span = tracer.buildSpan("get todo list request").start();
        span.log("TodoService getTodoList executing");
        try {
            HttpConnect.request(repoUrl("todo", "start=" + offset + "&size=" + limit),
                    HttpMethod.GET, Collections.emptyMap(),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("TodoService response", payload);
                        span.finish();
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Problem accessing repo", ex);
        }
    }

    public void createTodoItem(String name, Consumer<String> callback) {
        Span span = tracer.buildSpan("create todo item").start();
        span.log("TodoService createTodoItem executing");
        try {
            HttpConnect.request(repoUrl("todo/" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString()), null),
                    HttpMethod.POST, Collections.emptyMap(),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("TodoService response", payload);
                        span.finish();
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Problem accessing repo", ex);
        }
    }

    public void updateTodoItem(String name, Boolean completed, Consumer<String> callback) {
        Span span = tracer.buildSpan("update todo item").start();
        span.log("TodoService updateTodoItem executing");
        try {
            HttpConnect.request(repoUrl("todo", null),
                    HttpMethod.PUT, Collections.emptyMap(),
                    String.format("{\"name\":\"%s\",\"completed\":%b}", name, completed),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("TodoService response", payload);
                        span.finish();
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Problem accessing repo", ex);
        }
    }

    public void deleteTodoItem(String name, Consumer<String> callback) {
        Span span = tracer.buildSpan("delete todo item").start();
        span.log("TodoService deleteTodoItem executing");
        try {
            HttpConnect.request(repoUrl("todo/" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString()), null),
                    HttpMethod.DELETE, Collections.emptyMap(),
                    (response) -> {
                        String payload = new String(response.body);
                        callback.accept(payload);
                        span.setTag("TodoService response", payload);
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(TodoCriteria todo, Consumer<String> handler) {
        switch (todo.action) {
            case CREATE_TODO: {
                logger.info("create new todo item");
                createTodoItem(todo.name, handler);
                break;
            }
            case UPDATE_TODO: {
                logger.info("update todo item");
                updateTodoItem(todo.name, todo.completed, handler);
                break;
            }
            case TODO_LIST: {
                logger.info("fetch list of todo items");
                getTodoList(todo.offset, todo.limit, handler);
                break;
            }
            case DELETE_TODO: {
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

    @Override
    public boolean canHandle(QueRequest request) {
        return true;
    }
}
