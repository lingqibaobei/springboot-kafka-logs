
# 整合logback收集日志到kafka

## 简述

```
本示例旨在快速整合kafka的使用，调参，优化，压测性能等操作；不建议直接作为生产使用，配置化，异常容错等等待处理；
比如：容错机制
- 如果kafka服务宕机,输出到本地文件,可用其他方式重新load数据记录;
- 也可直接用kafka客户端写入到kafka中,手动针对异常做容错(如,写入文件)
```

## 环境：

| 框架    | 版本或描述 |
| ----- | ------------|
| spring-boot |2.1.6.RELEASE |
| kafka-clients |2.0.1 |

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


##  Get Started

- **1.pom依赖**
- **2.logback.xml配置**
- **3.自定义KafkaAppender**
- **4.测试代码**

-------------------
### 1.kafka相关pom依赖：(0.10.1.1版本)
```xml
<dependency>
	<groupId>org.apache.kafka</groupId>
	<artifactId>kafka-clients</artifactId>
	<version>${kafka.client.version}</version>
	<scope>compile</scope>
	<exclusions>
		<exclusion>
			<artifactId>slf4j-api</artifactId>
			<groupId>org.slf4j</groupId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>org.apache.kafka</groupId>
	<artifactId>kafka_2.11</artifactId>
	<version>${kafka.client.version}</version>
	<exclusions>
		<exclusion>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-log4j12</artifactId>
		</exclusion>
	</exclusions>
</dependency>
```
> sl4j依赖,自行选择;此处整合springboot,未单独引入

### 2.logback的配置
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
	<!--定义日志文件的存储地址 勿在 LogBack 的配置中使用相对路径 -->
	<property name="LOG_HOME" value="logs" />
	<property name="SYS_NAME" value="system" />
	<property name="DATA_NAME" value="data" />
	<property name="APP_LOGS_FILENAME" value="app" />
	<property name="EVENT_LOGS_FILENAME" value="event_loss_data" />
   <!--  <springProperty scope="context" name="APP_LOGS_FILENAME" source="logback.filename.applogs"/>
    <springProperty scope="context" name="EVENT_LOGS_FILENAME" source="logback.filename.eventlogs"/> -->

	<appender name="CONSOLE"
		class="ch.qos.logback.core.ConsoleAppender">
		<layout class="ch.qos.logback.classic.PatternLayout">
			<pattern>[%d] [%-5level] [%thread] [%logger] - %msg%n</pattern>
		</layout>
	</appender>
	
	<appender name="FILE"
		class="ch.qos.logback.core.rolling.RollingFileAppender">
		<rollingPolicy
			class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
			<!--日志文件输出的文件名 -->
			<FileNamePattern>${LOG_HOME}/${SYS_NAME}/${APP_LOGS_FILENAME}.%d{yyyy-MM-dd}.log
			</FileNamePattern>
			<!--日志文件保留天数 -->
			<MaxHistory>10</MaxHistory>
		</rollingPolicy>

		<layout class="ch.qos.logback.classic.PatternLayout">
			<pattern>[%d] [%-5level] [%thread] [%logger] - %msg%n</pattern>
		</layout>
		<!--日志文件最大的大小 -->
		<triggeringPolicy
			class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
			<MaxFileSize>100MB</MaxFileSize>
		</triggeringPolicy>
	</appender>


	<!-- 业务日志：写入kafka -->
	<appender name="KAFKA-EVENTS"
		class="com.demo.kafka.core.KafkaAppender">
		<layout class="ch.qos.logback.classic.PatternLayout">
			<pattern>%msg</pattern>
		</layout>
	</appender>

	<appender name="ASYNC-KAFKA-EVENTS"
		class="ch.qos.logback.classic.AsyncAppender">
		<discardingThreshold>0</discardingThreshold>
		<queueSize>2048</queueSize>
		<appender-ref ref="KAFKA-EVENTS" />
	</appender>

	<logger name="kafka-event" additivity="false">
		<appender-ref ref="ASYNC-KAFKA-EVENTS" />
	</logger>

	<!-- 业务日志：异常 写入本地 -->
	<appender name="LOCAL"
		class="ch.qos.logback.core.rolling.RollingFileAppender">
		<rollingPolicy
			class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy"><!-- 基于时间的策略 -->
			<fileNamePattern>${LOG_HOME}/${DATA_NAME}/${EVENT_LOGS_FILENAME}.%d{yyyy-MM-dd}.log
			</fileNamePattern>
			<!-- 日志文件保留天数 -->
			<MaxHistory>10</MaxHistory>
		</rollingPolicy>
		<triggeringPolicy
			class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
			<!-- 文件大小触发重写新文件 -->
			<MaxFileSize>100MB</MaxFileSize>
			<!-- <totalSizeCap>10GB</totalSizeCap> -->
		</triggeringPolicy>
		<encoder>
			<charset>UTF-8</charset>
			<pattern>[%d] [%-5level] [%thread] [%logger] - %msg%n</pattern>
		</encoder>
	</appender>
	<appender name="ASYNC-LOCAL"
		class="ch.qos.logback.classic.AsyncAppender">
		<discardingThreshold>0</discardingThreshold>
		<queueSize>2048</queueSize>
		<appender-ref ref="LOCAL" />
	</appender>

	<!--万一kafka队列不通,记录到本地 -->
	<logger name="local" additivity="false">
		<appender-ref ref="ASYNC-LOCAL" />
	</logger>

	<root level="INFO">
		<appender-ref ref="CONSOLE" />
		<appender-ref ref="FILE" />
	</root>
	<logger name="org.apache.kafka" level="DEBUG">
	</logger>
	<logger name="org.apache.zookeeper" level="DEBUG">
	</logger>

