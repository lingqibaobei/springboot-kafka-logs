package com.rangers.datasource.core.db1;

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
@Table
public class UserDbOneEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String password;

    public UserDbOneEntity(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }


}
