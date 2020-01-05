package works.hop.rest.queue;

import works.hop.queue.QueServer;
import works.hop.queue.entity.QueRequestListener;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class QueApp {

    public final QueServer queServer;

    public QueApp(QueServer queServer) {
        super();
        this.queServer = queServer;
    }

    public static void main(String[] args) {
        QueApp app = new QueApp(QueServer.instance());
        app.initialize(new QueService());

        Boolean done = CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.setProperty("host", "localhost");
            props.setProperty("port", "7079");
            try {
                app.queServer.start(props);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return 0;
        }).handle((status, th) -> {
            if (th == null) {
                System.out.println("Thread exited with success status: " + status);
                return true;
            } else {
                System.out.println("Thread exited with failure status: " + status);
                return false;
            }
        }).join();
        System.out.println("Thread exited successfully? " + done);
    }

    public void initialize(QueRequestListener listener) {
        this.queServer.register(listener);
    }
}
