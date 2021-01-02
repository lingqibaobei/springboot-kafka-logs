package com.demo.kafka.ctrl;

import com.demo.kafka.core.MsgProducer;
import com.demo.kafka.entity.DemoVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Dean
 * @date 2021-01-02
 */
@Slf4j
@RestController
@RequestMapping("producer")
public class DemoCtrl {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MsgProducer msgProducer;

    @Autowired
    public DemoCtrl(MsgProducer msgProducer) {
        this.msgProducer = msgProducer;
    }

    @GetMapping("/do")
    public void providerMsg(@RequestParam(value = "msgContent", defaultValue = "100", required = false) int msgContent) {
        for (int i = 0; i < msgContent; i++) {
            try {
                msgProducer.sendMessage(MAPPER.writeValueAsString(new DemoVo(UUID.randomUUID().toString(), i + "")));
            } catch (JsonProcessingException e) {
                log.error("", e);
            }
        }
    }
}
