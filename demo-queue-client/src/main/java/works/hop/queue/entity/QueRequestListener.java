package works.hop.queue.entity;

import java.util.function.Consumer;

public interface QueRequestListener<T> {

    default void onMessage(QueRequest request, Consumer<String> handler) {
        //do something useful with the event
        handle(convert(request.payload), handler);
    }

    T convert(String input);

    void handle(T message, Consumer<String> handler);
}
