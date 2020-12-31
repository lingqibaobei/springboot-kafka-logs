package com.demo.kafka.config;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.util.StringUtils;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * kafka统一配置
 *
 * @author Dean/Dean
 * @date 2018-05-08
 */
public class KafkaConfigHelper {

	private static final String TEST_KAFKA_SEVERS_URL = "kfk1.test.rangers.co:9092,kfk2.test.rangers.co:9092,kfk3.test.rangers.co:9092";

	private static final String TOPIC_DATE_PATTERN = "yyyy-MM-dd";
	private static final String DEFAULT_CONSUMER_GROUP_NAME = "_sys_default_group";
	private static final String TOPIC_PREFIX = "_Dean_";
	private static final String DEFAULT_ZK_SEVERS_URL = "localhost:2181";
	private static final String DEFAULT_KAFKA_SEVERS_URL = "localhost:9092";
	public static final String DEFAULT_TOPIC_NAME = getDefaultTopicNameByCurDate();

	private static String getDefaultTopicNameByCurDate() {
		String format = DateFormatUtils.format(new Date(), TOPIC_DATE_PATTERN);
		return TOPIC_PREFIX + format;
	}

	public static ConsumerConnector createHighConsumer(String zkServersUrl) {
		return createHighConsumer(DEFAULT_CONSUMER_GROUP_NAME, zkServersUrl);
	}

	/**
	 * 配置消费者
	 * 
	 * @param groupName
	 *            分组名称
	 * @param zkServersUrl
	 *            zk服务地址,集群逗号分隔
	 * @return
	 * @author Dean/Dean
	 */
	public static ConsumerConnector createHighConsumer(String groupName, String zkServersUrl) {
		if (StringUtils.isEmpty(groupName)){
			groupName = DEFAULT_CONSUMER_GROUP_NAME;
		}

		if (StringUtils.isEmpty(zkServersUrl)){
			zkServersUrl = DEFAULT_ZK_SEVERS_URL;
		}

		Properties props = new Properties();
		// properties.put("zookeeper.connect",
		// "zk1.test.rangers.co:2181,zk2.test.rangers.co:2181,zk3.test.rangers.co:2181");
		props.put("zookeeper.connect", zkServersUrl);
		props.put("offsets.storage", "zookeeper");
		props.put("auto.offset.reset", "largest");
		props.put("auto.commit.enable", "true");
		props.put("auto.commit.interval.ms", "2000");
		props.put("group.id", groupName);
		return Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

	}

	public static Producer<String, String> createProducer() {
		return createProducer(
				StringUtils.isEmpty(TEST_KAFKA_SEVERS_URL) ? DEFAULT_KAFKA_SEVERS_URL : TEST_KAFKA_SEVERS_URL);
	}

	/**
	 * 配置生产者
	 * 
	 * @param kafkaServersUrl
	 *            kafka服务地址,集群逗号分隔
	 * @return
	 * @author Dean/Dean
	 */
	public static Producer<String, String> createProducer(String kafkaServersUrl) {
		if (StringUtils.isEmpty(kafkaServersUrl)){
			kafkaServersUrl = DEFAULT_KAFKA_SEVERS_URL;
		}
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServersUrl);
		/**
		 * acks=0：意思server不会返回任何确认信息，不保证server是否收到，因为没有返回retires重试机制不会起效。
		 * acks=1：意思是partition leader已确认写record到日志中，但是不保证record是否被正确复制(建议设置1)。
		 * acks=all：意思是leader将等待所有同步复制broker的ack信息后返回。
		 */
		props.put(ProducerConfig.ACKS_CONFIG, "1");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		/**
		 * 1.Specify buffer size in config
		 * 2.10.0后product完全支持批量发送给broker，不管你指定不同partition，product都是批量自动发送指定parition上。
		 * 3.当batch.size达到最大值就会触发dosend机制
		 */
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 20000);
		/**
		 * Reduce the no of requests less than 0;
		 * 意思在指定batch.size数量没有达到情况下，在5s内也回推送数据
		 * 
		 */
		props.put(ProducerConfig.LINGER_MS_CONFIG, 5000);
		/**
		 * 1. The buffer.memory controls the total amount of memory available to the
		 * producer for buffering. 
		 * 2. 生产者总内存被应用缓存，压缩，及其它运算
		 */
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		// props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
		return new KafkaProducer<String, String>(props);
	}
}
