package com.rangers.datasource.core.db2;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@Profile("multi")
@EnableJpaRepositories(basePackages = {
		"com.rangers.dbsource.db2.repo" }, entityManagerFactoryRef = "entityManagerFactory2", transactionManagerRef = "transactionManager2")
@EnableTransactionManagement
public class DataSource2Config {

	@Bean("datasource2")
	@Qualifier("datasource2")
	@ConfigurationProperties(prefix = "multidb.datasources.db2")
	public DataSource dataSource2() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean(name = "entityManagerFactory2")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory2(EntityManagerFactoryBuilder builder,
			JpaProperties jpaProperties) {

		DataSource dataSource2 = dataSource2();
		return builder.dataSource(dataSource2).properties(jpaProperties.getHibernateProperties(dataSource2))
				.packages("com.rangers.dbsource.db2").persistenceUnit("db2").build();

	}

	@Bean(name = "transactionManager2")
	public PlatformTransactionManager transactionManager2(EntityManagerFactoryBuilder builder,
			JpaProperties jpaProperties) {
		return new JpaTransactionManager(entityManagerFactory2(builder, jpaProperties).getObject());
	}
}
