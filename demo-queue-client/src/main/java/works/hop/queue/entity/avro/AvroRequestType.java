/**
 * Autogenerated by Avro
 * <p>
 * DO NOT EDIT DIRECTLY
 */
package works.hop.queue.entity.avro;

@org.apache.avro.specific.AvroGenerated
public enum AvroRequestType implements org.apache.avro.generic.GenericEnumSymbol<AvroRequestType> {
    CONNECT, REQUEST, CLOSE;
    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"AvroRequestType\",\"namespace\":\"works.hop.queue.entity.avro\",\"symbols\":[\"CONNECT\",\"REQUEST\",\"CLOSE\"]}");

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }

    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }
}
