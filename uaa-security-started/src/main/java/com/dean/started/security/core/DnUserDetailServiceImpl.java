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

import java.util.Objects;

/**
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

    @Override
    public UserDetails loadUserByUsername(String uniqueKey) throws UsernameNotFoundException {
        SysUser findOne = repo.findByUsername(uniqueKey);
        if (Objects.isNull(findOne)) {
            findOne = repo.findByMobile(uniqueKey);
        }
        if (Objects.isNull(findOne)) {
            throw new UsernameNotFoundException("not found user:" + uniqueKey);
        }
        AuthUser authUser = new AuthUser();
        BeanUtils.copyProperties(findOne, authUser);
        return authUser;
    }

}
