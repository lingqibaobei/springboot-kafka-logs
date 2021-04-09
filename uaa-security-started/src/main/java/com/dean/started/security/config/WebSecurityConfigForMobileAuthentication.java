package com.dean.started.security.config;

import com.dean.started.security.core.AuthConstants;
import com.dean.started.security.core.DnMobileAuthenticationFilter;
import com.dean.started.security.core.DnMobileAuthenticationProvider;
import com.dean.started.security.core.DnUserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
                .and().formLogin()
                .and().addFilterBefore(dnAccountPwdAuthenticationFilter(
                super.authenticationManagerBean()), BasicAuthenticationFilter.class)
                .csrf().disable();
        // @formatter:on

    }

}
