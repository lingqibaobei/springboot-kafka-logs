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
### 2.2 PeopleCtrl示例接口

>  提供一个获取登录用户信息接口(示例用)，当登录成功，跳转到该接口

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
    @GetMapping("/")
    public Object userInfo() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}

```

### 2.3 验证


默认无需做其他配置，启动应用，请求指定的接口：`http://localhost:8080/`
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

# chapter2 基于内存的用户认证


## 1 实现目标

> 实现: 
**基于内存创建一个用户名为`dean`,密码为`123456`，角色为`ADMIN`的用户**


## 2 添加配置类
> 注意： **.and().formLogin()** 启用默认的登录表单页面

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
                .roles("ADMIN");
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

启动应用，请求指定的接口：`http://localhost:8080/`未登录，则会跳转到默认的登录页面；
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

# chapter3 基于数据库的用户认证

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

    private String mobile;

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
INSERT INTO `uaa`.`sys_user`(`id`, `password`, `username`,`mobile`) VALUES (1, '$2a$10$62ly2.TONU5KKmOY5mQUPeP2tuWjyt0.0SqujX6iWo6tEVMcUesxK', 'dean','13111111111');

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
    
    private String mobile;

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
		SysUser findByUsername = repo.findByUsername(username);
		if(Objects.isNull(findByUsername)) {
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

启动应用，请求指定的接口：`http://localhost:8080/`未登录，则会跳转到默认的登录页面；
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
        "sessionId": "F3650FE26A815736A692E719D044DEC1"
    },
    "authenticated": true,
    "principal": {
        "id": 1,
        "username": "dean",
        "password": "$2a$10$62ly2.TONU5KKmOY5mQUPeP2tuWjyt0.0SqujX6iWo6tEVMcUesxK",
        "mobile": "13111111111",
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

# chapter4 自定义手机号验证码登录1

## 设计规划

> 默认的用户密码验证流程如下:
![username-password-auth](/asset/img/uaa/security/user-pwd-auth.jpg)


> 参照上述流程，设计手机号验证码的验证流程
![mobile-captcha-auth](/asset/img/uaa/security/mobile-captcha-auth.jpg)

## 创建登录页面

> **第一阶段先创建一个基于手机号验证码登录的H5页面**

### 1 MAVEN依赖补充

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```
### 2 创建页面

> 在`resources/templates/mobile-login.html` 创建手机号验证码的登录页面<br>注意css的依赖:`static/css`目录下

```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>登录</title>
    <link rel="stylesheet" th:href="@{css/bootstrap.min.css}"/>
    <link rel="stylesheet" th:href="@{css/signin.css}"/>
</head>
<body>
<div class="container">
    <form class="form-signin" th:action="@{/login/mobile}"
          method="post">
        <h2 class="form-signin-heading">手机验证码登录</h2>
        <div th:if="${param.logout}" class="alert alert-warning" role="alert">已注销</div>
        <div th:if="${param.error}" class="alert alert-danger" role="alert">手机验证码有误，请重试</div>
        <div class="form-group">
            <input type="tel" required maxlength="11" class="form-control" name="mobile" placeholder="手机号"/>
        </div>
        <div class="form-group">
            <input type="text" required class="form-control" name="captcha" placeholder="验证码"/>
        </div>
        <input type="submit" id="login" value="Sign in"
               class="btn btn-lg btn-primary btn-block"/>
    </form>
</div>
</body>
</html>
```
创建完页面，添加一下MVC的映射配置：

> 在之前的PeopleCtrl类添加一天接口，指向`mobile-login.html`的视图<be>
注意：PeopleCtrl的注解用的是`@Controller`，`@RestController`默认会对接口补充`@ResponseBody`

```
@GetMapping("/mobile")
public String mobileLogin() {
    return "mobile-login";
}
```

> [!TIP] 总结页面的功能，创建一个表单提交，主要信息：
**`/mobile`页面URL，`/login/mobile`登录接口，`mobile&captcha`字段，`POST`请求方式；**

**常量信息**

```java
public interface AuthConstants {
    String DEFAULT_MOBILE_LOGIN = "/login/mobile";
    String DEFAULT_MOBILE_LOGIN_PAGE = "/mobile";
    String DEFAULT_MOBILE_LOGIN_ERROR_PAGE = "/mobile?error";
}
```

### 3 更改安全配置类

> 添加对`/mobile`登录页面和`/login/mobile`登录接口的允许访问；移除`formLogin()`的配置

```
.antMatchers(AuthConstants.DEFAULT_MOBILE_LOGIN, AuthConstants.DEFAULT_MOBILE_LOGIN_PAGE).permitAll()
```

浏览器器访问`http://localhost:8080/mobile` 验证页面是否可以请求，页面可以访问后，下一篇开始实现登录的功能

# chapter4 自定义手机号验证码登录2


## 配置登录逻辑

> **这一阶段，添加配置处理手机号验证码的登录逻辑，按照之前的设计规划图进行如下的流程**：

### 1 自定义`DnMobileReqToken`

> 指定手机验证码认证登录需要的字段，示例：**mobile & captcha**字段
```java
public class DnMobileReqToken extends AbstractAuthenticationToken {

    private final String mobile;

    private final String captcha;

    public DnMobileReqToken(final String mobile, final String captcha) {
        super(null);
        this.mobile = mobile;
        this.captcha = captcha;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return captcha;
    }

    @Override
    public Object getPrincipal() {
        return mobile;
    }

}
```


### 2 自定义`DnMobileAuthenticationFilter`


> **主要逻辑**：<br>
1 匹配登录路径`AuthConstants.DEFAULT_MOBILE_LOGIN`,拦截到该路径后执行该Filter,示例的路径为`/login/mobile`<br>
2 校验登录请求参数，封装`DnMobileReqToken`请求参数，后续交给`AuthenticationManager`

```java
public class DnMobileAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String SPRING_SECURITY_FORM_USERNAME_KEY = "mobile";
    private static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "captcha";


    @Setter
    @Getter
    private String accountParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
    @Setter
    @Getter
    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
    @Setter
    @Getter
    private boolean postOnly = true;

    @Setter
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler(AuthConstants.DEFAULT_MOBILE_LOGIN_ERROR_PAGE);

    public DnMobileAuthenticationFilter() {
        super(new AntPathRequestMatcher(AuthConstants.DEFAULT_MOBILE_LOGIN, HttpMethod.POST.name()));
        // 登录失败的处理逻辑: 失败后跳转失败页面 AuthConstants.DEFAULT_MOBILE_LOGIN_ERROR_PAGE
        super.setAuthenticationFailureHandler(failureHandler);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        // 验证请求方式： 是否是仅支持POST
        if (postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        // 验证请求参数
        String mobile = request.getParameter(accountParameter);
        String captcha = request.getParameter(passwordParameter);
        if (mobile == null) {
            throw new AuthenticationServiceException("mobile must not null");
        }
        if (captcha == null) {
            throw new AuthenticationServiceException("captcha must not null");
        }
        // 封装请求对象
        DnMobileReqToken reqToken = new DnMobileReqToken(mobile, captcha);
        reqToken.setDetails(authenticationDetailsSource.buildDetails(request));
        return getAuthenticationManager().authenticate(reqToken);
    }
}

```

### 3 自定义`DnMobileAuthenticationProvider`

> **过滤器拦截后会调用`ProviderManager`委托给`AuthenticationProvider`列表做身份认证**<br>
1 根据`supports`方法匹配是否处理对应的请求，主要逻辑调用`UserDetailsService`加载用户到安全框架<br>
2 加载成功后，封装`Authentication`对象，认证成功后放入`SecurityContextHolder`

```java
public class DnMobileAuthenticationProvider implements AuthenticationProvider {

    private final DnUserDetailServiceImpl userServices;

    public DnMobileAuthenticationProvider(DnUserDetailServiceImpl userServices) {
        this.userServices = userServices;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String mobile = (String) authentication.getPrincipal();
        UserDetails userDetails = null;
        try {
            userDetails = userServices.loadUserByUsername(mobile);
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("账户不存在");
        }
        // TODO 验证captcha
        return createSuccessAuthentication(userDetails);
    }

    /**
     * 成功后构建`Authentication`
     */
    private Authentication createSuccessAuthentication(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                userDetails.getPassword(), userDetails.getAuthorities());
        token.setDetails(userDetails);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return DnMobileReqToken.class.isAssignableFrom(authentication);
    }
}
```

### 4 更改安全配置类


> **把自定义Provider和Filter配置到SecurityConfig中**

**配置`DnMobileAuthenticationProvider`**

```
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(new DnMobileAuthenticationProvider(userDetailsService));
}
```

**配置`DnMobileAuthenticationFilter`**

```
private DnMobileAuthenticationFilter dnAccountPwdAuthenticationFilter(AuthenticationManager manager) {
    DnMobileAuthenticationFilter filter = new DnMobileAuthenticationFilter();
    filter.setAuthenticationManager(manager);
    return filter;
}

 
.and().addFilterBefore(dnAccountPwdAuthenticationFilter(
                super.authenticationManagerBean()), BasicAuthenticationFilter.class)
```

**完整配置**

```java

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.started.chapter", havingValue = "mobile-authentication", matchIfMissing = false)
public class WebSecurityConfigForMobileAuthentication extends WebSecurityConfigurerAdapter {

    private final DnUserDetailServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public WebSecurityConfigForMobileAuthentication(DnUserDetailServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "*.html", "/favicon.ico");
    }

    private DnMobileAuthenticationFilter dnAccountPwdAuthenticationFilter(AuthenticationManager manager) {
        DnMobileAuthenticationFilter filter = new DnMobileAuthenticationFilter();
        filter.setAuthenticationManager(manager);
        return filter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new DnMobileAuthenticationProvider(userDetailsService));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
                .and().authorizeRequests()
                .antMatchers(AuthConstants.DEFAULT_MOBILE_LOGIN, AuthConstants.DEFAULT_MOBILE_LOGIN_PAGE).permitAll()
                .anyRequest().authenticated()
                .and().addFilterBefore(dnAccountPwdAuthenticationFilter(
                super.authenticationManagerBean()), BasicAuthenticationFilter.class)
                .csrf().disable();
        // @formatter:on

    }
}

```

> 添加如下配置,使安全配置类的生效：

```yaml
security:
  started:
    chapter:
      mobile-authentication
```

### 5 验证

> 此时，在浏览器器访问`http://localhost:8080/mobile`手机号验证码登录页面，
输入指定的手机号验证码(`13111111111 & 验证码TODO，随意输入`)，即可登录,登录成功后，跳转到`/`接口，获取登录用户信息如下：

```json
{
    "authorities": [
        {
            "authority": "ROLE_ADMIN"
        }
    ],
    "details": {
        "remoteAddress": "0:0:0:0:0:0:0:1",
        "sessionId": "F3650FE26A815736A692E719D044DEC1"
    },
    "authenticated": true,
    "principal": {
        "id": 1,
        "username": "dean",
        "password": "$2a$10$62ly2.TONU5KKmOY5mQUPeP2tuWjyt0.0SqujX6iWo6tEVMcUesxK",
        "mobile": "13111111111",
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
> [!TIP] **Congratulation！至此你已经实现了自定义的手机号验证码登录；**<br>
此示例只做了手机号验证码登录，你可能会疑问，是否可以账户密码登录和手机验证码登录并存呢？
**当然可以，甚至可以实现：指定哪些规则的请求用账户密码登录，哪些规则的请求用手机号验证码登录**下一篇就来实践一下

# chapter5 实现多身份认证

> 本篇目标实现： **<br>`/user/**`匹配的请求路径，自动跳转到用户名密码认证<br>`/mobile/**`匹配的请求路径，自动跳转到手机号验证码认证**

## 1 调整授权的常量类

```java
public interface AuthConstants {
   String DEFAULT_MOBILE_LOGIN = "/login/mobile";
   String DEFAULT_MOBILE_LOGIN_PAGE = "/mobile";
   String DEFAULT_MOBILE_LOGIN_ERROR_PAGE = "/mobile?error";
 
   String DEFAULT_ACCOUNT_LOGIN = "/login";
   String DEFAULT_ACCOUNT_LOGIN_PAGE = "/account";
   String DEFAULT_ACCOUNT_LOGIN_ERROR_PAGE = "/account?error";
   
   String[] IGNORE_PATTERN = new String[]{
               "/error",
               AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE,
               AuthConstants.DEFAULT_ACCOUNT_LOGIN,
               AuthConstants.DEFAULT_ACCOUNT_LOGIN_ERROR_PAGE,
               AuthConstants.DEFAULT_MOBILE_LOGIN,
               AuthConstants.DEFAULT_MOBILE_LOGIN_PAGE,
               AuthConstants.DEFAULT_MOBILE_LOGIN_ERROR_PAGE
       };
}
```

## 2 自定义EntryPoint实现

> [!TIP] **继承`LoginUrlAuthenticationEntryPoint`重写`determineUrlToUseForThisRequest`方法**，注意：<br>
1 如果请求路径与AuthPoint路径一致，忽略不重定向，否则会循环重定向.<br>
例如：当前请求`/mobile`与登录路径`/mobile`一致,此时直接返回，不应再重定向到 loginFormUrl的`/mobile`<br>
2 提供**String[] ignorePattern**忽略配置的选项

```java
public class DnAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private String[] ignorePattern;

    public DnAuthenticationEntryPoint(String loginFormUrl, String[] ignorePattern) {
        super(loginFormUrl);
        this.ignorePattern = ignorePattern;
    }

    /**
     * 配置请求路径返回不同的登录页路径
     */
    @Setter
    @Getter
    private Map<String, String> authPointMap = Collections.emptyMap();

    /**
     * AntPathRequestMatcher可以实现路径的匹配工作
     */
    @Setter
    @Getter
    private PathMatcher pathMatcher = new AntPathMatcher();


    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
                                                     AuthenticationException exception) {
        String reqUri = request.getRequestURI().replaceAll(request.getContextPath(), "");
        // as the same to the auth point ignore
        if (this.authPointMap.values().contains(reqUri)) {
            return reqUri;
        }
        // ignore pattern
        if (Objects.nonNull(ignorePattern)) {
            for (String s : ignorePattern) {
                if (this.pathMatcher.match(s, reqUri)) {
                    return reqUri;
                }
            }
        }

        // match auth point
        for (String pattern : this.authPointMap.keySet()) {
            if (this.pathMatcher.match(pattern, reqUri)) {
                return this.authPointMap.get(pattern);
            }
        }
        // redirect default loginFormUrl
        return super.determineUrlToUseForThisRequest(request, response, exception);
    }

}

