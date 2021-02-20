package com.demo.redis.started.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;

/**
 * redis pub/sub队列消息监听
 *
 *
 * 典型的优点是：
 * 1.典型的广播模式，一个消息可以发布到多个消费者
 * 2.多信道订阅，消费者可以同时订阅多个信道，从而接收多类消息
 * 3.消息即时发送，消息不用等待消费者读取，消费者会自动接收到信道发布的消息
 *
 * 缺点：
 * 1.消息一旦发布，不能接收。换句话就是发布时若客户端不在线，则消息丢失，不能寻回
 * 2.不能保证每个消费者接收的时间是一致的
 * 3.若消费者客户端出现消息积压，到一定程度，会被强制断开，导致消息意外丢失。通常发生在消息的生产远大于消费速度时
 *
 * 由此可见，Pub/Sub 模式不适合做消息存储，消息积压类的业务，而是擅长处理广播，即时通讯，即时反馈的业务
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