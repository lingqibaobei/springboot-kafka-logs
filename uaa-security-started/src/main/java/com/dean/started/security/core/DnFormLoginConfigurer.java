package com.dean.started.security.core;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 自定义登录页
 * HttpSecurity#apply(new DnFormLoginConfigurer<>())
 *
 * @author Dean
 * @date 2021-04-07
 * @see FormLoginConfigurer
 */
public class DnFormLoginConfigurer<H extends HttpSecurityBuilder<H>> extends
        AbstractAuthenticationFilterConfigurer<H, DnFormLoginConfigurer<H>, DnMobileAuthenticationFilter> {

    public DnFormLoginConfigurer() {
        super(new DnMobileAuthenticationFilter(), DnMobileAuthenticationFilter.DEFAULT_PATTERN);
        usernameParameter("username");
        passwordParameter("password");
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, HttpMethod.POST.name());
    }

    @Override
    public void init(H http) throws Exception {
        super.init(http);
        initDefaultLoginFilter(http);
    }

    /**
     * If available, initializes the {@link DefaultLoginPageGeneratingFilter} shared
     * object.
     *
     * @param http the {@link HttpSecurityBuilder} to use
     */
    private void initDefaultLoginFilter(H http) {
        DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = http
                .getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if (loginPageGeneratingFilter != null && !isCustomLoginPage()) {
            loginPageGeneratingFilter.setFormLoginEnabled(true);
            loginPageGeneratingFilter.setUsernameParameter(getAccountParameter());
            loginPageGeneratingFilter.setPasswordParameter(getPasswordParameter());
            loginPageGeneratingFilter.setLoginPageUrl(getLoginPage());
            loginPageGeneratingFilter.setFailureUrl(getFailureUrl());
            loginPageGeneratingFilter.setAuthenticationUrl(getLoginProcessingUrl());
        }
    }

    /**
     * Gets the HTTP parameter that is used to submit the password.
     *
     * @return the HTTP parameter that is used to submit the password
     */
    private String getPasswordParameter() {
        return getAuthenticationFilter().getPasswordParameter();
    }

    /**
     * Gets the HTTP parameter that is used to submit the username.
     *
     * @return the HTTP parameter that is used to submit the username
     */
    private String getAccountParameter() {
        return getAuthenticationFilter().getAccountParameter();
    }


    /**
     * The HTTP parameter to look for the password when performing authentication. Default
     * is "password".
     *
     * @param passwordParameter the HTTP parameter to look for the password when
     * performing authentication
     * @return the {@link FormLoginConfigurer} for additional customization
     */
    public DnFormLoginConfigurer<H> passwordParameter(String passwordParameter) {
        getAuthenticationFilter().setPasswordParameter(passwordParameter);
        return this;
    }

    /**
     * The HTTP parameter to look for the username when performing authentication. Default
     * is "username".
     *
     * @param usernameParameter the HTTP parameter to look for the username when
     * performing authentication
     * @return the {@link FormLoginConfigurer} for additional customization
     */
    public DnFormLoginConfigurer<H> usernameParameter(String usernameParameter) {
        getAuthenticationFilter().setAccountParameter(usernameParameter);
        return this;
    }
}
