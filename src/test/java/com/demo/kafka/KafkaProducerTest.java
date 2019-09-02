package com.demo.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KafkaProducerTest {

	public static void main(String[] args) {
		String zkServersUrl = "kfk1.test.rangers.co:9092,kfk3.test.rangers.co:9092";
//		String zkServersUrl = "localhost:9092";

		Producer<String, String> producer = KafkaConfigUtils.createProducer(zkServersUrl);
		for (int j = 0; j < 550; j++) {
			long now = System.currentTimeMillis();
			ProducerRecord<String, String> record = new ProducerRecord<String, String>(
					KafkaConfigUtils.DEFAULT_TOPIC_NAME, now + "xiaolang" + j);
			Future<RecordMetadata> send = producer.send(record);
			try {
				RecordMetadata recordMetadata = send.get();
				System.err.println("push:" + recordMetadata);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

}
