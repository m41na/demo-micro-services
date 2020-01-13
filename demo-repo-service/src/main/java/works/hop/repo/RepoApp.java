package works.hop.repo;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.app.IServer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import works.hop.trace.AppTracer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RepoApp {

    private final TodoDao dao;
    private final Tracer tracer;
    private final AppProvider provider = new AppProvider() {

        @Override
        public IServer provide(Map<String, String> props) {

            IServer app = AppServer.instance(props);
            app.post("/todo/{name}", (req, res, done) -> {
                Span span = tracer.buildSpan("add new item").start();
                span.log("RepoApp add new todo item");

                String name = req.param("name");
                dao.addTodoItem(name);

                String payload = String.format("new item {%s} added", name);
                res.send(payload);

                span.setTag("RepoApp response", payload);
                span.finish();
                done.complete();
            });
            app.put("/todo", (req, res, done) -> {
                Span span = tracer.buildSpan("add new item").start();
                span.log("RepoApp updating todo item");

                TodoDao.Todo todo = req.body(TodoDao.Todo.class);
                dao.updateTodoItem(todo);

                String payload = String.format("item {%s} updated", todo.name);
                res.send(payload);

                span.setTag("RepoApp response", payload);
                span.finish();
                done.complete();
            });
            app.get("/todo", (req, res, done) -> {
                Span span = tracer.buildSpan("add new item").start();
                span.log("RepoApp fetching todo items");

                Integer offset = Integer.parseInt(Optional.ofNullable(req.param("start")).orElse("0"));
                Integer limit = Integer.parseInt(Optional.ofNullable(req.param("size")).orElse("10"));
                List<TodoDao.Todo> todos = dao.fetchTodoItems(limit, offset);

                res.json(todos);

                span.setTag("RepoApp response", "fetched " + todos.size() + " todo items");
                span.finish();
                done.complete();
            });
            app.delete("/todo/{name}", (req, res, done) -> {
                Span span = tracer.buildSpan("add new item").start();
                span.log("RepoApp deleting todo item");

                String name = req.param("name");
                dao.removeTodoItem(name);

                String payload = String.format("removed {%s} item", name);
                res.send(payload);

                span.setTag("RepoApp response", payload);
                span.finish();
                done.complete();
            });
            return app;
        }

    };

    public RepoApp(TodoDao dao) {
        this(dao, AppTracer.tracer());
    }

    public RepoApp(TodoDao dao, Tracer tracer) {
        this.dao = dao;
        this.tracer = tracer;
    }

    public static void main(String[] args) {
        new RepoApp(new TodoDao()).provider.start(args);
    }

    public AppProvider getProvider() {
        return provider;
    }
}
