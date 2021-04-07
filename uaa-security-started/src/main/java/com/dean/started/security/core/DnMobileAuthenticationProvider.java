package com.dean.started.security.core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Dean
 * @date 2021-04-06
 * @see DaoAuthenticationProvider
 */
public class DnMobileAuthenticationProvider implements AuthenticationProvider, InitializingBean {

    private final DnUserDetailServiceImpl userServices;
    private final PasswordEncoder passwordEncoder;

    public DnMobileAuthenticationProvider(DnUserDetailServiceImpl userServices, PasswordEncoder passwordEncoder) {
        this.userServices = userServices;
        this.passwordEncoder = passwordEncoder;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(userServices, "the implements of UserDetail can not null");
    }

}
