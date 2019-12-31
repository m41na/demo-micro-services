package works.hop.repo;

import com.practicaldime.zesty.app.AppProvider;
import com.practicaldime.zesty.app.AppServer;

public class RepoApp {

    public static AppProvider provider = props -> {
        AppServer app = AppServer.instance(props);
        app.get("/repo", (req, res, done) -> {
            res.send("repo: Completed");
            done.complete();
        });
        return app;
    };

    public static void main(String[] args) {
        provider.start(args);
    }
}
