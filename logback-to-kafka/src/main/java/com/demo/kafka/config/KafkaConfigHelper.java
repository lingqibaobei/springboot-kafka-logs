package com.demo.kafka.config;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Properties;

/**
 * kafka统一配置
 *
 * @author Dean/Dean
 * @date 2018-05-08
 */
public class KafkaConfigHelper {

    /**
     * 示例： kfk1.test.rangers.co:9092,kfk2.test.rangers.co:9092,kfk3.test.rangers.co:9092
     */
    private static final String TOPIC_DATE_PATTERN = "yyyy-MM-dd";
    private static final String TOPIC_PREFIX = "_Dean_";
    private static final String DEFAULT_KAFKA_SEVERS_URL = "localhost:9092";
    public static final String DEFAULT_TOPIC_NAME = getDefaultTopicNameByCurDate();

    private static String getDefaultTopicNameByCurDate() {
        String format = DateFormatUtils.format(new Date(), TOPIC_DATE_PATTERN);
        return TOPIC_PREFIX + format;
    }

    public static Producer<String, String> createProducer() {
        return createProducer(DEFAULT_KAFKA_SEVERS_URL);
    }

    /**
     * 配置生产者
     *
     * @param kafkaServersUrl kafka服务地址,集群逗号分隔
     * @return Producer
     */
    public static Producer<String, String> createProducer(String kafkaServersUrl) {
        if (StringUtils.isEmpty(kafkaServersUrl)) {
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
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    /**
     * @param groupName group name
     * @return KafkaConsumer
     */
    public static KafkaConsumer<String, String> createConsumer(String groupName) {
        return createConsumer(groupName, DEFAULT_KAFKA_SEVERS_URL, true);
    }

    /**
     * @param groupName group name
     * @param autoCommit auto commit
     * @return KafkaConsumer
     */
    public static KafkaConsumer<String, String> createConsumer(String groupName, Boolean autoCommit) {
        return createConsumer(groupName, DEFAULT_KAFKA_SEVERS_URL, autoCommit);
    }

    /**
     * @param groupName group name
     * @param autoCommit auto commit
     * @param kafkaServers kafka servers url
     * @return KafkaConsumer
     */
    public static KafkaConsumer<String, String> createConsumer(String groupName, String kafkaServers, Boolean autoCommit) {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit.toString());
        //自动位移提交间隔时间
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        // latest none earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // 每次poll方法调用都是client与server的一次心跳
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "2");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(props);
    }

}