```



## 3 提供账户密码登录页面

> [!WARNING] **还记得上篇自定义的手机号验证码的登录页面吗？这里我没有才有默认的账户登录页面，也定义了登录页面，并修改了登录地址**

### 3.1 补充账户密码登录页面

**修改PeopleCtrl**

```
@GetMapping("/account")
public String accountLogin() {
    return "account-login";
}
```

**补充页面`templates/account-login.html`**

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>登录</title>
    <link rel="stylesheet" th:href="@{css/bootstrap.min.css}"/>
    <link rel="stylesheet" th:href="@{css/signin.css}"/>
</head>
<body>
<div class="container">
    <form class="form-signin" th:action="@{/login}"
          method="post">
        <h2 class="form-signin-heading">账户密码登录</h2>
        <div th:if="${param.logout}" class="alert alert-warning" role="alert">已注销</div>
        <div th:if="${param.error}" class="alert alert-danger" role="alert">账户密码有误，请重试</div>
        <div class="form-group">
            <input type="text" class="form-control" required id="username" name="username" placeholder="username"/>
        </div>
        <div class="form-group">
            <input type="password" required class="form-control" id="password" name="password" placeholder="password"/>
        </div>
        <input type="submit" id="login" value="Sign in"
               class="btn btn-lg btn-primary btn-block"/>
    </form>
</div>
</body>
</html>
```


