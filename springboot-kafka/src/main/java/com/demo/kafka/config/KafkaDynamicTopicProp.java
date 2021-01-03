package com.demo.kafka.config;

import com.demo.kafka.core.MsgProducer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 动态topic的实现
 * @author Dean
 * @date 2021-01-02
 */
@Configuration
@ConfigurationProperties(prefix = "kafka.dynamic.topic")
public class KafkaDynamicTopicProp {

	@Setter
	@Getter
	private String topicName = MsgProducer.getTopicName();
}
