package com.demo.redis.started.config;

import com.demo.redis.started.listener.RedisQueueListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * redis的监听器配置
 *
 * @author fuhw/Dean
 * @date 2020-08-05
 */
@Configuration
public class RedisListenerConfig {


    @Value("${redis.queue.channel:dean-pub-topic}")
    private String listenerChannel;

    /**
     * RedisMessageListenerContainer提供订阅消息的多路分发，
     * 这样多个订阅可以共享同一个Redis连接.
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // SUBSCRIBE
        container.addMessageListener(new RedisQueueListener(), new ChannelTopic(listenerChannel));
        return container;
    }


}