package works.hop.acceptor;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.app.IServer;
import org.apache.commons.cli.Options;

import java.util.Map;

public class AcceptorApp {

    private final AcceptorSocket socket = new AcceptorSocket();

    private final AppProvider provider = new AppProvider() {

        @Override
        public IServer provide(Map<String, String> props) {
            new AcceptorClient(props, props.get("natsTopic"), socket);
            IServer app = AppServer.instance(props);
            app.websocket("/accept", () -> socket);
            return app;
        }
    };

    public static void main(String[] args) {
        //allow additional command line options
        Options options = new Options();
        options.addOption("natsPort", true, "The listening port for the nats server")
                .addOption("natsHost", true, "The host name for the nats server")
                .addOption("natsTopic", true, "The topic name from which to read messages")
                .addOption("maxReconnect", true, "Maximum attempts to reconnect if connection fails");
        new AcceptorApp().getProvider().start(options, args);
    }

    public AppProvider getProvider() {
        return provider;
    }
}
