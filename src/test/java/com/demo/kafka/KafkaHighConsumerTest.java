package com.demo.kafka;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

/**
 * 高级 API 写起来简单
 * 
 * 不需要去自行去 管理offset，系统通过 zookeeper 自行管理
 * 
 * 不需要管理分区，副本等情况，系统自动管理
 * 
 * 消费者断线会自动根据上一次记录在 zookeeper 中的 offset去接着获取数据（默认设置1分钟更新一下 zookeeper 中存的的
 * offset）
 * 
 * 可以使用 group 来区分对同一个 topic 的不同程序访问分离开来（不同的 group 记录不同的 offset，这样不同程序读取同一个 topic
 * 才不会因为 offset 互相影响）
 *
 * @author DeanKano/DeanKano
 * @date 2018-05-07
 */
public class KafkaHighConsumerTest {

	public static void main(String[] args) {
		int msgCount = 0;
		String zkServersUrl = "zk1.test.rangers.co:2181,zk2.test.rangers.co:2181,zk3.test.rangers.co:2181";
		ConsumerConnector consumer = KafkaConfigUtils.createHighConsumer("tt",zkServersUrl);

		Map<String, List<KafkaStream<byte[], byte[]>>> messageSteam = consumer
				.createMessageStreams(Collections.singletonMap(KafkaConfigUtils.DEFAULT_TOPIC_NAME, 1));

		KafkaStream<byte[], byte[]> stream = messageSteam.get(KafkaConfigUtils.DEFAULT_TOPIC_NAME).get(0);
		ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
		while (iterator.hasNext()) {
			MessageAndMetadata<byte[], byte[]> next = iterator.next();
			String message = new String(next.message());
			msgCount++;
			System.err.println("partition=" + next.partition() + ",msg-no." + msgCount + ":" + message);
		}
	}
}
