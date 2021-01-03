package com.rangers.dbsource;

import com.rangers.datasource.core.HikariCPRepo;
import com.rangers.datasource.core.entity.HikariCPEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@ActiveProfiles("single")
@RunWith(SpringRunner.class)
@SpringBootTest
public class HikariCPRepoTests {

	@Resource
	private HikariCPRepo hikariCPRepo;

	@Before
	public void init() {
		List<HikariCPEntity> entities = Arrays.asList(new HikariCPEntity("AA", "BB"), new HikariCPEntity("AA", "BB"),
				new HikariCPEntity("AA", "BB"));
		hikariCPRepo.save(entities);
	}

	@After
	public void destory() {
		hikariCPRepo.deleteAll();
	}

	@Test
	public void find() {
		List<HikariCPEntity> findAll = hikariCPRepo.findAll();
		System.err.println(findAll);
		Assert.assertNotNull(findAll);
		Assert.assertEquals(findAll.size(), 3);
	}

}
