package works.hop.acceptor;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AcceptorSocket extends WebSocketAdapter {

    private final Set<Session> sessions = new HashSet<>();

    public void broadcast(String message) {
        sessions.forEach(session -> {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        try {
            this.onClose(getSession(), statusCode, reason);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        try {
            this.onConnect(getSession());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        try {
            this.onError(getSession(), cause);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        super.onWebSocketError(cause);
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        try {
            this.onMessage(getSession(), message);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void onConnect(Session session) throws IOException {
        this.sessions.add(session);
        session.getRemote().sendString("connection accepted");
        System.out.println(session.getRemoteAddress().getHostString() + "connected");
    }

    public void onClose(Session session, int status, String reason) throws IOException {
        this.sessions.remove(session);
        session.getRemote().sendString(String.format("connection closed - status: %d, reason: %s", status, reason));
        System.out.println(session.getRemoteAddress().getHostString() + " closed");
    }

    public void onError(Session session, Throwable error) throws IOException {
        session.getRemote().sendString(String.format("encountered error - error: %s", error.getMessage()));
        System.out.println(session.getRemoteAddress().getHostString() + " error - " + error.getMessage());
    }

    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Message received - " + message);
        if (session.isOpen()) {
            String response = message.toUpperCase();
            session.getRemote().sendString(response);
        }
    }
}
