package com.dean.started.security.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Dean
 * @date 2021-04-02
 */
@Data
@Entity
public class SysRole implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleTypeEnum roleName;

    public SysRole(RoleTypeEnum roleName) {
        this.roleName = roleName;
    }

    public SysRole() {
    }
}

