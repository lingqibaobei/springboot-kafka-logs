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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dean
 * @date 2021-04-02
 */
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

    private DnMobileAuthenticationFilter dnAccountPwdAuthenticationFilter(AuthenticationManager manager) {
        DnMobileAuthenticationFilter filter = new DnMobileAuthenticationFilter();
        filter.setAuthenticationManager(manager);
        return filter;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "*.html", "/favicon.ico");
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
