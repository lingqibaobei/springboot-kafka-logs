package com.dean.started.security.core;

import com.dean.started.security.entity.AuthUser;
import com.dean.started.security.entity.SysUser;
import com.dean.started.security.repo.SysUserRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 *
 * @author Dean
 * @date 2021-04-02
 */
@Service
public class DnUserDetailServiceImpl implements UserDetailsService {

	private final SysUserRepo repo;

	@Autowired
	public DnUserDetailServiceImpl(SysUserRepo repo) {
		this.repo = repo;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(String)
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		List<SysUser> byUsername = repo.findByUsername(username);
		if(CollectionUtils.isEmpty(byUsername)) {
			throw new UsernameNotFoundException("the username not found");
		}
		SysUser findByUsername = byUsername.get(0);
		if (findByUsername == null) {
			throw new UsernameNotFoundException("the username not found");
		}
		AuthUser authUser = new AuthUser();
		BeanUtils.copyProperties(findByUsername, authUser);
		return authUser;
	}

}
