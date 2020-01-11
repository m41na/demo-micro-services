package works.hop.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.function.Function;

public class Connection {

    public static final Logger log = LoggerFactory.getLogger(Connection.class);

    public final SocketChannel channel;
    public final ByteArrayOutputStream accumulator;
    public final Function<SelectionKey, Consumer<String>> onDataEvent;
    public ByteBuffer inputBuffer;
    public ByteBuffer outputBuffer;
    public boolean haveFullMessage;

    public Connection(SocketChannel channel, ByteBuffer inputBuffer, Function<SelectionKey, Consumer<String>> onDataEvent) {
        this.channel = channel;
        this.inputBuffer = inputBuffer;
        this.accumulator = new ByteArrayOutputStream();
        this.onDataEvent = onDataEvent;
        this.haveFullMessage = false;
    }

    public int read() throws IOException {
        int bytesRead = 0;
        int totalRead = 0;

        do {
            totalRead += bytesRead;
            bytesRead = channel.read(inputBuffer);
            accumulator.write(inputBuffer.array(),
                    inputBuffer.arrayOffset(),
                    inputBuffer.limit() - inputBuffer.arrayOffset());
            inputBuffer.clear();

        } while (bytesRead > 0);

        if (totalRead == 0) {
            return 0;
        }

        inputBuffer.flip();
        inputBuffer.clear();
        haveFullMessage = true;

        return bytesRead == 0 ? totalRead : -1;
    }

    public void output(ByteBuffer buffer) {
        this.outputBuffer = buffer;
    }

    public void write(Selector selector, SelectionKey key) throws IOException {
        int written = channel.write(outputBuffer);
        log.info("{} bytes written", written);
        if (outputBuffer.hasRemaining()) {
            outputBuffer.compact();
        } else {
            outputBuffer.clear();
            channel.register(selector, SelectionKey.OP_READ, key.attachment());
        }
    }

    public boolean hasFullMessage() {
        return haveFullMessage;
    }

    public void resetHasFullMessage() {
        this.haveFullMessage = false;
        this.inputBuffer.flip();
        this.inputBuffer.clear();
    }
}
