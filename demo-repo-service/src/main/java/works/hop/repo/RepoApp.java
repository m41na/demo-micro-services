package works.hop.repo;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.app.IServer;
import works.hop.message.client.NatsClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RepoApp {

    private final TodoDao dao;
    private final NatsClient client;
    private final AppProvider provider = new AppProvider() {

        @Override
        public IServer provide(Map<String, String> props) {

            IServer app = AppServer.instance(props);
            app.post("/todo/{name}", (req, res, done) -> {
                String name = req.param("name");
                dao.addTodoItem(name);

                String payload = String.format("new item {%s} added", name);
                res.send(payload);

                client.publish("RepoApp add new todo item".getBytes(StandardCharsets.UTF_8));
                done.complete();
            });
            app.put("/todo", (req, res, done) -> {
                TodoDao.Todo todo = req.body(TodoDao.Todo.class);
                dao.updateTodoItem(todo);

                String payload = String.format("item {%s} updated", todo.name);
                res.send(payload);

                client.publish("RepoApp updating todo item".getBytes(StandardCharsets.UTF_8));
                done.complete();
            });
            app.get("/todo", (req, res, done) -> {
                Integer offset = Integer.parseInt(Optional.ofNullable(req.param("start")).orElse("0"));
                Integer limit = Integer.parseInt(Optional.ofNullable(req.param("size")).orElse("10"));
                List<TodoDao.Todo> todos = dao.fetchTodoItems(limit, offset);

                res.json(todos);

                client.publish("RepoApp fetching todo items".getBytes(StandardCharsets.UTF_8));
                done.complete();
            });
            app.delete("/todo/{name}", (req, res, done) -> {
                String name = req.param("name");
                dao.removeTodoItem(name);

                String payload = String.format("removed {%s} item", name);
                res.send(payload);

                client.publish("RepoApp deleting todo item".getBytes(StandardCharsets.UTF_8));
                done.complete();
            });
            return app;
        }

    };

    public RepoApp(TodoDao dao) {
        this(NatsClient.newInstance(), dao);
    }

    public RepoApp(NatsClient client, TodoDao dao) {
        this.dao = dao;
        this.client = client;
    }

    public static void main(String[] args) {
        new RepoApp(new TodoDao()).getProvider().start(args);
    }

    public AppProvider getProvider() {
        return provider;
    }
}
