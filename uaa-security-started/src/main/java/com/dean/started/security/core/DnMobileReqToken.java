package com.dean.started.security.core;

import org.springframework.security.authentication.AbstractAuthenticationToken;


/**
 * 自定义账户密码Token
 *
 * @author Dean
 * @date 2021-04-06
 * @see org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 */
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
