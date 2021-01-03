package com.demo.kafka;

import com.demo.kafka.config.KafkaConfigHelper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 能够开发者自己控制 offset，想从哪里读取就从哪里读取。
 * <p>
 * 自行控制连接分区，对分区自定义进行负载均衡
 * <p>
 * 对 zookeeper 的依赖性降低（如：offset 不一定非要靠 zk 存储，自行存储 offset 即可，比如存在文件或者内存中）
 *
 * @author Dean/Dean
 * @date 2018-05-07
 */
public class KafkaLowConsumerTest {

    public static void main(String[] args) {
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.execute(() -> consumerMsg("group1"));
		newFixedThreadPool.execute(() -> consumerMsg("group2"));
    }

    private static void consumerMsg(String groupName) {
        KafkaConsumer<String, String> consumer = getKafkaConsumer(groupName, true);
        consumer.subscribe(Collections.singletonList(KafkaConfigHelper.DEFAULT_TOPIC_NAME));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (TopicPartition partition : records.partitions()) {
                    /**
                     * 指定到上次的offset偏移量consumer.seek(partition, lastOffset);
                     */
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                    for (ConsumerRecord<String, String> record : partitionRecords) {
                        System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(),
                                record.value());
                    }
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    System.out.printf("[partition:{%s}][groupName:{%s}]lastOffset:{%s}", partition.toString(), groupName, lastOffset);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private static KafkaConsumer<String, String> getKafkaConsumer(String groupName, boolean autoCommit) {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "kfk1.test.rangers.co:9092,kfk2.test.rangers.co:9092,kfk3.test.rangers.co:9092");
//		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        if (autoCommit) {
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        } else {
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        }
        // latest none earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // props.put("auto.commit.interval.ms", "1000");
        // 每次poll方法调用都是client与server的一次心跳
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        // props.put("max.poll.records", "2");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(props);
    }

}


