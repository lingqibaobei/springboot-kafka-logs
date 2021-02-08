package com.demo.kafka;

import com.demo.kafka.config.KafkaConfigHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
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
        newFixedThreadPool.execute(() -> consumerMsg("groupA"));
        newFixedThreadPool.execute(() -> consumerMsg("groupB"));
    }

    private static void consumerMsg(String groupName) {
        try (KafkaConsumer<String, String> consumer = KafkaConfigHelper.createConsumer(groupName, true)) {
            consumer.subscribe(Collections.singletonList(KafkaConfigHelper.DEFAULT_TOPIC_NAME));
            while (true) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("message------------ " + record.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


