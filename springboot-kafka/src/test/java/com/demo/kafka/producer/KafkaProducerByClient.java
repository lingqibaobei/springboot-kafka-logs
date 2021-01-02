package com.demo.kafka.producer;

import com.demo.kafka.core.MsgProducer;
import org.apache.kafka.clients.producer.*;
import org.springframework.util.StringUtils;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
/**
 * 生产者生产的消息只能被已经注册过的组消费掉(可以先创建好消费组，然后再去mock生产消息)
 * @author fuhw/Dean
 * @date 2019-09-03
 */
public class KafkaProducerByClient {


    private static final String STR_TEMPLATE = "[{\"caller\":\"akucun-product\",\"createTime\":\"2019-09-03 11:15:29\",\"meta\":{\"referer\":\"https://h5.xxcang.com/webapp/share/goods.html?v=2&params=86QN3PL4F&from=groupmessage&isappinstalled=0\",\"remoteip\":\"223.104.2.246\",\"accept-language\":\"zh-cn\",\"x-forwarded-proto\":\"https\",\"method\":\"GET\",\"origin\":\"https://h5.xxcang.com\",\"ip\":\"223.104.2.246\",\"query-string\":\"action=wholeRepost&pageno=1&pagesize=20&params=86QN3PL4F&time=1567480529651&sign=CE846997DF807DA3E124AAA89BCC7877\",\"x-forwarded-for\":\"223.104.2.246\",\"accept\":\"application/json, text/javascript, */*; q=0.01\",\"request-uri\":\"/akucun-product/api/v2.0/live.do\",\"host\":\"zuul.aikucun.com\",\"connection\":\"close\",\"accept-encoding\":\"br, gzip, deflate\",\"user-agent\":\"Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 MicroMessenger/7.0.5(0x17000523) NetType/4G Language/zh_CN\"},\"requestId\":null,\"scene\":\"spider\",\"token\":null}]";

    public static void main(String[] args) throws InterruptedException {
//		String zkServersUrl = "kfk1.test.rangers.co:9092,kfk3.test.rangers.co:9092";
        String zkServersUrl = "localhost:9092";
        String topicName = MsgProducer.getTopicName();
        System.out.println("发送到TOPIC:" + topicName);

        Producer<String, String> producer = createProducer(zkServersUrl);
        for (int j = 0; j < 5; j++) {
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(
                    topicName, System.currentTimeMillis() + "_" + j + "_" + STR_TEMPLATE);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    System.out.println("发送数据：" + recordMetadata);
                }
            });
        }

        TimeUnit.SECONDS.sleep(30);
    }


    /**
     * 配置生产者
     *
     * @param kafkaServersUrl kafka服务地址,集群逗号分隔
     * @return
     * @author DeanKano/DeanKano
     */
    public static Producer<String, String> createProducer(String kafkaServersUrl) {
        if (StringUtils.isEmpty(kafkaServersUrl))
            kafkaServersUrl = "localhost:9092";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServersUrl);
        /**
         * acks=0：意思server不会返回任何确认信息，不保证server是否收到，因为没有返回retires重试机制不会起效。
         * acks=1：意思是partition leader已确认写record到日志中，但是不保证record是否被正确复制(建议设置1)。
         * acks=all：意思是leader将等待所有同步复制broker的ack信息后返回。
         */
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        // 自定义分区规则
//        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,"com.demo.kafka.producer.MyPartitioner");

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
//		props.put(ProducerConfig.LINGER_MS_CONFIG, 5000);
        /**
         * 1. The buffer.memory controls the total amount of memory available to the
         * producer for buffering.
         * 2. 生产者总内存被应用缓存，压缩，及其它运算
         */
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        // props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<String, String>(props);
    }
}
