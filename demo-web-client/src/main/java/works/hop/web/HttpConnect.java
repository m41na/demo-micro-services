package works.hop.web;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;

import java.util.Map;
import java.util.function.Consumer;

public class HttpConnect {

    public static void request(String url, HttpMethod method, Map<String, String> query, Consumer<HttpConnectResponse> onComplete) throws Exception {
        // Instantiate HttpClient
        HttpClient httpClient = new HttpClient();

        // Configure HttpClient, for example:
        httpClient.setFollowRedirects(false);

        // Start HttpClient
        httpClient.start();

        //invoke request
        Request request = httpClient.newRequest(url);
        request.method(method);
        query.forEach((key, value) -> request.param(key, value));
        request.agent(HttpConnect.class.getSimpleName());
        ContentResponse response = request.send();

        //create response
        HttpConnectResponse reply = new HttpConnectResponse(
                response.getStatus(), response.getContent(), response.getMediaType()
        );
        onComplete.accept(reply);

        //stop client
        httpClient.stop();
    }

    public static class HttpConnectResponse {

        public final Integer status;
        public final byte[] body;
        public final String type;

        public HttpConnectResponse(Integer status, byte[] body, String type) {
            this.status = status;
            this.body = body;
            this.type = type;
        }
    }
}
