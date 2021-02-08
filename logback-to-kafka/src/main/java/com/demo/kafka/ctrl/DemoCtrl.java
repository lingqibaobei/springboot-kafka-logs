package com.demo.kafka.ctrl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.demo.kafka.entity.DemoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.kafka.core.LogsToKafkaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Dean
 * @date 2020-12-31
 */
@RestController
@RequestMapping("producer")
public class DemoCtrl {

    private static final Logger log = LoggerFactory.getLogger("demo");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LogsToKafkaService logsToKafkaService;

    @Autowired
    public DemoCtrl(LogsToKafkaService logsToKafkaService) {
        this.logsToKafkaService = logsToKafkaService;
    }

    @GetMapping("/do")
    public void providerMsg(@RequestParam(value = "msgContent", defaultValue = "1000000", required = false) int msgContent, @RequestParam(value = "isLog", required = false) boolean isLog) {
        List<String> msgContents = new ArrayList<>(msgContent);
        for (int i = 0; i < msgContent; i++) {
            try {
                msgContents.add(MAPPER.writeValueAsString(new DemoVo(UUID.randomUUID().toString(), i + "")));
            } catch (JsonProcessingException e) {
                log.error("", e);
            }
        }
        if (isLog) {
            logsToKafkaService.produceByLogBatch(msgContents);
        } else {
            logsToKafkaService.produceBatchByClient(msgContents);
        }
    }

}
