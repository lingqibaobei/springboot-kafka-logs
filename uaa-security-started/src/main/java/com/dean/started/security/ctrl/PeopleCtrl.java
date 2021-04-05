package com.dean.started.security.ctrl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    @GetMapping("/user-info")
    public Object userInfo() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