## 4 更改安全配置类

> **把上面自定义的`DnAuthenticationEntryPoint`设定到安全配置中**，如下：<br>
**`.exceptionHandling().authenticationEntryPoint(dnAuthenticationEntryPoint())`**

```
private DnAuthenticationEntryPoint dnAuthenticationEntryPoint() {
        DnAuthenticationEntryPoint authEntryPoint =
                new DnAuthenticationEntryPoint(AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE,AuthConstants.IGNORE_PATTERN);
        Map<String, String> pointMap = new LinkedHashMap<>();
        pointMap.put("/mobile/**", AuthConstants.DEFAULT_MOBILE_LOGIN_PAGE);
        pointMap.put("/user/**", AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE);
        authEntryPoint.setAuthPointMap(pointMap);
        return authEntryPoint;
    }
```


> **上篇自定义Provider会把默认的DaoAuthenticationProvider,这边手动注入**

```
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // 系统提供的
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    auth.authenticationProvider(authenticationProvider);
    // 自定义
    auth.authenticationProvider(new DnMobileAuthenticationProvider(userDetailsService));
}
```

> **优化忽略路径的处理**
```
.antMatchers(AuthConstants.IGNORE_PATTERN).permitAll()
```

> **重新启动表单提交**

```
.and().formLogin()
        .loginPage(AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE)
        .loginProcessingUrl(AuthConstants.DEFAULT_ACCOUNT_LOGIN)
        .permitAll()
```


