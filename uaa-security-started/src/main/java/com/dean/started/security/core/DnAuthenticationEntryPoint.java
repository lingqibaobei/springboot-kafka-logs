package com.dean.started.security.core;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author fuhw
 * @date 2017年11月30日
 */
public class DnAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private String[] ignorePattern;

    public DnAuthenticationEntryPoint(String loginFormUrl, String[] ignorePattern) {
        super(loginFormUrl);
        this.ignorePattern = ignorePattern;
    }

    /**
     * 配置请求路径返回不同的登录页路径
     */
    private Map<String, String> authPointMap = Collections.emptyMap();

    /**
     * AntPathRequestMatcher可以实现路径的匹配工作
     */
    private PathMatcher pathMatcher = new AntPathMatcher();


    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
                                                     AuthenticationException exception) {
        String reqUri = request.getRequestURI().replaceAll(request.getContextPath(), "");

        if (this.authPointMap.values().contains(reqUri)) {
            return reqUri;
        }

        if (Objects.nonNull(ignorePattern)) {
            for (String s : ignorePattern) {
                if (this.pathMatcher.match(s, reqUri)) {
                    return reqUri;
                }
            }
        }


        for (String pattern : this.authPointMap.keySet()) {
            if (this.pathMatcher.match(pattern, reqUri)) {
                return this.authPointMap.get(pattern);
            }
        }
        return super.determineUrlToUseForThisRequest(request, response, exception);
    }

    public Map<String, String> getAuthPointMap() {
        return authPointMap;
    }

    public void setAuthPointMap(Map<String, String> authPointMap) {
        this.authPointMap = authPointMap;
    }

    public PathMatcher getPathMatcher() {
        return pathMatcher;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }


}
