package works.hop.repo;

import com.practicaldime.zesty.app.IServer;
import com.practicaldime.zesty.testing.server.ZestyJUnit4ClassRunner;
import com.practicaldime.zesty.testing.server.ZestyProvider;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

@RunWith(ZestyJUnit4ClassRunner.class)
@Ignore("run only during integration testing")
public class RepoAppTest {

    public static final Integer PORT = 9099;
    public static final String HOST = "127.0.0.1";
    private RepoApp app;

    @ZestyProvider
    public IServer provider() {
        app = new RepoApp(new TodoDao("/test-todos-db.properties", "/test-create-tables.sql", "/test-initial-data.sql"));
        return app.getProvider().provide(Collections.emptyMap());
    }

    public String getUrl(String endpoint) {
        return String.format("http://%s:%d/%s", HOST, PORT, endpoint);
    }

    @Test
    public void testAddTodoItem(HttpClient client) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(getUrl("todo") + "/movie")
                .method(HttpMethod.POST)
                .timeout(3, SECONDS)
                .send();
        assertEquals("Contains 'hello from server'", true, response.getContentAsString().contains("hello from server"));
    }
}
