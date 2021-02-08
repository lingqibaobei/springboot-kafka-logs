package com.demo.redis.started.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;

/**
 * redis pub/sub队列消息监听
 *
 * @author Dean
 * @date 2021-02-08
 */
@Slf4j
public class RedisQueueListener implements MessageListener {
    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {

        log.info("listener queue received: {}", message);
    }
}