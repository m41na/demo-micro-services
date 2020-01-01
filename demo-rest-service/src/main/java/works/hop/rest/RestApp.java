package works.hop.rest;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.servlet.HandlerException;
import org.eclipse.jetty.http.HttpMethod;
import works.hop.web.HttpConnect;

import java.io.IOException;
import java.util.Collections;

public class RestApp {

    public static AppProvider provider = props -> {
        AppServer app = AppServer.instance(props);
        app.get("/rest", (req, res, done) -> {
            try {
                HttpConnect.request("http://localhost:7081/search", HttpMethod.GET, Collections.emptyMap(),
                        (response) -> {
                            res.send("rest: " + new String(response.body));
                        });
            } catch (Exception e) {
                try {
                    res.sendError(500, e.getMessage());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new HandlerException(500, "Problem accessing search", ex);
                }
            }
            done.complete();
        });
        return app;
    };

    public static void main(String[] args) {
        provider.start(args);
    }
}
