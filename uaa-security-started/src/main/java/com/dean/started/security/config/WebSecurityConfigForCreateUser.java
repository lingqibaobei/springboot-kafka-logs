package com.dean.started.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author Dean
 * @date 2021-04-02
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.started.chapter", havingValue = "create-user", matchIfMissing = false)
public class WebSecurityConfigForCreateUser extends WebSecurityConfigurerAdapter {

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
