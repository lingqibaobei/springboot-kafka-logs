# springboot-redis-started

> 基于redisTemplate实践redis的事务，管道，监听，lua脚本等功能


## 环境：

 框架 | 版本 | 
-------- | -----
spring-boot |2.1.6.RELEASE 
spring-boot-starter-data-redis |2.1.6.RELEASE 
lettuce-core |5.1.7.RELEASE |
commons-pool2 |2.6.2|


> Spring Boot2.x 后底层不再是Jedis，默认是Lettuce,使用Lettuce 需要额外引用 commons-pool2 包

```
Lettuce 和 Jedis 的都是连接Redis Server的客户端程序。
1 Jedis在实现上是直连redis server，多线程环境下非线程安全，除非使用连接池，为每个Jedis实例增加物理连接。
2 Lettuce基于Netty的连接实例（StatefulRedisConnection），可以在多个线程间并发访问，且线程安全，
满足多线程环境下的并发访问，同时它是可伸缩的设计，一个连接实例不够的情况也可以按需增加连接实例
```

### get started


#### 1 maven 依赖

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- redis 连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```
#### 2 连接配置（pls：确保redis服务已启动）

```yaml
spring:
  redis:
    database: 13
    host: localhost
    port: 6379
#    password: localhost
    timeout: 6000
    lettuce:
      pool:
        max-active: 5000 # 连接池最大连接数
        max-wait: -1ms  # 连接池最大阻塞等待时间（使用负值表示没有限制）
        min-idle: 5 # 连接池中的最小空闲连接
        max-idle: 2000 # 连接池中的最大空闲连接

```


#### 3 客户端配置

```java
/**
 * @author fuhw/dean
 * @date 2019-05-06
 */
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> getRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
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
```

#### 4 功能说明

 类or目录 | 描述 | 
-------- | -----
com.demo.redis.started.callback | redis execute callback
com.demo.redis.started.config | redis客户端配置，监听配置等
com.demo.redis.started.listener.RedisKeyExpiredListener | 过期key监听
com.demo.redis.started.utils.RedisTemplateLock | 基于lua脚本的redis lock锁实现
com.demo.redis.started.script | redis lua脚本的实践、事务，管道，session等
com.demo.redis.started.sample | hyperLogLog等实践

### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request
