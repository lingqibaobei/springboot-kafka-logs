package com.demo.redis.started;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dean
 * @date 2021-01-12
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisStartedApplicationTests {

    @Test
    public void contextLoads() {
        // do something
    }
}
