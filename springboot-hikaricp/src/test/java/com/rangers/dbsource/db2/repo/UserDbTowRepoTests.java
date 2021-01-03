package com.rangers.dbsource.db2.repo;

import java.util.Arrays;
import java.util.List;

import com.rangers.datasource.core.db2.repo.UserDbTowRepo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rangers.datasource.core.db2.UserDbTowEntity;

import javax.annotation.Resource;

@ActiveProfiles("multi")
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDbTowRepoTests {

	@Resource
	private UserDbTowRepo repo;

	@Before
	public void init() {
		List<UserDbTowEntity> entities = Arrays.asList(new UserDbTowEntity("AA", "BB"), new UserDbTowEntity("AA", "BB"),
				new UserDbTowEntity("AA", "BB"));
		repo.save(entities);
	}
	
	@After
	public void destory() {
		repo.deleteAll();
	}

	@Test
	public void find() {
		List<UserDbTowEntity> findAll = repo.findAll();
		System.err.println(findAll);
		Assert.assertNotNull(findAll);
		Assert.assertEquals(findAll.size(), 3);
	}

}
