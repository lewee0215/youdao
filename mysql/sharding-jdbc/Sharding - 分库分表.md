# Sharding - JDBC 使用
https://www.cnblogs.com/chengxy-nds/p/13877422.html  
对原有的 DataSource、Connection 等接口扩展成 ShardingDataSource、ShardingConnection，而对外暴露的分片操作接口与 JDBC 规范中所提供的接口完全一致

## 分片算法
https://www.cnblogs.com/chengxy-nds/p/13730973.html  

> 取模算法
按字段取模（对hash结果取余数 (hash() mod N)，N为数据库实例数或子表数量）

> 范围限定算法
按照 时间区间 或 ID区间 来切分，比如：我们切分的是用户表，可以定义每个库的 User 表里只存10000条数据，第一个库只存 userId 从1 ~ 9999的数据，第二个库存 userId 为10000 ~ 20000，第三个库存 userId 为 20001~ 30000......以此类推，按时间范围也是同理

## 分库分表的难点
> 分布式事务

> 分页、排序、跨库联合查询

> 分布式主键

> 读写分离

> 数据脱敏