## springboot+hikariCP 连接池(单数据源，多数据源示例)

<img src="./static/hikaricp-func.png" width="660px" height="400px"> 
		
> hikariCP官网:  https://github.com/brettwooldridge/HikariCP

在引入spring-boot-starter-jdbc后，内部包含了tomcat-jdbc包，里面有tomcat连接池.然后通过自动配置DataSourceAutoConfigurer创建DataSource对象。

SpringBoot创建默认DataSource时，规则如下：

优先寻找创建Tomcat连接池
如果没有Tomcat连接池，会查找创建HikariCP
如果没有HikariCP连接池，会查找创建dbcp
如果没有dbcp连接池，会查找创建dbcp2
可以使用spring.datasource.type属性指定连接池类型

	spring:
	  datasource:
	    type: com.zaxxer.hikari.HikariDataSource
	    
### hikariCP 单数据源配置
<img src="./static/persistent-process.png" width="660px" height="400px"> 	    
	    
#### application-single.yml
	
```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    username: root
    password: root
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/hikaricp?createDatabaseIfNotExist=true&amp;amp;useUnicode=true&amp;amp;characterEncoding=utf-8mb4&amp;amp;autoReconnect=true&useSSL=false
    hikari:
      max-lifetime: 2000000
      maximum-pool-size: 5
      minimum-idle: 3
      auto-commit: true
      connection-timeout: 30000
```
	
### hikariCP 多数据源配置

<img src="./static/multi-persistent-process.png" width="660px" height="400px"> 

#### application-multi.yml

```yaml
multidb:
  datasources:
    db1:
      jdbc-url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/hikaricp?createDatabaseIfNotExist=true&amp;amp;useUnicode=true&amp;amp;characterEncoding=utf-8mb4&amp;amp;autoReconnect=true&useSSL=false
      username: root
      password: root
      driver-class-name: com.mysql.jdbc.Driver
      max-lifetime: 2000000
      maximum-pool-size: 5
      minimum-idle: 3
      auto-commit: true
      connection-timeout: 30000
    db2:
      jdbc-url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3307}/hikaricp?createDatabaseIfNotExist=true&amp;amp;useUnicode=true&amp;amp;characterEncoding=utf-8mb4&amp;amp;autoReconnect=true&useSSL=false
      username: root
      password: root
      driver-class-name: com.mysql.jdbc.Driver
      max-lifetime: 2000000
      maximum-pool-size: 5
      minimum-idle: 3
      auto-commit: true
      connection-timeout: 30000
```

#### DataSource1Config

```java
    @Configuration
	@Profile("multi")
	@EnableJpaRepositories(basePackages = {"com.rangers.dbsource.db1.repo" }, 
	                entityManagerFactoryRef = "entityManagerFactory1", 
	                transactionManagerRef = "transactionManager1")
    @EnableTransactionManagement
    public class DataSource1Config {
    
        @Primary
        @Bean("datasource1")
        @Qualifier("datasource1")
         @ConfigurationProperties(prefix = "multidb.datasources.db1")
        public DataSource dataSource1() {
            return DataSourceBuilder.create().type(HikariDataSource.class).build();
        }
    
        @Bean(name = "entityManagerFactory1")
        @Primary
        public LocalContainerEntityManagerFactoryBean entityManagerFactory1(EntityManagerFactoryBuilder builder,
                JpaProperties jpaProperties) {
    
            DataSource dataSource1 = dataSource1();
            return builder.dataSource(dataSource1).properties(jpaProperties.getHibernateProperties(dataSource1))
                    .packages("com.rangers.dbsource.db1").persistenceUnit("db1").build();
    
        }
    
        @Bean(name = "transactionManager1")
        @Primary
        public PlatformTransactionManager transactionManager1(EntityManagerFactoryBuilder builder,
                JpaProperties jpaProperties) {
            return new JpaTransactionManager(entityManagerFactory1(builder, jpaProperties).getObject());
        }
    }  
```
  
#### DataSource2Config

```java
    @Configuration
	@Profile("multi")
	@EnableJpaRepositories(basePackages = {"com.rangers.dbsource.db2.repo" }, 
			entityManagerFactoryRef = "entityManagerFactory2", 
			transactionManagerRef = "transactionManager2")
	@EnableTransactionManagement
	public class DataSource2Config {

        @Bean("datasource2")
        @Qualifier("datasource2")
        @ConfigurationProperties(prefix = "multidb.datasources.db2")
        public DataSource dataSource2() {
            return DataSourceBuilder.create().type(HikariDataSource.class).build();
        }
    
        @Bean(name = "entityManagerFactory2")
        public LocalContainerEntityManagerFactoryBean entityManagerFactory2(EntityManagerFactoryBuilder builder,
                JpaProperties jpaProperties) {
    
            DataSource dataSource2 = dataSource2();
            return builder.dataSource(dataSource2).properties(jpaProperties.getHibernateProperties(dataSource2))
                    .packages("com.rangers.dbsource.db2").persistenceUnit("db2").build();
    
        }
    
        @Bean(name = "transactionManager2")
        public PlatformTransactionManager transactionManager2(EntityManagerFactoryBuilder builder,
                JpaProperties jpaProperties) {
            return new JpaTransactionManager(entityManagerFactory2(builder, jpaProperties).getObject());
        }
    }
```
  
  
## 执行测试:见测试目录