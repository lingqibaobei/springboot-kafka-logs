package com.demo.kafka;

import java.util.Arrays;

import com.demo.kafka.entity.DemoVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
/**
 *
 * @author Dean
 * @date 2020-12-31
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaLogsApplicationTests {

    private static final Logger log = LoggerFactory.getLogger("kafka-event");

    @Test
    public void testSendMessage() throws InterruptedException {
        log.info("user-do:{}",
                Arrays.asList(new DemoVo("xiaolang3", "30005L"),
                        new DemoVo("xiaolang4", "30006L")));
        Thread.sleep(2000);
    }

}
