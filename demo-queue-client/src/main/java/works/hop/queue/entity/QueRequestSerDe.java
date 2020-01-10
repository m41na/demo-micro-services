package works.hop.queue.entity;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.util.Utf8;
import works.hop.queue.entity.avro.AvroClientId;
import works.hop.queue.entity.avro.AvroQueRequest;
import works.hop.queue.entity.avro.AvroRequestType;

import java.io.ByteArrayOutputStream;

public class QueRequestSerDe {

    private static QueRequestSerDe instance;

    private final DatumReader<GenericRecord> datumReader;
    private final DatumWriter<GenericRecord> datumWriter;

    private QueRequestSerDe() {
        Schema classSchema = AvroQueRequest.getClassSchema();
        datumReader = new GenericDatumReader<>(classSchema);
        datumWriter = new GenericDatumWriter<>(classSchema);
    }

    public static QueRequestSerDe instance() {
        if (instance == null) {
            instance = new QueRequestSerDe();
        }
        return instance;
    }

    public AvroQueRequest deserialize(byte[] bytes) throws Exception {
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        GenericRecord record = datumReader.read(null, decoder);

        Long requestTime = ((Long) record.get("requestTime"));
        GenericData.EnumSymbol type = ((GenericData.EnumSymbol) record.get("type"));
        String payload = ((Utf8) record.get("payload")).toString();
        GenericData.Record clientId = ((GenericData.Record) record.get("clientId"));

        return AvroQueRequest.newBuilder()
                .setRequestTime(requestTime)
                .setType(AvroRequestType.valueOf(type.toString()))
                .setPayload(payload)
                .setClientId(AvroClientId.newBuilder()
                        .setIpAddress(((Utf8) clientId.get("ipAddress")).toString())
                        .setHostName(((Utf8) clientId.get("hostName")).toString()).build())
                .build();
    }

    public QueRequest deserializer(byte[] bytes) throws Exception {
        return QueRequest.fromAvroQueRequest(deserialize(bytes));
    }

    public byte[] serialize(AvroQueRequest entity) throws Exception {
        GenericData.Record record = new GenericData.Record(AvroQueRequest.getClassSchema());

        record.put("requestTime", entity.getRequestTime());
        record.put("type", entity.getType());
        record.put("payload", entity.getPayload());
        GenericData.Record clientId = new GenericData.Record(AvroClientId.getClassSchema());
        clientId.put("hostName", entity.getClientId().getHostName());
        clientId.put("ipAddress", entity.getClientId().getIpAddress());
        record.put("clientId", clientId);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);

        datumWriter.write(record, encoder);
        encoder.flush();

        return stream.toByteArray();
    }

    public byte[] serializer(QueRequest entity) throws Exception {
        return serialize(QueRequest.fromQueRequest(entity));
    }
}
