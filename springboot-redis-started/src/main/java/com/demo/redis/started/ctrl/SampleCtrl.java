package com.demo.redis.started.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author fuhw/Dean
 * @date 2020-08-05
 */
@RestController
public class SampleCtrl {

    private final ValueOperations<String, String> valueOperations;

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public SampleCtrl(ValueOperations<String, String> valueOperations, RedisTemplate<String, String> redisTemplate) {
        this.valueOperations = valueOperations;
        this.redisTemplate = redisTemplate;
    }

    @Value("${redis.queue.channel:dean-pub-topic}")
    private String listenerChannel;


    @GetMapping("/set")
    public void setValue() {
        valueOperations.set("A", "1000", 30, TimeUnit.SECONDS);
    }

    @GetMapping("/msg/send")
    public void sendMsg() {
        // PUBLISH
        redisTemplate.convertAndSend(listenerChannel, "Hello:" + System.currentTimeMillis());
    }


}
