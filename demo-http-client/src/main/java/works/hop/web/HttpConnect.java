package works.hop.web;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import works.hop.trace.AppTracer;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class HttpConnect {

    private static final CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendService");
    private static final Retry retry = Retry.ofDefaults("backendService");
    private static final Bulkhead bulkhead = Bulkhead.ofDefaults("backendService");
    private static final Tracer tracer = AppTracer.tracer();

    public static Function<HttpClient, HttpConnectResponse> decorate(Function<HttpClient, HttpConnectResponse> handler) {
        return Decorators.ofFunction(handler)
                .withRetry(retry)
                .withCircuitBreaker(circuitBreaker)
                .withBulkhead(bulkhead)
                .decorate();
    }

    public static void request(String url, HttpMethod method, Map<String, String> query, Consumer<HttpConnectResponse> onComplete) throws Exception {
        request(url, method, query, null, onComplete);
    }

    public static void request(String url, HttpMethod method, Map<String, String> query, String body, Consumer<HttpConnectResponse> onComplete) throws Exception {
        // Instantiate HttpClient
        HttpClient httpClient = new HttpClient();
        // Configure HttpClient, for example:
        httpClient.setFollowRedirects(false);
        // Start HttpClient
        httpClient.start();

        //invoke request
        HttpConnectResponse response = decorate(handle(url, method, query, body
        )).apply(httpClient);

        //return response
        onComplete.accept(response);
        //stop client
        httpClient.stop();
    }

    public static Function<HttpClient, HttpConnectResponse> handle(String url, HttpMethod method, Map<String, String> query, String body) {
        return handle(url, method, query, body, "application/json", "application/json");
    }

    public static Function<HttpClient, HttpConnectResponse> handle(String url, HttpMethod method, Map<String, String> query, String body, String contentType, String accepts) {
        return httpClient -> {
            Span span = tracer.buildSpan("handling search").start();
            span.log("HttpConnect request");

            try {
                Request request = httpClient.newRequest(url);
                request.method(method);
                request.header("Content-Type", contentType);
                request.header("Accepts", accepts);
                query.forEach((key, value) -> request.param(key, value));
                if (body != null) request.content(new StringContentProvider(body));
                request.agent(HttpConnect.class.getSimpleName());
                ContentResponse response = request.send();

                //create response
                span.setTag("HttpConnect response status", response.getStatus());
                span.finish();
                return new HttpConnectResponse(
                        response.getStatus(), response.getContent(), response.getMediaType()
                );
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
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