### 4.1 完整配置如下：

```java
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.started.chapter", havingValue = "create-entry-point", matchIfMissing = false)
public class WebSecurityConfigForCreateEntryPoint extends WebSecurityConfigurerAdapter {

    private final DnUserDetailServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public WebSecurityConfigForCreateEntryPoint(DnUserDetailServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "*.html", "/favicon.ico");
    }

    private DnMobileAuthenticationFilter dnAccountPwdAuthenticationFilter(AuthenticationManager manager) {
        DnMobileAuthenticationFilter filter = new DnMobileAuthenticationFilter();
        filter.setAuthenticationManager(manager);
        return filter;
    }

    private DnAuthenticationEntryPoint dnAuthenticationEntryPoint() {
        DnAuthenticationEntryPoint authEntryPoint =
                new DnAuthenticationEntryPoint(AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE,AuthConstants.IGNORE_PATTERN);
        Map<String, String> pointMap = new LinkedHashMap<>();
        pointMap.put("/mobile/**", AuthConstants.DEFAULT_MOBILE_LOGIN_PAGE);
        pointMap.put("/user/**", AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE);
        authEntryPoint.setAuthPointMap(pointMap);
        return authEntryPoint;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 系统提供的
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        auth.authenticationProvider(authenticationProvider);
        // 自定义
        auth.authenticationProvider(new DnMobileAuthenticationProvider(userDetailsService));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
                .and().authorizeRequests()
                .antMatchers(AuthConstants.IGNORE_PATTERN).permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                        .loginPage(AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE)
                        .loginProcessingUrl(AuthConstants.DEFAULT_ACCOUNT_LOGIN)
                        .permitAll()
                .and().addFilterBefore(dnAccountPwdAuthenticationFilter(
                        super.authenticationManagerBean()),
                        BasicAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(dnAuthenticationEntryPoint()).and()
                .csrf().disable();
        // @formatter:on

    }

}

```

