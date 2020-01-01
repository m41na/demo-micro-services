package works.hop.search;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.servlet.HandlerException;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.eclipse.jetty.http.HttpMethod;
import works.hop.trace.AppTracer;
import works.hop.web.HttpConnect;

import java.io.IOException;
import java.util.Collections;

public class SearchApp {

    public static AppProvider provider = props -> {
        Tracer tracer = AppTracer.tracer();

        AppServer app = AppServer.instance(props);
        app.get("/search", (req, res, done) -> {
            Span span = tracer.buildSpan("handling search").start();
            span.log("SearchApp executing");

            try {
                HttpConnect.request("http://localhost:7082/repo", HttpMethod.GET, Collections.emptyMap(),
                        (response) -> {
                            String payload = "repo: " + new String(response.body);
                            res.send(payload);

                            span.setTag("SearchApp response", payload);
                            span.finish();
                        });
            } catch (Exception e) {
                try {
                    res.sendError(500, e.getMessage());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new HandlerException(500, "Problem accessing repo", ex);
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
