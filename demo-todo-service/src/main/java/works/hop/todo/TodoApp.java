package works.hop.todo;

import works.hop.queue.server.Server;
import works.hop.todo.options.TodoOptions;

import java.io.IOException;
import java.util.Properties;

public class TodoApp {

    public static void main(String[] args) throws IOException {
        //fetch properties
        Properties props = TodoOptions.applyDefaults(args);
        //create service
        TodoService todoService = new TodoService(props);
        //start server
        Server server = Server.instance();
        server.start(props, System.out::println);
        //register service
        server.register(todoService);
    }
}
