package com.dean.started.security.ctrl;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dean
 * @date 2021-04-02
 */
@Controller
public class PeopleCtrl {

    /**
     * 当前用户信息
     */
    @ResponseBody
    @GetMapping("/")
    public Object userInfo() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @GetMapping("/mobile")
    public String mobileLogin() {
        return "mobile-login";
    }

    @GetMapping("/account")
    public String accountLogin() {
        return "account-login";
    }

    /**
     * 注意：用`@EnableGlobalMethodSecurity(prePostEnabled=true)` 启用功能
     * <p>如果当前用户没有：@PreAuthorize注解中的权限集'uaa:GET:/user/list'，则报403</p>
     */
    @PreAuthorize("hasAuthority('uaa:GET:/user/list')")
    @ResponseBody
    @GetMapping("/user/list")
    public Object userList() {
        Map<String, String> map = new HashMap<>();
        map.put("No.1", "Tom");
        map.put("No.2", "Jaine");
        return map;
    }
}
