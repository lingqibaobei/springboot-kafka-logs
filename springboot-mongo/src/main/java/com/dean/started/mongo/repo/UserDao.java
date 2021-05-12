package com.dean.started.mongo.repo;

import com.dean.started.mongo.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Dean
 * @date 2021-05-12
 */
@Repository
public interface UserDao extends MongoRepository<User, String> {

    /**
     * 根据年龄段来查找
     *
     * @param from from
     * @param to   to
     * @return List<User>
     */
    List<User> findByAgeBetween(Integer from, Integer to);


    /**
     * 通过年龄段，用户名，描述（模糊查询）
     *
     * @param from        from
     * @param to          to
     * @param name        name
     * @param description description
     * @return List<User>
     */
    List<User> findByAgeBetweenAndNameEqualsAndDescriptionIsLike(Integer from, Integer to, String name, String description);

    /**
     * 更具描述来模糊查询用户
     *
     * @param description 描述
     * @return List<User>
     */
    List<User> findByDescriptionIsLike(String description);

    /**
     * 通过用户名查询
     *
     * @param name 用户名
     * @return List<User>
     */
    List<User> findByNameEquals(String name);

}