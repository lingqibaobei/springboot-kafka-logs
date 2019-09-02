package com.demo.kafka.api;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import com.demo.kafka.KafkaConfigUtils;

// @formatter:off
/**
 * @Service( loadbalance = "roundrobin", retries = 2, cluster = "failover",
 * version = "1.0.0", application = "${dubbo.application.id}", protocol =
 * "${dubbo.protocol.id}", registry = "${dubbo.registry.id}" )
 *
 * @author DeanKano/DeanKano
 * @date 2018-05-08
 */
// @formatter:on

@Component
public class LogsToKafkaServiceImplV1 implements LogsToKafkaService {

	private static final Logger log = LoggerFactory.getLogger("kafka-event");

	@Override
	public void produce(String msgContent) {
		if (StringUtils.isEmpty(msgContent)) {
			return;
		}
		log.info(msgContent);
	}

	@Override
	public void produceBatch(List<String> msgContents) {
		if (CollectionUtils.isEmpty(msgContents)) {
			return;
		}
		msgContents.stream().forEach(msg -> log.info(msg));
	}

	@Override
	public void produceBatchByDirect(List<String> msgContents) {
		if (CollectionUtils.isEmpty(msgContents)) {
			return;
		}
		AtomicInteger successedCount = new AtomicInteger(0);
		final int msgSize = msgContents.size();
		final CountDownLatch threadCount = new CountDownLatch(msgSize);
		StopWatch sw = new StopWatch("producer-batch-bydirect");
		sw.start();
		Producer<String, String> producer = KafkaConfigUtils.createProducer();
		msgContents.stream().forEach(msg -> {
			producer.send(new ProducerRecord<String, String>(KafkaConfigUtils.DEFAULT_TOPIC_NAME, msg), new Callback() {
				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if (exception == null) {
						successedCount.incrementAndGet();
						threadCount.countDown();
					} else {
						//TODO 异常数据处理：写入到文件？写入到内存？
					}
				}
			});
		});
		sw.stop();
		try {
			threadCount.await();
			System.out.printf("推送到kafaf记录[%s]条完成,成功[%s]条！%n", msgSize, successedCount);
			System.out.println(sw.prettyPrint());
		} catch (InterruptedException e) {
			System.out.println("推送到kafka出现异常：" + e.getMessage());
		}
	}

}