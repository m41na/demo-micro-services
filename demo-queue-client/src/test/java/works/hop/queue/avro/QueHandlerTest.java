package works.hop.queue.avro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import works.hop.queue.entity.MockCriteria;
import works.hop.queue.entity.QueHandler;
import works.hop.queue.entity.QueRequest;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueHandlerTest {

    ObjectMapper mapper = new ObjectMapper();

    private QueHandler<MockCriteria> listener = new QueHandler<>() {
        @Override
        public MockCriteria convert(String input) {
            try {
                return mapper.readValue(input, MockCriteria.class);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle(MockCriteria message, Consumer<String> handler) {
            handler.accept(new StringBuilder(message.name).append(" is ").append(!message.completed ? " NOT " : " ").append("completed!!").reverse().toString());
        }

        @Override
        public boolean canHandle(QueRequest request) {
            return true;
        }
    };

    @Test
    public void testConvert() {
        MockCriteria todo = listener.convert("{\"name\":\"baking\",\"completed\":true}");
        assertNull("expected null for action value", todo.action);
        assertEquals("Expecting 'baking'", "baking", todo.name);
    }

    @Test
    public void testHandle() {
        MockCriteria todo = new MockCriteria("swimming", false, "add");
        listener.handle(todo, (result) -> {
            assertEquals("!!detelpmoc TON  si gnimmiws", result);
            System.out.println(result);
        });
    }

    @Test
    public void testOnMessage() {
        listener.onMessage(new QueRequest(1000l, null, QueRequest.RequestType.REQUEST, "{\"name\":\"baking\",\"completed\":true}"),
                System.out::println);
    }

    @Test
    public void testFormattingString() {
        String name = "bake";
        String input = String.format("{\"name\":\"%s\",\"completed\":%b}", name, true);
        assertEquals("Expecting matching input", "{\"name\":\"bake\",\"completed\":true}", input);
    }
}
