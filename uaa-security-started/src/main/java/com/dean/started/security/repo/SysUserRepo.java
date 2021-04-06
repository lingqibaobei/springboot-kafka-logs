package com.dean.started.security.repo;

import com.dean.started.security.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Dean
 * @date 2021-04-02
 */
public interface SysUserRepo extends JpaRepository<SysUser, Long> {

    /**
     * find the user by username
     *
     * @param username username
     * @return SysUser
     */
    SysUser findByUsername(String username);

    /**
     * find the user by mobile
     *
     * @param mobile mobile
     * @return SysUser
     */
    SysUser findByMobile(String mobile);

}
