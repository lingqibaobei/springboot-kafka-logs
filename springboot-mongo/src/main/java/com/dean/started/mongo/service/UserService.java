package com.dean.started.mongo.service;

import com.dean.started.mongo.domain.User;
import com.dean.started.mongo.repo.UserDao;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.model.IndexOptions;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Dean
 * @date 2021-05-12
 */
@Slf4j
@Service
public class UserService {

    private final UserDao userDao;

    private final MongoTemplate template;

    @Autowired
    public UserService(MongoTemplate template, UserDao userDao) {
        this.template = template;
        this.userDao = userDao;
    }

    public List<User> getUsers() {
        return userDao.findAll();
    }

    public Optional<User> getUser(String id) {
        return this.userDao.findById(id);
    }

    /**
     * 新增和修改都是 save方法，
     * id 存在为修改，id 不存在为新增
     */
    public User createUser(User user) {
        user.setId(null);
        return userDao.save(user);
    }

    public void deleteUser(String id) {
        this.userDao.findById(id)
                .ifPresent(this.userDao::delete);
    }

    public void updateUser(String id, User user) {
        this.userDao.findById(id)
                .ifPresent(
                        u -> {
                            u.setName(user.getName());
                            u.setAge(user.getAge());
                            u.setDescription(user.getDescription());
                            this.userDao.save(u);
                        }
                );
    }

    public List<User> getUserByAge(Integer from, Integer to) {
        return this.userDao.findByAgeBetween(from, to);
    }

    public List<User> getUserByName(String name) {
        return this.userDao.findByNameEquals(name);
    }

    public List<User> getUserByDescription(String description) {
        return this.userDao.findByDescriptionIsLike(description);
    }

    public Page<User> getUserByCondition(int size, int page, User user) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        if (!StringUtils.isEmpty(user.getName())) {
            criteria.and("name").is(user.getName());
        }
        if (!StringUtils.isEmpty(user.getDescription())) {
            criteria.and("description").regex(user.getDescription());
        }

        query.addCriteria(criteria);

        Sort sort = new Sort(Sort.Direction.DESC, "age");
        Pageable pageable = PageRequest.of(page, size, sort);

        List<User> users = template.find(query.with(pageable), User.class);
        return PageableExecutionUtils.getPage(users, pageable, () -> template.count(query, User.class));
    }

    public ListIndexesIterable<Document> getIndex(String collectionName) {
        return template.getCollection(collectionName).listIndexes();
    }


    /**
     * ---字段名称: 1是按升序创建索引，-1是按降序创建索引,"unique":true唯一索引,"dropDups":true去重，
     * <p>"sparse":true稀疏索引, db.user.createIndex({"email":1},{"unique":true,"sparse":true})</p>
     * <p>"name":"indexName"指定名字, db.user.createIndex( { "name": 1 },{ "name": "idx_name","unique":true,expireAfterSeconds:300})</p>
     * <p>删除索引：db.user.dropIndex("name")</p>
     * <p>过期：这个field字段要么是date字段，要么是带有date字段的一个array</p>
     * db.user.createIndex( { "name": 1 },{ expireAfterSeconds:300,"unique":true})
     * ex: user_id,name,idx_name
     */
    public boolean createIndex(String collectionName, String fieldName, String indexName, boolean isExpire, Long ttlSeconds) {
        ListIndexesIterable<Document> indexList = getIndex(collectionName);
        //先检查是否存在索引，特殊业务应用，一般不需要这一步
        for (Document document : indexList) {
            Object key = document.get(indexName);
            if (key instanceof Document) {
                Document keyDocument = (Document) key;
                if (keyDocument.containsKey(fieldName)) {
                    log.info("{} exist index [{}] already", fieldName, indexName);
                    return false;
                }
            }
        }

        //该参数为索引的属性配置
        IndexOptions indexOptions = new IndexOptions();
        if (isExpire) {
            indexOptions.expireAfter(ttlSeconds, TimeUnit.SECONDS);
        }
        indexOptions.background(true);
        indexOptions.name(indexName);
        String resultStr = template.getCollection(collectionName)
                // Document key为索引的列名称，value为索引类型，在userId上创建hashed类型索引
                .createIndex(new Document(fieldName, "hashed"), indexOptions);

        log.info("{} add index [{}] result : {}", fieldName, indexName, resultStr);
        return true;
    }
}

