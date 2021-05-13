

### springboot-mongo

```shell
// 使用数据库：dn-test
use dn-test
// 创建集合：user
db.createCollection("user")
// 创建索引：createTime，降序，索引名：exp_time,过期时间
db.user.createIndex( { "createTime": 1 },{ "name": "exp_time",expireAfterSeconds:3600*24})
// 添加数据
db.user.insert({"name": "dean01", "age": 29,"createTime": new Date})
db.user.insert({"name": "dean02", "age": 22,"createTime": new Date})
db.user.insert({"name": "dean03", "age": 10,"createTime": new Date})
db.user.insert({"name": "dean04", "age": 15,"createTime": new Date})
// 查询 name=dean01的数据
db.user.find({"name": "dean01"})
db.user.find().limit(10)
// 查询 age > 10的数据（条件查询）
db.getCollection("user").find(
    { 
        "age" : { 
            "$gt" : NumberLong(10)
        }
    }
);
// 查询 age > 10 and age< 20的数据（多条件查询）
db.getCollection("user").find(
    { 
        "$and" : [
            { 
                "age" : { 
                    "$gt" : NumberLong(10)
                }
            }, 
            { 
                "age" : { 
                    "$lt" : NumberLong(20)
                }
            }
        ]
    }
);
// 获取索引列表
db.user.getIndexes()
```