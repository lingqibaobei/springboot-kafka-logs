package com.demo.kafka.core;

import java.util.List;

/**
 * @author Dean
 * @date 2020-12-31
 */
public interface LogsToKafkaService {

    /**
     * produce msg by log
     *
     * @param msgContent message content
     */
    void produceByLog(String msgContent);

    /**
     * produce msg by log(batch)
     *
     * @param msgContents message content
     */
    void produceByLogBatch(List<String> msgContents);

    /**
     * produce msg by client of producer(batch)
     *
     * @param msgContents message content
     */
    void produceBatchByClient(List<String> msgContents);

}
