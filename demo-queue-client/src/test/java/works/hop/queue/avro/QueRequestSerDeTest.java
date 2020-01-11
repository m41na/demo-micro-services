package works.hop.queue.avro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import works.hop.queue.entity.MockCriteria;
import works.hop.queue.entity.QueRequest;
import works.hop.queue.entity.QueRequestSerDe;
import works.hop.queue.entity.avro.AvroQueRequest;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class QueRequestSerDeTest {

    private QueRequestSerDe serDe = QueRequestSerDe.instance();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSerializeThenDeserialize() throws Exception {
        long requestTime = new Date().getTime();
        MockCriteria todo = new MockCriteria("watch movies", false, "CREATE_TODO");
        QueRequest entity = new QueRequest(
                requestTime,
                new QueRequest.ClientId("127.0.0.1", "test-client"),
                QueRequest.RequestType.REQUEST,
                mapper.writeValueAsString(todo));
        byte[] bytes = serDe.serializer(entity);

        //deserialize now
        AvroQueRequest deserialized = serDe.deserialize(bytes);
        assertEquals("Expecting 'REQUEST'", "REQUEST", deserialized.getType().toString());
        assertEquals("Expecting same requestTime", requestTime, deserialized.getRequestTime());
        assertEquals("Expecting '127.0.0.1'", "127.0.0.1", deserialized.getClientId().getIpAddress());
        assertEquals("Expecting 'test-client'", "test-client", deserialized.getClientId().getHostName());

        MockCriteria deserializedTodo = mapper.readValue(deserialized.getPayload().toString(), MockCriteria.class);
        assertEquals("Expecting 'watch movies'", "watch movies", deserializedTodo.name);
        assertEquals("Expecting 'false''", false, deserializedTodo.completed);
    }
}
