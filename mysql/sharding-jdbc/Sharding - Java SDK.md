# Sharding-JDBC 基本配置  
https://cloud.tencent.com/developer/article/1452359  

## Sharding-JDBC 功能
* 分库 & 分表
* 读写分离
* 分布式主键
* 分布式事务

## Sharding-JDBC 适用范围
任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。
基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP等。
支持任意实现JDBC规范的数据库，目前支持MySQL，Oracle，SQLServer和PostgreSQL

## Maven 依赖
```xml
<!-- sharding jdbc 开始-->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-core</artifactId>
    <version>${sharding.version}</version>
</dependency>
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-namespace</artifactId>
    <version>${sharding.version}</version>
</dependency>
 
<!--分布式事务引用依赖-->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-2pc-xa</artifactId>
    <version>${sharding.version}</version>

</dependency>
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-spring</artifactId>
    <version>${sharding.version}</version>
</dependency>
<!-- sharding jdbc 结束-->
```