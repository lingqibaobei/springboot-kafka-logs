package com.dean.started.security.core;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

/**
 * @author Dean
 * @date 2021-04-08
 */
public interface AuthConstants {

    String DEFAULT_MOBILE_LOGIN = "/login/mobile";
    String DEFAULT_MOBILE_LOGIN_PAGE = "/mobile";
    String DEFAULT_MOBILE_LOGIN_ERROR_PAGE = "/mobile?error";
    String DEFAULT_MOBILE_LOGIN_OUT_PAGE = "/mobile/logout";
    String DEFAULT_MOBILE_URL_PATTERN = "/mobile/**";

    String DEFAULT_ACCOUNT_LOGIN = "/login";
    String DEFAULT_ACCOUNT_LOGIN_PAGE = "/account";
    String DEFAULT_ACCOUNT_LOGIN_ERROR_PAGE = "/account?error";
    String DEFAULT_ACCOUNT_LOGIN_OUT_PAGE = "/account/logout";
    String DEFAULT_ACCOUNT_URL_PATTERN = "/account/**";

    /**
     * logout RequestMatcher
     */
    OrRequestMatcher LOGOUT_REQUEST_MATCHER = new OrRequestMatcher(
            new AntPathRequestMatcher(AuthConstants.DEFAULT_ACCOUNT_LOGIN_OUT_PAGE, HttpMethod.GET.name()),
            new AntPathRequestMatcher(AuthConstants.DEFAULT_ACCOUNT_LOGIN_OUT_PAGE, HttpMethod.POST.name()),
            new AntPathRequestMatcher(AuthConstants.DEFAULT_MOBILE_LOGIN_OUT_PAGE, HttpMethod.GET.name()),
            new AntPathRequestMatcher(AuthConstants.DEFAULT_MOBILE_LOGIN_OUT_PAGE, HttpMethod.POST.name()));


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
