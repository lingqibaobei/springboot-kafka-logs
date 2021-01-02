package com.demo.kafka.producer;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * 自定义分区规则（前提broker的server.properties的num.partitions大于1）
 *
 * @author fuhw/Dean
 * @date 2019-09-03
 */
public class MyPartitioner implements Partitioner {

    /**
     * 生产者发送消息的时候指定KEY
     *
     * @link { public ProducerRecord(String topic, K key, V value) }
     */
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        Integer numPartitions = cluster.partitionCountForTopic(topic);
        try {
            int code = key.hashCode();
            return Math.abs(code % numPartitions);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
