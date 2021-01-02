package com.demo.kafka.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Dean
 * @date 2021-01-02
 */
@Slf4j
@Component
@EnableKafka
public class MsgProducer {

    private static final String TOPIC_DATE_PATTERN = "yyyy-MM-dd";
    private static final String TOPIC_PREFIX = "_DeanKano_";

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public static void main(String[] args) {
        System.out.println(getTopicName());
    }


    public static String getTopicName() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault());
        String format = localDateTime.format(DateTimeFormatter.ofPattern(TOPIC_DATE_PATTERN));
        return TOPIC_PREFIX + format;
    }

    public void sendMessage(String jsonData) {
        this.sendMessage(null, jsonData);
    }

    public void sendMessage(String topicName, String jsonData) {
        if (StringUtils.isEmpty(topicName)) {
            topicName = getTopicName();
        }
        if (StringUtils.isEmpty(jsonData)) {
            log.debug("push data is empty");
            return;
        }
        try {
            ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(topicName, jsonData);
            sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                @Override
                public void onSuccess(SendResult<String, String> sendResult) {
                    log.info("push to kafka succeed: producerRecord={},metadata={}", sendResult.getProducerRecord(),
                            sendResult.getRecordMetadata());
                }

                @Override
                public void onFailure(Throwable err) {
                    log.error("push to kafka failed:{}", err);
                }
            });
        } catch (Exception e) {
            log.error("push to kafka: topic=[{}],data=[{}],exception:", topicName, jsonData, e);
        }

        // 消息发送的监听器，用于回调返回信息
//        kafkaTemplate.setProducerListener(new ProducerListener<String, String>() {
//            @Override
//            public void onSuccess(String topic, Integer partition, String key, String
//                    value,
//                                  RecordMetadata recordMetadata) {
//            }
//
//            @Override
//            public void onError(String topic, Integer partition, String key, String
//                    value, Exception exception) {
//            }
//
//            @Override
//            public boolean isInterestedInSuccess() {
//                return false;
//            }
//        });
    }

}