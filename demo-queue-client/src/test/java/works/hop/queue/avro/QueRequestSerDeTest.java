package works.hop.queue.avro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import works.hop.queue.entity.QueRequest;
import works.hop.queue.entity.QueRequestSerDe;
import works.hop.queue.entity.TodoAction;
import works.hop.queue.entity.avro.AvroClientRequest;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class QueRequestSerDeTest {

    private QueRequestSerDe serDe = QueRequestSerDe.instance();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSerializeThenDeserialize() throws Exception {
        long requestTime = new Date().getTime();
        TodoAction todo = new TodoAction("watch movies", false, "CREATE_TODO");
        QueRequest entity = new QueRequest(
                requestTime,
                new QueRequest.ClientId("127.0.0.1", "test-client"),
                QueRequest.RequestType.REQUEST,
                mapper.writeValueAsString(todo));
        byte[] bytes = serDe.serializer(entity);

        //deserialize now
        AvroClientRequest deserialized = serDe.deserialize(bytes);
        assertEquals("Expecting 'REQUEST'", "REQUEST", deserialized.getType().toString());
        assertEquals("Expecting same requestTime", requestTime, deserialized.getRequestTime());
        assertEquals("Expecting '127.0.0.1'", "127.0.0.1", deserialized.getClientId().getIpAddress());
        assertEquals("Expecting 'test-client'", "test-client", deserialized.getClientId().getHostName());

        TodoAction deserializedTodo = mapper.readValue(deserialized.getPayload().toString(), TodoAction.class);
        assertEquals("Expecting 'watch movies'", "watch movies", deserializedTodo.name);
        assertEquals("Expecting 'false''", false, deserializedTodo.completed);
    }
}
