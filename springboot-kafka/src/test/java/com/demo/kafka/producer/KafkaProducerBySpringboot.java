package com.demo.kafka.producer;

import com.demo.kafka.core.MsgProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaProducerBySpringboot {

    @Autowired
    private MsgProducer msgProducer;

    @Test
    public void testSendMessage() {
        msgProducer.sendMessage("test send and listener some messages...");
    }

}
