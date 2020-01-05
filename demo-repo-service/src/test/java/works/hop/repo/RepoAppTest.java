package works.hop.repo;

import com.google.gson.Gson;
import com.practicaldime.zesty.app.AppServer;
import com.practicaldime.zesty.testing.server.ZestyJUnit4ClassRunner;
import com.practicaldime.zesty.testing.server.ZestyProvider;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

@RunWith(ZestyJUnit4ClassRunner.class)
@Ignore("figure out how to mock the tracer")
public class RepoAppTest {

    private RepoApp app;
    public static final Integer PORT = 9099;
    public static final String HOST = "127.0.0.1";
    public static final Properties props = new Properties();

    @ZestyProvider
    public AppServer provider() {
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
