package works.hop.queue;

import works.hop.queue.entity.QueRequest;
import works.hop.queue.entity.QueRequestListener;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class QueObserver {

    private final Set<QueRequestListener> listeners = new HashSet<>();

    public void register(QueRequestListener listener) {
        System.out.println("registering new listener");
        listeners.add(listener);
    }

    public void unregister(QueRequestListener listener) {
        System.out.println("unregistering existing listener");
        listeners.remove(listener);
    }

    public void receive(QueRequest request, Consumer<String> handler) {
        this.listeners.forEach(listener -> listener.onMessage(request, handler));
    }

    public abstract void complete();

    public void onEvent(QueEvent event) {
        if (event.request != null) {
            QueRequest request = QueRequest.fromAvroClientRequest(event.request);
            receive(request, event.handler);
        }
    }
}
