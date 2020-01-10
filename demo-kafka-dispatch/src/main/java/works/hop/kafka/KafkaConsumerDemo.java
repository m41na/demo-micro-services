package works.hop.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class KafkaConsumerDemo {

    public static Logger logger = LoggerFactory.getLogger(KafkaConsumerDemo.class);

    private static KafkaConsumer<String, String> createKafkaConsumer() {
        //create kafka properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "demo-simple-consumer");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //create a producer
        return new KafkaConsumer<>(properties);
    }

    public static void basicConsumer() {
        //create kafka producer
        KafkaConsumer<String, String> consumer = createKafkaConsumer();

        consumer.subscribe(Arrays.asList("first_topic"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                logger.info("Key: " + record.key() + ", Value: " + record.value());
                logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                logger.info("Timestamp: " + record.timestamp() + ", Topic: " + record.topic());
            }
        }
    }

    public static void main(String[] args) {
        basicConsumer();
    }
}
