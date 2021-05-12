package com.dean.started.mongo.ctrl;

import com.dean.started.mongo.domain.User;
import com.dean.started.mongo.domain.UserAddDto;
import com.dean.started.mongo.domain.UserEditDto;
import com.dean.started.mongo.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Dean
 * @date 2021-05-12
 */
@RestController
@RequestMapping("user")
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    public User createUser(@RequestBody @Valid UserAddDto dto) {
        User entity = new User();
        BeanUtils.copyProperties(dto,entity);
        return userService.createUser(entity);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable String id, @RequestBody @Valid UserEditDto dto) {
        User entity = new User();
        BeanUtils.copyProperties(dto,entity);
        userService.updateUser(id, entity);
    }

    /**
     * 根据用户 id查找
     * 存在返回，不存在返回 null
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        return userService.getUser(id).orElse(null);
    }

    /**
     * 根据年龄段来查找
     */
    @GetMapping("/age/{from}/{to}")
    public List<User> getUserByAge(@PathVariable Integer from, @PathVariable Integer to) {
        return userService.getUserByAge(from, to);
    }

    /**
     * 根据用户名查找
     */
    @GetMapping("/name/{name}")
    public List<User> getUserByName(@PathVariable String name) {
        return userService.getUserByName(name);
    }

    /**
     * 根据用户描述模糊查找
     */
    @GetMapping("/description/{description}")
    public List<User> getUserByDescription(@PathVariable String description) {
        return userService.getUserByDescription(description);
    }

    /**
     * 根据多个检索条件查询
     */
    @GetMapping("/condition")
    public Page<User> getUserByCondition(int size, int page, User user) {
        return userService.getUserByCondition(size, page, user);
    }

}