</configuration>
```

### 3.自定义KafkaAppender
上述logback.xml中的com.demo.kafka.logs.KafkaAppender

``` java
package com.demo.kafka.logs;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.demo.kafka.config.KafkaConfigHelper;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class KafkaAppender<E> extends AppenderBase<E> {
	//此处,logback.xml中的logger的name属性,输出到本地
	private static final Logger log = LoggerFactory.getLogger("local");
	protected Layout<E> layout;
	private Producer<String, String> producer;//kafka生产者

	@Override
	public void start() {
		Assert.notNull(layout, "you don't set the layout of KafkaAppender");
		super.start();
		this.producer = KafkaConfigUtils.createProducer();
	}

	@Override
	public void stop() {
		super.stop();
		producer.close();
		System.out.println("[Stopping KafkaAppender !!!]");
	}

	@Override
	protected void append(E event) {
		String msg = layout.doLayout(event);
		//拼接消息内容
		ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(
				KafkaConfigUtils.DEFAULT_TOPIC_NAME, msg);
		System.out.println("[推送数据]:" + producerRecord);
		//发送kafka的消息
		producer.send(producerRecord, new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				//监听发送结果
				if (exception != null) {
					exception.printStackTrace();
					log.info(msg);
				} else {
					System.out.println("[推送数据到kafka成功]:" + metadata);
				}
			}
		});
	}
	public Layout<E> getLayout() {
		return layout;
	}
	public void setLayout(Layout<E> layout) {
		this.layout = layout;
	}

}

``` 


### 4.测试代码段

```
//logback.xml中logger的name属性(输出到kafka)
private static final Logger log = LoggerFactory.getLogger("kafka-event");
	@Override
	public void produce(String msgContent) {
		if (StringUtils.isEmpty(msgContent)) {
			return;
		}
		//打印日志
		log.info(msgContent);
	}

```

## 其他

> http://localhost:8080/producer/do?msgContent=1000 压测kafka生产消息性能

> com.demo.kafka 该目录测试调参，优化等 [High Consumer,Low Consumer,Producer ,Stream...]

### 参考链接

[https://spring.io/projects/spring-kafka](https://spring.io/projects/spring-kafka)

### 打赏？

| 联系我 | 下午茶(支付宝) |  下午茶(微信)|
| :------: | :------: | :------: |
| <img src="https://note.youdao.com/yws/api/personal/file/WEB87ca0fa2c0ee3e9f6c32fe5523f88c88?method=download&shareKey=907b187d00e41a0128f13e575ddf7f10" width="200"> |<img src="https://note.youdao.com/yws/api/personal/file/WEB143ed930a3562f2e65442a3f5b0e7bdd?method=download&shareKey=7a6847f4a2a61ee61522cf3ae7324846" width="200"> | <img src="https://note.youdao.com/yws/api/personal/file/WEBcc0561b27e1f96f089d624af2ad710ed?method=download&shareKey=b6ada6ef8e555407a34fd19f238eba0b" width="200">|  