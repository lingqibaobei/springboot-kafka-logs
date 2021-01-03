package com.demo.kafka;

import com.demo.kafka.config.KafkaConfigHelper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.Future;

/**
 * 
 * @author Dean 
 * @date 2021-01-03
 */
public class KafkaProducerTest {

    public static void main(String[] args) {
        try (Producer<String, String> producer = KafkaConfigHelper.createProducer()) {
            for (int j = 0; j < 10; j++) {
                long now = System.currentTimeMillis();
                ProducerRecord<String, String> record = new ProducerRecord<>(
                        KafkaConfigHelper.DEFAULT_TOPIC_NAME, now + "-dean-" + j);
                Future<RecordMetadata> send = producer.send(record);
                try {
                    RecordMetadata recordMetadata = send.get();
                    System.err.println("push:" + recordMetadata);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
