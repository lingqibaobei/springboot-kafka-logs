package com.dean.started.mongo.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Dean
 * @date 2021-05-12
 */
@Data
@Document(collection = "user")
public class User {

    @Id
    private String id;

    private String name;

    private Integer age;

    private String description;

}