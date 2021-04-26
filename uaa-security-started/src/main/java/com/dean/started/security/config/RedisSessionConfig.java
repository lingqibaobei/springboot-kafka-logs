//package com.dean.started.security.config;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
//
///**
// * redis实现session共享
// *
// * @author Dean
// * @date 2021-04-20
// */
//@EnableCaching
//@Configuration
//@EnableRedisHttpSession
//@ConditionalOnProperty(name = "security.started.session", havingValue = "share", matchIfMissing = false)
//public class RedisSessionConfig {
//
//    @Bean
//    public RedisTemplate<String, Object> getRedisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        StringRedisSerializer serializer = new StringRedisSerializer();
//        template.setKeySerializer(serializer);
//        template.setValueSerializer(serializer);
//        template.setHashKeySerializer(serializer);
//        template.setHashValueSerializer(serializer);
//        template.afterPropertiesSet();
//        return template;
//    }
//}