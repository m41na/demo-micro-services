package works.hop.queue;

import works.hop.queue.entity.avro.AvroClientRequest;

import java.util.function.Consumer;

public class QueEvent {

    public AvroClientRequest request;
    public Consumer<String> handler;

    public QueEvent(AvroClientRequest request, Consumer<String> handler) {
        this.request = request;
        this.handler = handler;
    }
}
