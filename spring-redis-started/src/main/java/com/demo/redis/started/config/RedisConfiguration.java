package com.demo.redis.started.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author fuhw/dean
 * @date 2019-05-06
 */
@Configuration
public class RedisConfiguration {


//    @Autowired
//    private RedisProperties redisProperties;
//    @Bean
//    public JedisConnectionFactory redisClusterFactory() {
//        RedisClusterConfiguration redisClusterConfig = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
//        redisClusterConfig.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());
//        return new JedisConnectionFactory(redisClusterConfig);
//    }
//
//    @Bean
//    public JedisConnectionFactory redisStandaloneFactory() {
//        RedisStandaloneConfiguration redisStandaloneConfig = new RedisStandaloneConfiguration();
//        redisStandaloneConfig.setHostName(redisProperties.getHost());
//        redisStandaloneConfig.setPort(redisProperties.getPort());
//        redisStandaloneConfig.setPassword(redisProperties.getPassword());
//        redisStandaloneConfig.setDatabase(redisProperties.getDatabase());
//        return new JedisConnectionFactory(redisStandaloneConfig);
//    }
//
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//        RedisClusterConfiguration redisClusterConfig = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
//        redisClusterConfig.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());
//
//        //支持自适应集群拓扑刷新和静态刷新源
//        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
//                .enablePeriodicRefresh()
//                .enableAllAdaptiveRefreshTriggers()
//                // ShutdownTimeout ? refreshPeriod
//                .refreshPeriod(redisProperties.getLettuce().getShutdownTimeout())
//                .build();
//
//        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
//                .topologyRefreshOptions(clusterTopologyRefreshOptions).build();
//
//        //从优先，读写分离，读从可能存在不一致，最终一致性CP
//        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
//                .readFrom(ReadFrom.SLAVE_PREFERRED)
//                .clientOptions(clusterClientOptions).build();
//        return new LettuceConnectionFactory(redisClusterConfig, lettuceClientConfiguration);
//    }

    public @Bean
    RedisTemplate<String, Object> getRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用Jackson2JsonRedisSerialize 替换默认序列化 JdkSerializationRedisSerializer StringRedisSerializer
//        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    @Bean
    public ValueOperations<String, String> valueOperations(RedisTemplate<String, String> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    @Bean
    public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    @Bean
    public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }
}