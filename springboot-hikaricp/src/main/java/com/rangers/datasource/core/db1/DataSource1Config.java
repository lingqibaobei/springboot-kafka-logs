package com.rangers.datasource.core.db1;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
		"com.rangers.dbsource.db1.repo" }, entityManagerFactoryRef = "entityManagerFactory1", transactionManagerRef = "transactionManager1")
@EnableTransactionManagement
public class DataSource1Config {

	@Primary
	@Bean("datasource1")
	@Qualifier("datasource1")
	 @ConfigurationProperties(prefix = "multidb.datasources.db1")
	public DataSource dataSource1() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean(name = "entityManagerFactory1")
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory1(EntityManagerFactoryBuilder builder,
			JpaProperties jpaProperties) {

		DataSource dataSource1 = dataSource1();
		return builder.dataSource(dataSource1).properties(jpaProperties.getHibernateProperties(dataSource1))
				.packages("com.rangers.dbsource.db1").persistenceUnit("db1").build();

	}

	@Bean(name = "transactionManager1")
	@Primary
	public PlatformTransactionManager transactionManager1(EntityManagerFactoryBuilder builder,
			JpaProperties jpaProperties) {
		return new JpaTransactionManager(entityManagerFactory1(builder, jpaProperties).getObject());
	}
}
