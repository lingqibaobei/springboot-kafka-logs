package com.dean.started.security.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dean
 * @date 2021-04-02
 */
@Data
@Entity
public class SysUser implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String mobile;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private List<SysRole> roles;

    public SysUser(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public SysUser() {
        super();
    }


}
