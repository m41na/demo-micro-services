package works.hop.queue.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import works.hop.queue.entity.avro.AvroClientId;
import works.hop.queue.entity.avro.AvroClientRequest;
import works.hop.queue.entity.avro.AvroRequestType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class QueRequest {

    public Long requestTime;
    public ClientId clientId;
    public RequestType type;
    public String payload;

    @JsonCreator
    public QueRequest(@JsonProperty("requestTime") Long requestTime, @JsonProperty("clientId") ClientId clientId, @JsonProperty("type") RequestType type, @JsonProperty("payload") String payload) {
        this.requestTime = requestTime;
        this.clientId = clientId;
        this.type = type;
        this.payload = payload;
    }

    public static AvroClientRequest fromClientRequest(QueRequest request) {
        AvroClientRequest entity = AvroClientRequest.newBuilder()
                .setType(AvroRequestType.valueOf(request.type.toString()))
                .setClientId(AvroClientId.newBuilder()
                        .setHostName(request.clientId.hostName)
                        .setIpAddress(request.clientId.ipAddress).build())
                .setRequestTime(request.requestTime)
                .setPayload(request.payload).build();
        return entity;
    }

    public static QueRequest fromAvroClientRequest(AvroClientRequest entity) {
        QueRequest request = new QueRequest(
                entity.getRequestTime(),
                new ClientId(entity.getClientId().getIpAddress().toString(), entity.getClientId().getHostName().toString()),
                RequestType.valueOf(entity.getType().toString()),
                entity.getPayload().toString()
        );
        return request;
    }

    public static byte[] serialize(AvroClientRequest entity) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Schema schema = entity.getSchema();
        DatumWriter<AvroClientRequest> outputDatumWriter = new SpecificDatumWriter<>(schema);
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        outputDatumWriter.write(entity, encoder);
        encoder.flush();
        return baos.toByteArray();
    }

    public static AvroClientRequest deserialize(byte[] bytes) throws IOException {
        return AvroClientRequest.fromByteBuffer(ByteBuffer.wrap(bytes));
    }

    public enum RequestType {
        CONNECT, REQUEST, CLOSE
    }

    public static class ClientId {

        public String ipAddress;
        public String hostName;

        @JsonCreator
        public ClientId(@JsonProperty("ipAddress") String ipAddress, @JsonProperty("hostName") String hostName) {
            this.ipAddress = ipAddress;
            this.hostName = hostName;
        }
    }
}
