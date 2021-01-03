package com.rangers.dbsource.db1.repo;

import com.rangers.datasource.core.db1.UserDbOneEntity;
import com.rangers.datasource.core.db1.repo.UserDbOneRepo;
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

@ActiveProfiles("multi")
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDbOneRepoTests {

    @Resource
    private UserDbOneRepo repo;

    @Before
    public void init() {
        List<UserDbOneEntity> entities = Arrays.asList(new UserDbOneEntity("AA", "BB"), new UserDbOneEntity("AA", "BB"),
                new UserDbOneEntity("AA", "BB"));
        repo.save(entities);
    }

    @After
    public void destory() {
        repo.deleteAll();
    }

    @Test
    public void find() {
        List<UserDbOneEntity> findAll = repo.findAll();
        System.err.println(findAll);
        Assert.assertNotNull(findAll);
        Assert.assertEquals(findAll.size(), 3);
    }

}
