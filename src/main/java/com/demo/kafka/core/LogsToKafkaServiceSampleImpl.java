package com.demo.kafka.core;

import com.demo.kafka.config.KafkaConfigHelper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * sample implements for
 *
 * @author Dean/Dean
 * @date 2018-05-08
 */
@Component
public class LogsToKafkaServiceSampleImpl implements LogsToKafkaService {

    private static final Logger log = LoggerFactory.getLogger("kafka-event");

    @Override
    public void produceByLog(String msgContent) {
        if (StringUtils.isEmpty(msgContent)) {
            return;
        }
        log.info(msgContent);
    }

    @Override
    public void produceByLogBatch(List<String> msgContents) {
        if (CollectionUtils.isEmpty(msgContents)) {
            return;
        }
        msgContents.forEach(log::info);
    }

    @Override
    public void produceBatchByClient(List<String> msgContents) {
        if (CollectionUtils.isEmpty(msgContents)) {
            return;
        }
        AtomicInteger succeedCounter = new AtomicInteger(0);
        final int msgSize = msgContents.size();
        final CountDownLatch threadCount = new CountDownLatch(msgSize);
        StopWatch sw = new StopWatch("producer-batch-by-client");
        sw.start();
        Producer<String, String> producer = KafkaConfigHelper.createProducer();
        msgContents.forEach(msg -> {
            producer.send(new ProducerRecord<>(KafkaConfigHelper.DEFAULT_TOPIC_NAME, msg), (metadata, exception) -> {
                if (exception == null) {
                    succeedCounter.incrementAndGet();
                    threadCount.countDown();
                } else {
                    //TODO 异常数据处理：写入到文件？写入到内存？
                }
            });
        });
        sw.stop();
        try {
            threadCount.await();
            System.out.printf("推送到kafka记录[%s]条完成,成功[%s]条！%n", msgSize, succeedCounter);
            System.out.println(sw.prettyPrint());
        } catch (InterruptedException e) {
            System.out.println("推送到kafka出现异常：" + e.getMessage());
        }
    }

}