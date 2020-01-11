package works.hop.queue;

import works.hop.queue.entity.QueHandler;
import works.hop.queue.entity.QueRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class QueObserver {

    private final Set<QueHandler> handlers = new HashSet<>();

    public void register(QueHandler listener) {
        System.out.println("registering new listener");
        handlers.add(listener);
    }

    public void unregister(QueHandler listener) {
        System.out.println("unregistering existing listener");
        handlers.remove(listener);
    }

    public void receive(QueRequest request, Consumer<String> consumer) {
        this.handlers.stream().filter(handler -> handler.canHandle(request)).forEach(listener -> listener.onMessage(request, consumer));
    }

    public void onEvent(QueEvent event) {
        if (event.request != null) {
            QueRequest request = QueRequest.fromAvroQueRequest(event.request);
            receive(request, event.handler);
        }
    }
}
