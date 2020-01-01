package works.hop.repo;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import works.hop.trace.AppTracer;

public class RepoApp {

    public static AppProvider provider = props -> {
        Tracer tracer = AppTracer.tracer();

        AppServer app = AppServer.instance(props);
        app.get("/repo", (req, res, done) -> {
            Span span = tracer.buildSpan("handling repo").start();
            span.log("RepoApp executing");

            String payload = "repo: Completed";
            res.send(payload);

            span.setTag("RepoApp response", payload);
            span.finish();
            done.complete();
        });
        return app;
    };

    public static void main(String[] args) {
        provider.start(args);
    }
}
