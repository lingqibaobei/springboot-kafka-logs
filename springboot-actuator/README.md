# springboot整合actuator

## 一 简介
在Spring Boot的众多模块中，actuator(`spring-boot-starter-actuator`) 不同于其他模块，它完全是一个用于暴露自身信息的模块，主要作用是用于监控与管理

对于实施微服务的中小团队来说，可以有效地减少监控系统在采集应用指标时的开发量。
当然，它也并不是万能的，有时候需要对其做一些简单的扩展来实现个性化的监控需求。

>[!info] 总结：actuator主要应用在监控与管理，极大减少监控系统的开发

## 二 springboot整合actutor

### 1 示例环境

| 工具  | 版本或描述 |
| ----- | ---|
| JDK   | 1.8  |
| IDE   |  `IntelliJ` IDEA |
| Maven | 3.x                  |
| spring-boot-starter | 2.1.6.RELEASE |

### 2 maven依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

> 引入依赖启动应用后台，会看到控制台打印出如下信息


```
... o.s.b.a.e.web.EndpointLinksResolver      : Exposing 20 endpoint(s) beneath base path '/actuator'
```

### 3 验证请求

> [!NOTE] 
`Springboot2.x`所有endpoints默认情况下都已移至`/actuator`下
`Springboot1.x`所有的endpoints直接访问


```shell
# 请求
curl -X GET "http://localhost:8080/actuator"

# 响应

{
    "_links": {
        "self": {
            "href": "http://localhost:8080/actuator",
            "templated": false
        },
        "health": {
            "href": "http://localhost:8080/actuator/health",
            "templated": false
        },
        "health-component-instance": {
            "href": "http://localhost:8080/actuator/health/{component}/{instance}",
            "templated": true
        },
        "health-component": {
            "href": "http://localhost:8080/actuator/health/{component}",
            "templated": true
        },
        "info": {
            "href": "http://localhost:8080/actuator/info",
            "templated": false
        }
    }
}
```


> [!NOTE] 上面是默认暴露的接口，如果你想修改，更改配置application.yml如下：

```shell
management:
  endpoints:
    web:
      exposure:
        include: "*"
```
        
配置之后重新启动访问，即可看到所有的配置，也可以选择性暴露，示例：`"info,env"`


### 4 常见暴露端口释义：


endpoint | 描述
---|:----
autoconfig|显示一个auto-configuration的报告，可以分析候选者有没有应用的原因
beans|显示所有spring Beans的完整列表
configprops|显示一个@ConfigurationProperties的列表
dump|执行一个线程存储env暴露来自spring ConfigurableEnvironment的属性
health|应用健康检查接口info显示应用的配置信息metrics展示当前应用的指标信息
mappings|显示一个@RequestMapping路径的相关信息
shutdown|允许应用优雅的方式关闭，默认不开启
trace显示|trace信息（默认为最新一些http请求）
...|...

## see more details : [博客链接](http://know.himygirl.cn/topic/spring/springboot-actuator/springboot-actuator1.html)
