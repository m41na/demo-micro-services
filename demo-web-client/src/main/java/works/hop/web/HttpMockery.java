package works.hop.web;

import org.eclipse.jetty.http.HttpMethod;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpMockery {

    public static void main(String[] args) throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        for(int i = count.get(); i < 100; i++){
            HttpConnect.request("http://localhost:7080/rest/" + i, HttpMethod.GET, Collections.emptyMap(), (res) -> {
                System.out.printf("%d) %s\n", count.getAndIncrement(), new String(res.body));
            });
            Thread.sleep(50);
        }
        System.out.println("Mock completed");
    }
}
