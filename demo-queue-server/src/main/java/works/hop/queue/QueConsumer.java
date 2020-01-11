package works.hop.queue;

import works.hop.queue.server.Server;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class QueConsumer implements Runnable {

    private final BlockingQueue<QueEvent> queue;
    private final List<QueObserver> observers = new CopyOnWriteArrayList<>();

    public QueConsumer(BlockingQueue<QueEvent> q) {
        this.queue = q;
    }

    public void onMessage(QueEvent event) {
        observers.forEach(obs -> obs.onEvent(event));
    }

    @Override
    public void run() {
        while (true) {
            try {
                QueEvent input = queue.take();
                if (input.equals(Server.POISON_PILL)) {
                    onMessage(new QueEvent(null, input.handler));
                    break;
                }
                onMessage(input);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addObserver(QueObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(QueObserver observer) {
        this.observers.remove(observer);
    }
}
