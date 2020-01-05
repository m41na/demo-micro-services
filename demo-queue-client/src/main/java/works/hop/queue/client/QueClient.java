package works.hop.queue.client;

import works.hop.queue.entity.QueRequest;
import works.hop.queue.entity.QueRequestSerDe;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class QueClient {

    private SocketChannel client;

    private QueClient(String host, Integer port) {
        try {
            client = SocketChannel.open(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static QueClient start(String host, Integer port) {
        return new QueClient(host, port);
    }

    public void stop() throws IOException {
        client.close();
    }

    public String sendMessage(String msg) {
        String response;
        try {
            QueRequest request = new QueRequest(new Date().getTime(), new QueRequest.ClientId("localhost", "que-client"), QueRequest.RequestType.REQUEST, msg);
            ByteBuffer buffer = ByteBuffer.wrap(QueRequestSerDe.instance().serializer(request));

            int length = client.write(buffer);
            buffer.clear();
            System.out.printf("sent %d bytes%n", length);

            //read response
            client.read(buffer);
            buffer.flip();
            response = new String(buffer.array()).trim();
            buffer.clear();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return response;
    }
}
