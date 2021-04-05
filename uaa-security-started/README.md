# chapter1 入门使用

## 1 环境

| 工具    | 版本或描述 |
| ----- | ------------|
| JDK   | 1.8         |
| IDE   | IntelliJ IDEA |
| Maven | 3.x|
| spring-boot |2.1.6.RELEASE |
| spring-boot-starter-security |2.1.6.RELEASE |
| spring-security-core |5.1.5.RELEASE |
| mysql-connector-java |8.0.16 |


> https://docs.spring.io/spring-security/site/docs/5.1.5.RELEASE/reference/htmlsingle/

## 2 Get started

### 2.1 maven依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
### 2.2 提供一个获取登录用户信息接口(示例用)

```java
/**
 * @author Dean
 * @date 2021-04-02
 */
@Controller
public class PeopleCtrl {

    /**
     * 当前用户信息
     */
    @ResponseBody
    @GetMapping("/user-info")
    public Object userInfo() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}

```

### 2.3 验证


默认无需做其他配置，启动应用，请求指定的接口：`http://localhost:8080/user-info`
`spring security`会检测用户是否登录，未登录，则会跳转到默认的登录页面；
输入默认的用户名密码，即可登录

> 默认用户：user 默认密码，见控制台日志：

```
Using generated security password: 53d8b573-3c71-4988-9153-71b2a717439f
```
登录成功后，响应登录用户信息

```json
{
    "authorities": [],
    "details": {
        "remoteAddress": "0:0:0:0:0:0:0:1",
        "sessionId": "C690BF8234A156573B2DF9891D70DD57"
    },
    "authenticated": true,
    "principal": {
        "password": null,
        "username": "user",
        "authorities": [],
        "accountNonExpired": true,
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "enabled": true
    },
    "credentials": null,
    "name": "user"
}
```

> [!NOTE] 默认配置Servlet创建Filter为名为的`springSecurityFilterChain`。该bean负责应用程序内的所有安全性
(保护应用程序URL，验证提交的用户名和密码，重定向到登录表单等)`UserDetailsService`创建一个`user`的用户，
并将其随机生成的密码记录到控制台


# chapter2 实现自定义用户名(内存)


## 1 实现目标

> 添加如下配置，实现: 
**基于内存创建一个用户名为`dean`,密码为`123456`，角色为`_DN_USER`的用户**
注意： **.and().formLogin()** 启用默认的登录表单页面


## 2 添加配置类
```java
/**
 * @author Dean
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.started.chapter", havingValue = "create-user", matchIfMissing = false)
public class WebSecurityConfigByMemory extends WebSecurityConfigurerAdapter {

    /**
     * 基于内存创建一个用户名为`dean`,密码为`123456`，角色为`_DN_USER`的用户
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .passwordEncoder(new BCryptPasswordEncoder())
                .withUser("dean")
                .password(new BCryptPasswordEncoder().encode("123456"))
                .roles("_DN_USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
                // 启用默认的表单提交页面
                .and().formLogin()
                .and().authorizeRequests()
                .anyRequest().authenticated()
                .and().csrf().disable();
        // @formatter:on

    }

}

```

## 3 修改yaml配置文件

> 添加如下配置,指定配置类的实现

```yaml
security:
  started:
    chapter:
      create-user
```

## 4 验证

启动应用，请求指定的接口：`http://localhost:8080/user-info`未登录，则会跳转到默认的登录页面；
输入指定的用户名密码(`dean & 123456`)，即可登录,登录成功后，响应登录用户信息如下：

```json
{
    "authorities": [
        {
            "authority": "ROLE__DN_USER"
        }
    ],
    "details": {
        "remoteAddress": "0:0:0:0:0:0:0:1",
        "sessionId": "DB593656CCDD9DC1846A5ABDB139C4B3"
    },
    "authenticated": true,
    "principal": {
        "password": null,
        "username": "dean",
        "authorities": [
            {
                "authority": "ROLE_ADMIN"
            }
        ],
        "accountNonExpired": true,
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "enabled": true
    },
    "credentials": null,
    "name": "dean"
}
```


# chapter3 实现自定义用户(MySQL)

## 1 引入MySQL,JPA

### 1.1 maven依赖

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 1.2 配置

```yaml
spring:
  application:
    name: uaa-security-started
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:test}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
  jpa:
    show-sql: true
    properties:
      hibernate.format_sql: true
    hibernate:
      ddl-auto: update
      # 解决springboot2.x默认MyISAM的逻辑
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
```

> 注意：使用JPA自动创建表

