package works.hop.queue.buffer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TestBufferBehavior {

    public static void main(String[] args) throws UnsupportedEncodingException {
        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.put("hello world".getBytes("UTF-8"));
        printState("added 'hello world' to allocated buffer", buffer);
//        buffer.clear();
//        printState("reset the buffer", buffer);
        ByteBuffer buffer2 = ByteBuffer.wrap("hello world".getBytes("UTF-8"));
        printState("wrapped 'hello world' bytes", buffer2);
        buffer.flip();
        printState("flipped buffer before reading", buffer);
        byte[] output = new byte[buffer.limit()];
        System.out.println(">>>" + new String(output, Charset.forName("UTF-8")) + "\n");
        printState("after reading buffer content", buffer);
        buffer.clear();
        printState("cleared buffer after reading", buffer);
    }

    public static void printState(String header, ByteBuffer buffer) {
        System.out.println(header);
        System.out.println("position: " + buffer.position());
        System.out.println("limit: " + buffer.limit());
        System.out.println("capacity: " + buffer.capacity());
        System.out.println("arrayOffset: " + buffer.arrayOffset());
        System.out.println();
    }
}
