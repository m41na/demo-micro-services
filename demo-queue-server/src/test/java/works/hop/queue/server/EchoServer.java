package works.hop.queue.server;

import works.hop.queue.client.QueClient;
import works.hop.queue.entity.QueHandler;
import works.hop.queue.entity.QueRequest;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

public class EchoServer {

    private static String host = "localhost";
    private static Integer port = 7078;

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("host", host);
        props.setProperty("port", Integer.toString(port));

        Server server = Server.instance();
        server.start(props, System.out::println);
        server.register(new QueHandler<String>() {

            @Override
            public String convert(String input) {
                return input.toUpperCase();
            }

            @Override
            public void handle(String message, Consumer<String> handler) {
                //echo back the message
                System.out.printf("received message '%s' inside consumer%n", message);
                handler.accept(message);
            }

            @Override
            public boolean canHandle(QueRequest request) {
                return true;
            }
        });

        try {
            EchoClient.test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class EchoClient {

        public static void test() throws IOException {
            String msg = "[{\"name\":\"take out trash\",\"completed\":false},{\"name\":\"buy bread\",\"completed\":true},{\"name\":\"buy milk\",\"completed\":false}]";
            QueClient client = QueClient.start(host, port);
            String response = client.sendMessage(msg);
            System.out.printf("Received response: %s%n", response);
            client.stop();
        }
    }
}