### 4.2 修改配置文件，启动验证

```yaml
security:
  started:
    chapter:
      create-entry-point
```

**预期结果**
> 1 浏览器访问 `http://localhost:8080/user/123` 会重定向到 `http://localhost:8080/account`进行账户密码认证<br>
2 浏览器访问 `http://localhost:8080/mobile/123` 会重定向到 `http://localhost:8080/mobile`进行手机号验证码认证<br>
登录信息参见之前

# chapter6 登出的实践
> [!INFO|style:flat] **细心的小伙伴会发现前文实现的`多身份认证`，登出只有默认的`/logout`的接口生效，是否能实现如下的效果呢：**<br>
1 `/account/logout` 账户密码登出，退出后跳转到`/account`登录页<br>
2 `/mobile/logout` 手机验证码登出，退出后跳转到`/mobile`登录页

## 1 调整授权的常量类
> **`AuthConstants`补充如下常量，并定义一个退出的RequestMatcher，实现请求匹配该RequestMatcher后执行登出逻辑**

```
String DEFAULT_MOBILE_LOGIN_OUT_PAGE = "/mobile/logout";
String DEFAULT_ACCOUNT_LOGIN_OUT_PAGE = "/account/logout";

/**
 * logout RequestMatcher
 */
OrRequestMatcher LOGOUT_REQUEST_MATCHER = new OrRequestMatcher(
        new AntPathRequestMatcher(AuthConstants.DEFAULT_ACCOUNT_LOGIN_OUT_PAGE, HttpMethod.GET.name()),
        new AntPathRequestMatcher(AuthConstants.DEFAULT_ACCOUNT_LOGIN_OUT_PAGE, HttpMethod.POST.name()),
        new AntPathRequestMatcher(AuthConstants.DEFAULT_MOBILE_LOGIN_OUT_PAGE, HttpMethod.GET.name()),
        new AntPathRequestMatcher(AuthConstants.DEFAULT_MOBILE_LOGIN_OUT_PAGE, HttpMethod.POST.name()));
```

## 2 安全配置调整

> 实现`RequestMatcher`匹配请求，执行logout的Handler处理器，处理成功后的重定向配合自定义的`DnAuthenticationEntryPoint`实现预期的效果

```
.and().logout().logoutRequestMatcher(AuthConstants.LOGOUT_REQUEST_MATCHER)
                        .permitAll()
```

**完整的配置**

