package com.demo.kafka.core;

import com.demo.kafka.config.KafkaDynamicTopicProp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Dean
 * @date 2021-01-02
 */
@Slf4j
@Component
@EnableConfigurationProperties(KafkaDynamicTopicProp.class)
public class MsgConsumer implements InitializingBean {


    @Value("#{kafkaDynamicTopicProp.topicName}")
    private String topicName;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("the TOPIC of consumer is:{}", topicName);
        Assert.notNull(topicName, "the topicName for kafka Consumer must be not null;");
    }

    /**
     * spring-kafka 1.3.x的版本后，增加了groupId的属性
     * 配置文件配置的是默认的groupId
     * consumer.group-id=defaultConsumer
     */
    @KafkaListener(topics = "#{kafkaDynamicTopicProp.topicName}", groupId = "A")
    public void processMessageGroupA(String content) {
        log.info("[GROUP-A] listener process message: {}", content);
    }


    @KafkaListener(topics = "#{kafkaDynamicTopicProp.topicName}", groupId = "B")
    public void processMessageGroupB(String content) {
        log.info("[GROUP-B] listener process message: {}", content);
    }

}