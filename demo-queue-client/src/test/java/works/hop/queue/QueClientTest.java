package works.hop.queue;

import works.hop.queue.client.QueClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueClientTest {

    public static void main(String[] args) {
        String[] messages = {"I like non-blocking servers", "Hello non-blocking world!", "One more message..", "POISON_PILL"};
        System.out.println("Starting clients...");
        List<QueClient> clients = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            QueClient client = QueClient.start("localhost", 8089);
            clients.add(client);
        }

        for (String msg : messages) {
            for (int i = 0; i < clients.size(); i++) {
                System.out.printf("Prepared message (%d): %s%n", i, msg);
                String response = clients.get(i).sendMessage(msg + "(" + i + ")");
                System.out.printf("Received response (%d): %s%n", i, response);

            }
        }

        for (QueClient client : clients) {
            try {
                client.stop();
                System.out.println("CLOSING...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client connections closed");

    }
}
