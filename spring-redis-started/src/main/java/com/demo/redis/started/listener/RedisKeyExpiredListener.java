package com.demo.redis.started.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * <p>由于每个key过期都会回调onMessage方法，所以不建议在 onPMessage 回调方法中直接处理业务</p>
 * 这里可以通过 MQ 来做缓冲,在onMessage中把消息直接扔到MQ里，然后再去监听队列消费消息处理具体的业务。
 *
 * @author Dean
 * @date 2021-01-11
 */
@Slf4j
@Component
public class RedisKeyExpiredListener extends KeyExpirationEventMessageListener {

    public RedisKeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("listener [{}] has expired", expiredKey);
    }
}