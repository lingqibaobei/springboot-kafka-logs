package com.dean.started.security.repo;

import com.dean.started.security.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 *
 * @author Dean
 * @date 2021-04-02
 */
public interface SysUserRepo extends JpaRepository<SysUser, Long> {
	
	List<SysUser> findByUsername(String username);
	
}
