package works.hop.queue.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.queue.entity.QueRequest;
import works.hop.queue.entity.QueRequestSerDe;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class QueClient {

    public static Logger log = LoggerFactory.getLogger(QueClient.class);
    private final Integer OUTPUT_BUFFER = 4096;
    private final SocketChannel client;

    private QueClient(String host, Integer port) {
        log.info("Making connection request to {}:{}", host, port);
        try {
            client = SocketChannel.open(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Could not establish connection to host '%s' on port '%d'", host, port), e);
        }
    }

    public static QueClient start(String host, Integer port) {
        return new QueClient(host, port);
    }

    public void stop() throws IOException {
        client.close();
    }

    public String sendMessage(String payload) {
        String response;
        try {
            QueRequest request = new QueRequest(new Date().getTime(), new QueRequest.ClientId("localhost", "que-client"), QueRequest.RequestType.REQUEST, payload);
            ByteBuffer input = ByteBuffer.wrap(QueRequestSerDe.instance().serializer(request));
            //write request
            int length = client.write(input);
            log.info("sending {} bytes long request", length);

            //read response
            ByteBuffer output = ByteBuffer.allocate(OUTPUT_BUFFER);
            client.read(output);
            output.flip();
            log.info("response output bytes: position : {}, limit: {}, capacity: {}, arrayOffset: {}", output.position(), output.limit(), output.capacity(), output.arrayOffset());
            response = new String(
                    output.array(),
                    output.arrayOffset(),
                    output.limit() - output.arrayOffset()).trim();
            output.clear();
            log.info("received response {}", response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return response;
    }
}
