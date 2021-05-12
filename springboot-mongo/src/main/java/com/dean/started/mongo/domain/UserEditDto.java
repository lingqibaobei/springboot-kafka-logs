package com.dean.started.mongo.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Dean
 * @date 2021-05-12
 */
@Data
public class UserEditDto {

    private String name;

    private Integer age;

    private String description;
}
