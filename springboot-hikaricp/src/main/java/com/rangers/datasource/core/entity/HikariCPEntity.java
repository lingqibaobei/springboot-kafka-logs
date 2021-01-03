package com.rangers.datasource.core.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Dean
 * @date 2021-01-03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hc_tb_one")
public class HikariCPEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String password;

    public HikariCPEntity(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }


}
