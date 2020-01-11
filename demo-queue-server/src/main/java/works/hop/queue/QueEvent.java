package works.hop.queue;

import works.hop.queue.entity.avro.AvroQueRequest;

import java.util.function.Consumer;

public class QueEvent {

    public AvroQueRequest request;
    public Consumer<String> handler;

    public QueEvent(AvroQueRequest request, Consumer<String> handler) {
        this.request = request;
        this.handler = handler;
    }
}