```java
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.started.chapter", havingValue = "logout-process", matchIfMissing = false)
public class WebSecurityConfigForLogoutProcess extends WebSecurityConfigurerAdapter {

    private final DnUserDetailServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public WebSecurityConfigForLogoutProcess(DnUserDetailServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "*.html", "/favicon.ico");
    }

    private DnMobileAuthenticationFilter dnAccountPwdAuthenticationFilter(AuthenticationManager manager) {
        DnMobileAuthenticationFilter filter = new DnMobileAuthenticationFilter();
        filter.setAuthenticationManager(manager);
        return filter;
    }

    private DnAuthenticationEntryPoint dnAuthenticationEntryPoint() {
        DnAuthenticationEntryPoint authEntryPoint =
                new DnAuthenticationEntryPoint(AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE,AuthConstants.IGNORE_PATTERN);
        Map<String, String> pointMap = new LinkedHashMap<>();
        pointMap.put("/mobile/**", AuthConstants.DEFAULT_MOBILE_LOGIN_PAGE);
        pointMap.put("/user/**", AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE);
        authEntryPoint.setAuthPointMap(pointMap);
        return authEntryPoint;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 系统提供的
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        auth.authenticationProvider(authenticationProvider);
        // 自定义
        auth.authenticationProvider(new DnMobileAuthenticationProvider(userDetailsService));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
                .and().authorizeRequests()
                .antMatchers(AuthConstants.IGNORE_PATTERN).permitAll()
                .anyRequest().authenticated()
                .and().logout().logoutRequestMatcher(AuthConstants.LOGOUT_REQUEST_MATCHER)
                        .permitAll()
                .and().formLogin()
                        .loginPage(AuthConstants.DEFAULT_ACCOUNT_LOGIN_PAGE)
                        .loginProcessingUrl(AuthConstants.DEFAULT_ACCOUNT_LOGIN)
                        .permitAll()
                .and().addFilterBefore(dnAccountPwdAuthenticationFilter(
                        super.authenticationManagerBean()),
                        BasicAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(dnAuthenticationEntryPoint()).and()
                .csrf().disable();
        // @formatter:on

    }

}

```

## 3 修改配置文件，启动验证

```yaml
security:
  started:
    chapter:
      logout-process
```

**预期结果**
> 1 账户密码认证成功后，浏览器访问`http://localhost:8080/account/logout`,登出重定向到`http://localhost:8080/account`<br>
2 手机号验证码认证成功后，浏览器访问`http://localhost:8080/mobile/logout`,登出重定向到`http://localhost:8080/mobile`<br>
登录信息参见之前

![最终效果](/asset/img/uaa/security/uaa-security-process.gif) 

# chapter7 session共享
> **Spring Security本身依赖于单节点的实现，session存在于内存中，
当在多个容器环境或多实例运行时，改造为redis统一存储,实现session共享**

## maven依赖
```xml
<dependencies>

    <dependency>
        <groupId>org.springframework.session</groupId>
        <artifactId>spring-session-data-redis</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Lettuce pool 连接池 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-pool2</artifactId>
    </dependency>
    
</dependencies>
```

## 配置启用
```java

/**
 * redis实现session共享
 */
@Configuration
@EnableRedisHttpSession
public class SessionConfig {
 
}



@Configuration
@EnableCaching //缓存启动注解
public class RedisConfig {

    //redis连接工厂
    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;

    /**
     * 配置自定义redisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);

        //使用FastJsonRedisSerializer序列化和反序列化redis的key、value值
        template.setValueSerializer(fastJsonRedisSerializer());
        template.setKeySerializer(fastJsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * fastjson序列化Bean
     */
    @Bean
    public FastJsonRedisSerializer<?> fastJsonRedisSerializer() {
        return new FastJsonRedisSerializer<>(Object.class);
    }

}

```

```yaml
spring:
    redis:
        database: 0 #索引
        host: 127.0.0.1
        port: 6379
        password: 123456 #修改成对应自己的redis密码
        lettuce:
          pool:
            max-active: 8 #最大连接数
            max-idle: 8 #最大空闲连接
            min-idle: 0 #最小空闲连接
```
> 配置即可，因为Spring Security已经自动实现将session存在redis
  
  
## 对应redis的存储

- spring.session.sessions.[SESSION_ID].creationTime
- spring.session.sessions.[SESSION_ID].sessionAttr:SPRING_SECURITY_CONTEXT
- spring.session.sessions.[SESSION_ID].maxInactiveInterval
- spring.session.sessions.[SESSION_ID].lastAccessedTime