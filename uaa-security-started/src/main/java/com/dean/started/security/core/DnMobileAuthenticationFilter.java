package com.dean.started.security.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Dean
 * @date 2021-04-06
 * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 */
public class DnMobileAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String SPRING_SECURITY_FORM_USERNAME_KEY = "mobile";
    private static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "captcha";
    public static final String DEFAULT_PATTERN = "/login/account";
    public static final String DEFAULT_LOGIN_PAGE = "/mobile";

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
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler(DEFAULT_LOGIN_PAGE + "?error");

    public DnMobileAuthenticationFilter() {
        super(new AntPathRequestMatcher(DEFAULT_PATTERN, HttpMethod.POST.name()));
        // 登录失败的处理逻辑
        super.setAuthenticationFailureHandler(failureHandler);

    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String mobile = request.getParameter(accountParameter);
        String captcha = request.getParameter(passwordParameter);
        if (mobile == null) {
            throw new AuthenticationServiceException("mobile must not null");
        }
        if (captcha == null) {
            throw new AuthenticationServiceException("captcha must not null");
        }
        DnMobileReqToken reqToken = new DnMobileReqToken(mobile, captcha);
        reqToken.setDetails(authenticationDetailsSource.buildDetails(request));
        return getAuthenticationManager().authenticate(reqToken);
    }
}
