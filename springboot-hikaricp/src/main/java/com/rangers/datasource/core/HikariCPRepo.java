package com.rangers.datasource.core;

import com.rangers.datasource.core.entity.HikariCPEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HikariCPRepo extends JpaRepository<HikariCPEntity, Long>{

}
