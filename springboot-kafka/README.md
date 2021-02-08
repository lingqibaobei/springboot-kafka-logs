# springboot-kafka
> springboot集成kafkaTemplate


## 环境：

 框架 | 版本 | 
-------- | -----
spring-boot |2.1.6.RELEASE 
spring-kafka |2.2.5.RELEASE |
kafka-clients |2.0.1 |

> 注意：`分支支持的版本，技术文档更新，版本的对应关系，开始示例前确保kafka服务已启动等`

<img src="https://note.youdao.com/yws/api/personal/file/WEBc4952d3bfa0d6e4cf3e6a6d99c04c35a?method=download&shareKey=80e337b8392524dbae8464674180052c" width="780">

```
建议所有代理> = 0.10.xx的用户（以及所有spring boot 1.5.x用户）使用spring-kafka版本1.3.x或更高版本
Spring Boot 1.5（EOL）用户应使用1.3.x（Boot依赖管理默认情况下将使用1.1.x，因此应予以覆盖）。
Spring Boot 2.1（EOL）用户应使用2.2.x（引导依赖性管理将使用正确的版本）。
Spring Boot 2.2用户应使用2.3.x（引导依赖性管理将使用正确的版本）或将版本覆盖为2.4.x）。
Spring Boot 2.3用户应使用2.5.x（引导依赖项管理将使用正确的版本）。
Spring Boot 2.4用户应该使用2.6.x（Boot依赖管理将使用正确的版本）
```


## Get Started


### 1 maven依赖

```xml
 <dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>${spring-kafka.version}</version>
</dependency>
```

### 2 Producer

> `@EnableKafka`启用kafka自动配置，使用`KafkaTemplate<String, String>` 客户端

### 3 Consumer

> `@KafkaListener(topics = "#{kafkaDynamicTopicProp.topicName}", groupId = "A")` 监听消费kafka