## 2 添加用户,角色，用户角色中间表

### 2.1 model

```java

@Data
@Entity
public class SysUser {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private List<SysRole> roles;

    public SysUser(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public SysUser() {
        super();
    }


}


@Data
@Entity
public class SysRole implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleTypeEnum roleName;

    public SysRole(RoleTypeEnum roleName) {
        this.roleName = roleName;
    }

    public SysRole() {
    }
}

```


### 2.2 初始化数据

> **创建一个用户名为`dean`,密码为`123456`，角色为`ROLE_ADMIN`的用户**


```sql
-- 角色
INSERT INTO `uaa`.`sys_role`(`id`, `role_name`) VALUES (1, 'ROLE_SUPER_ADMIN');
INSERT INTO `uaa`.`sys_role`(`id`, `role_name`) VALUES (2, 'ROLE_ADMIN');
INSERT INTO `uaa`.`sys_role`(`id`, `role_name`) VALUES (3, 'ROLE_USER');

-- 用户：password=123456,密文
INSERT INTO `uaa`.`sys_user`(`id`, `password`, `username`) VALUES (1, '$2a$10$62ly2.TONU5KKmOY5mQUPeP2tuWjyt0.0SqujX6iWo6tEVMcUesxK', 'dean');

INSERT INTO `uaa`.`sys_user_roles`(`sys_user_id`, `roles_id`) VALUES (1, 2);
```

## 3 自定义实现用户Authentication

### 3.1 自定义UserDetail的实现`AuthUser`

```java

@Data
public class AuthUser implements UserDetails {

    private Long id;

    private String username;

    private String password;

    @Transient
    private List<SysRole> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (CollectionUtils.isEmpty(roles)) {
            return new ArrayList<>();
        }
        return roles.stream().map(role ->
                new SimpleGrantedAuthority(role.getRoleName().toString())).collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```

### 3.1 自定义UserDetailService的实现`DnUserDetailServiceImpl`

```java
@Service
public class DnUserDetailServiceImpl implements UserDetailsService {

	private final SysUserRepo repo;

	@Autowired
	public DnUserDetailServiceImpl(SysUserRepo repo) {
		this.repo = repo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		List<SysUser> byUsername = repo.findByUsername(username);
		if(CollectionUtils.isEmpty(byUsername)) {
			throw new UsernameNotFoundException("the username not found");
		}
		SysUser findByUsername = byUsername.get(0);
		if (findByUsername == null) {
			throw new UsernameNotFoundException("the username not found");
		}
		AuthUser authUser = new AuthUser();
		BeanUtils.copyProperties(findByUsername, authUser);
		return authUser;
	}

}

```

### 3.3 更改Security的配置
```java

/**
 * @author Dean
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.started.chapter", havingValue = "create-user-detail", matchIfMissing = false)
public class WebSecurityConfigForCreateUserDetail extends WebSecurityConfigurerAdapter {

    private final DnUserDetailServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public WebSecurityConfigForCreateUserDetail(DnUserDetailServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
                // 启用默认的表单提交页面
                .and().formLogin()
                .and().authorizeRequests()
                .anyRequest().authenticated()
                .and().csrf().disable();
        // @formatter:on

    }

}

```

## 4 修改yaml配置文件

> 添加如下配置,指定配置类的实现

```yaml
security:
  started:
    chapter:
      create-user-detail
```

## 5 验证

启动应用，请求指定的接口：`http://localhost:8080/user-info`未登录，则会跳转到默认的登录页面；
输入指定的用户名密码(`dean & 123456`)，即可登录,登录成功后，响应登录用户信息如下：

```json
{
    "authorities": [
        {
            "authority": "ROLE_ADMIN"
        }
    ],
    "details": {
        "remoteAddress": "0:0:0:0:0:0:0:1",
        "sessionId": "4B3CEBE1CF3EDF8A8082F35A28804B7E"
    },
    "authenticated": true,
    "principal": {
        "id": 1,
        "username": "dean",
        "password": "$2a$10$62ly2.TONU5KKmOY5mQUPeP2tuWjyt0.0SqujX6iWo6tEVMcUesxK",
        "roles": [
            {
                "id": 2,
                "roleName": "ROLE_ADMIN"
            }
        ],
        "enabled": true,
        "authorities": [
            {
                "authority": "ROLE_ADMIN"
            }
        ],
        "accountNonExpired": true,
        "accountNonLocked": true,
        "credentialsNonExpired": true
    },
    "credentials": null,
    "name": "dean"
}
```

# chapter4 