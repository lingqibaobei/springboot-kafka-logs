package com.dean.started.security.config;

import com.dean.started.security.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * @author Dean
 * @date 2021-04-02
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.started.chapter", havingValue = "create-auth-filter-provider", matchIfMissing = false)
public class WebSecurityConfigForCreateAuthFilterAndProvider extends WebSecurityConfigurerAdapter {

    private final DnUserDetailServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public WebSecurityConfigForCreateAuthFilterAndProvider(DnUserDetailServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "*.html", "favicon.ico");
    }

    private DnMobileAuthenticationFilter dnAccountPwdAuthenticationFilter(AuthenticationManager manager) {
        DnMobileAuthenticationFilter filter = new DnMobileAuthenticationFilter();
        filter.setAuthenticationManager(manager);
        return filter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 系统提供的
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        auth.authenticationProvider(authenticationProvider);
        // 自定义
        auth.authenticationProvider(new DnMobileAuthenticationProvider(userDetailsService, passwordEncoder()));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
                .and().authorizeRequests()
                .mvcMatchers(DnMobileAuthenticationFilter.DEFAULT_PATTERN, DnMobileAuthenticationFilter.DEFAULT_LOGIN_PAGE).permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .and().addFilterBefore(dnAccountPwdAuthenticationFilter(
                super.authenticationManagerBean()), BasicAuthenticationFilter.class)
                .csrf().disable();
        // @formatter:on

    }

}
