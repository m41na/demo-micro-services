package works.hop.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.servlet.HandlerException;
import io.opentracing.Span;
import io.opentracing.Tracer;
import works.hop.queue.client.QueClient;
import works.hop.rest.query.TodoCriteria;
import works.hop.trace.AppTracer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class RestApp {

    private final Tracer tracer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AppProvider provider = new AppProvider() {
        @Override
        public AppServer provide(Map<String, String> props) {

            AppServer app = AppServer.instance(props);
            app.post("/rest/{name}", (req, res, done) -> {
                String name = req.param("name");
                Span span = tracer.buildSpan("handling create todo request").start();
                span.log("RestApp create new todo  (" + name + ") item");

                try {
                    QueClient client = QueClient.start("localhost", 7079);
                    String payload = client.sendMessage(mapper.writeValueAsString(new TodoCriteria(name, false, 0, 10, "CREATE_TODO")));
                    res.header("Content-Type", "application/json");
                    res.send(payload);
                    span.setTag("RestApp response", payload);
                    span.finish();
                } catch (Exception e) {
                    try {
                        res.sendError(500, e.getMessage());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new HandlerException(500, "Problem accessing search", ex);
                    }
                }
                finally {
                    done.complete();
                }
            });
            app.put("/rest", (req, res, done) -> {
                Span span = tracer.buildSpan("handling update todo request").start();
                span.log("RestApp update todo item");

                TodoCriteria todo = req.body(TodoCriteria.class);

                try {
                    QueClient client = QueClient.start("localhost", 7079);
                    String payload = client.sendMessage(mapper.writeValueAsString(new TodoCriteria(todo.name, todo.completed, 0, 10, "UPDATE_TODO")));
                    res.header("Content-Type", "application/json");
                    res.send(payload);

                    span.setTag("RestApp response", payload);
                    span.finish();
                } catch (Exception e) {
                    try {
                        res.sendError(500, e.getMessage());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new HandlerException(500, "Problem accessing search", ex);
                    }
                }
                finally {
                    done.complete();
                }
            });
            app.get("/rest", (req, res, done) -> {
                Span span = tracer.buildSpan("add new item").start();
                span.log("RepoApp fetching todo items");

                Integer offset = Integer.parseInt(Optional.ofNullable(req.param("start")).orElse("0"));
                Integer limit = Integer.parseInt(Optional.ofNullable(req.param("size")).orElse("10"));
                try {
                    QueClient client = QueClient.start("localhost", 7079);
                    String payload = client.sendMessage(mapper.writeValueAsString(new TodoCriteria(null, null, limit, offset, "TODO_LIST")));
                    res.header("Content-Type", "application/json");
                    res.send(payload);
                    span.setTag("RestApp response", payload);
                    span.finish();
                } catch (Exception e) {
                    try {
                        res.sendError(500, e.getMessage());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new HandlerException(500, "Problem accessing search", ex);
                    }
                }
                finally {
                    done.complete();
                }
            });
            app.delete("/rest/{name}", (req, res, done) -> {
                String name = req.param("name");
                Span span = tracer.buildSpan("add new item").start();
                span.log("RepoApp deleting todo item");

                try {
                    QueClient client = QueClient.start("localhost", 7079);
                    String payload = client.sendMessage(mapper.writeValueAsString(new TodoCriteria(name, false, 0, 10, "DELETE_TODO")));
                    res.header("Content-Type", "application/json");
                    res.send(payload);
                    span.setTag("RestApp response", payload);
                    span.finish();
                } catch (Exception e) {
                    try {
                        res.sendError(500, e.getMessage());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new HandlerException(500, "Problem accessing search", ex);
                    }
                }
                finally {
                    done.complete();
                }
            });
            return app;
        }
    };

    public RestApp() {
        this(AppTracer.tracer());
    }

    public RestApp(Tracer tracer) {
        this.tracer = tracer;
    }

    public static void main(String[] args) {
        new RestApp().getProvider().start(args);
    }

    public AppProvider getProvider() {
        return provider;
    }
}
