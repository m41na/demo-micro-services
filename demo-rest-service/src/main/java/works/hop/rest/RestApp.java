package works.hop.rest;

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

public class RestApp {

    public static AppProvider provider = props -> {
        Tracer tracer = AppTracer.tracer();

        AppServer app = AppServer.instance(props);
        app.get("/rest/{count}", (req, res, done) -> {
            String count = req.param("count");
            Span span = tracer.buildSpan("handling rest").start();
            span.log("RestApp executing  (" + count + ")");

            try {
                HttpConnect.request("http://localhost:7081/search", HttpMethod.GET, Collections.emptyMap(),
                        (response) -> {
                            String payload = "rest -> search: " + new String(response.body);
                            res.send(payload);

                            span.setTag("RestApp response", payload);
                            span.finish();
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
