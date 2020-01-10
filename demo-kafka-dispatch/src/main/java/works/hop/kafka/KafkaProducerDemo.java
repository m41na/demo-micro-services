package works.hop.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducerDemo {

    public static Logger logger = LoggerFactory.getLogger(KafkaProducerDemo.class);

    public static void main(String[] args) {
        basicProducerWithKeys();
    }

    public static void basicProducer() {
        //create kafka producer
        KafkaProducer<String, String> producer = createKafkaProducer();

        //create a record to send
        ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "basic hello world from kafka");

        //send data
        producer.send(record);

        //flush data
        producer.flush();

        //alternatively, flush and close producer
        producer.close();
    }

    public static void basicProducerWithCallback() {
        //create kafka producer
        KafkaProducer<String, String> producer = createKafkaProducer();

        for (int i = 0; i < 5; i++) {
            //create a record to send
            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "kafka with callback " + i);

            //send data
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    //execute every time a record is successfully sent (or exception is thrown)
                    if (e == null) {
                        //success
                        logger.info("Received metadata \n" +
                                "Topic: " + recordMetadata.topic() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" +
                                "Timestamp: " + recordMetadata.timestamp() + "\n");
                    } else {
                        logger.error("error while producing", e);
                    }
                }
            });
        }

        //flush data
        producer.flush();

        //alternatively, flush and close producer
        producer.close();
    }

    public static void basicProducerWithKeys() {
        //create kafka producer
        KafkaProducer<String, String> producer = createKafkaProducer();

        for (int i = 0; i < 5; i++) {
            String topic = "first_topic";
            String value = "kafka with callback " + i;
            String key = "Key__" + i;

            //create a record to send
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            //send data
            producer.send(record, (recordMetadata, e) -> {
                //execute every time a record is successfully sent (or exception is thrown)
                if (e == null) {
                    //success
                    logger.info("Received metadata \n" +
                            "Topic: " + recordMetadata.topic() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "Timestamp: " + recordMetadata.timestamp() + "\n" +
                            "Key: " + key);
                } else {
                    logger.error("error while producing", e);
                }
            });
        }

        //flush data
        producer.flush();

        //alternatively, flush and close producer
        producer.close();
    }

    private static KafkaProducer<String, String> createKafkaProducer() {
        //create kafka properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //create a producer
        return new KafkaProducer<>(properties);
    }
}
