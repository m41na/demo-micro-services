package works.hop.acceptor;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.app.IServer;
import org.apache.commons.cli.Options;

import java.util.Map;

public class AcceptorApp {

    private final AcceptorSocket adapter = new AcceptorSocket();

    private final AppProvider provider = new AppProvider() {

        @Override
        public IServer provide(Map<String, String> props) {
            new AcceptorClient("/nats.properties", "nats-events", adapter);
            IServer app = AppServer.instance(props);
            app.websocket("/accept", () -> adapter);
            return app;
        }
    };

    public static void main(String[] args) {
        //allow additional command line options
        Options options = new Options();
        options.addOption("natsPort", true, "The listening port for the todo's server")
                .addOption("natsHost", true, "The host name for the todo's server");
        new AcceptorApp().getProvider().start(options, args);
    }

    public AppProvider getProvider() {
        return provider;
    }
}
