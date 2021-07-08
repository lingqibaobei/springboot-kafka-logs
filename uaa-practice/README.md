
> **基于spring-cloud微服务架构,spring oauth2框架，实践分布式uaa统一认证授权中心**[博客链接](http://know.vip.dnlife.fun/uaa/oauth2/)<br>
**参见项目: [devops实践](/devops-practice/README.md)**


## 1 开发环境

依赖| 版本
---|---|
jdk| 1.8 
spring-boot-starter-parent| 2.1.6.RELEASE
spring-boot-starter-oauth2-resource-server| 2.1.6.RELEASE
spring-security-oauth2| 2.3.5.RELEASE
spring-security-oauth2-autoconfigure| 2.1.2.RELEASE
mybatis-plus-boot-starter| 3.1.0 
mysql-connector-java| 8.0.21 
gson| 2.8.5 


## 2 实践功能列表

功能描述| 进度
:----|:---|
token存储动态化配置(jdbc存储，redis存储,jwt) | ✅
jwt增强处理(`OIDC`协议支持,`RSA`非对称加密`JWK`,自定义`Claims`信息)| ✅ [token获取方式](http://know.mobile.himygirl.cn/uaa/oauth2/12dn-uaa-token-retrieve.html)
ClientDetailsService存储配置(jdbc存储，redis存储)| ✅
授权码code存储动态化配置(jdbc存储，redis存储)| ✅
DefaultTokenServices定制化配置| ✅
token(jwt)解析认证方案动态化配置(本地，远程)| ✅
资源授权异常的转换处理(统一异常响应model)| ✅
授权事件的监听处理（成功，失败，拒绝访问等）| ✅
登出自定义处理| ✅
提供code模式换取token的默认endpoint实现(`/oauth/code/token`)| ✅ [示例获取token](http://47.103.88.209:8881/oauth/authorize?client_id=res-service-3rd-01&redirect_uri=http://47.103.88.209:9999/consumer/oauth/code/token&response_type=code&scope=client_credentials&state=123456) deanName&deanPwd
提供自定义授权确认页，登录认证页(三方登录项)等| ✅ [示例UAA服务](http://47.103.88.209:8881) deanName&deanPwd
提供接入三方的认证(gitee,github)| ✅ 微信，qq等其他暂未调试
提供三方接入openId认证 | ✅ [三方接入](http://know.mobile.himygirl.cn/uaa/oauth2/11dn-uaa-thrid-party-provider.html)
授权服务组件化| 🖌暂未开源<br>[示例UAA授权服务](http://47.103.88.209:8881) deanName&deanPwd
资源服务组件化| 🖌暂未开源<br>[示例资源服务](http://47.103.88.209:9999/swagger-ui.html?urls.primaryName=consumer)
