package com.rangers.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.Map;


/**
 * @author Dean
 * @date 2021-01-03
 */
@Slf4j
@SpringBootApplication
public class HikariCPApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(HikariCPApplication.class, args);
        Map<String, DataSource> beansOfType = context.getBeansOfType(DataSource.class);
        if (!CollectionUtils.isEmpty(beansOfType)) {
            beansOfType.forEach((k, v) -> {
                log.info("load data source:[k={},v={}]", k, v);
            });
        }
    }
}
