package works.hop.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class KafkaSeekConsumerDemo {

    public static Logger logger = LoggerFactory.getLogger(KafkaSeekConsumerDemo.class);

    private static KafkaConsumer<String, String> createKafkaConsumer() {
        //create kafka properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //create a producer
        return new KafkaConsumer<>(properties);
    }

    public static void main(String[] args) {
        basicConsumer();
    }

    private static void basicConsumer() {
        CountDownLatch latch = new CountDownLatch(1);
        logger.info("creating the ");
        ConsumerThread consumer = new ConsumerThread("my_thread_topic", latch);
        Thread thread = new Thread(consumer);
        thread.start();

        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("caught shutdown hook");
            consumer.shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("information got interrupted", e);
        } finally {
            logger.info("application is closing");
        }
        logger.info("Application has shutdown");
    }

    public static class ConsumerThread implements Runnable {

        private String topic;
        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;

        public ConsumerThread(String topic, CountDownLatch latch) {
            this.topic = topic;
            this.latch = latch;
            //create kafka producer
            this.consumer = createKafkaConsumer();
        }

        @Override
        public void run() {
            //assign and seek is used to reply data of fetch a specific message (or range of messages)
            //assign
            TopicPartition partition = new TopicPartition(topic, 0);
            long offsetToReadFrom = 15L;
            consumer.assign(Arrays.asList(partition));

            //seek
            consumer.seek(partition, offsetToReadFrom);

            //loop controls
            int numOfMessagesToread = 5;
            boolean keepOnReading = true;
            int numOfMessagesRead = 0;

            //poll for new data
            try {
                while (keepOnReading) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key: " + record.key() + ", Value: " + record.value());
                        logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                        logger.info("Timestamp: " + record.timestamp() + ", Topic: " + record.topic());
                        if (numOfMessagesRead >= numOfMessagesToread) {
                            keepOnReading = false; //to exit while loop
                            break; //to exit for loop
                        }
                    }
                }

                logger.info("Exiting the application");
            } catch (WakeupException e) {
                logger.info("Received shutdown signal through interruption of consumer.poll");
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        public void shutdown() {
            //wakeup method is a special method to interrupt consumer.poll
            //it will throw a WakeupException
            consumer.wakeup();
        }
    }
}
