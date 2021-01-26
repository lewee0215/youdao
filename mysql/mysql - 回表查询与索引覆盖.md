# Mysql中的回表查询与索引覆盖
https://www.cnblogs.com/yanggb/p/11252966.html  
InnoDB有两大类索引，一类是聚集索引（Clustered Index），一类是普通索引（Secondary Index）

## InnoDB 回表查询

InnoDB <font color='yellow'>聚集索引的叶子节点存储行记录</font> 聚集索引的叶子节点存储行记录，因此InnoDB必须要有且只有一个聚集索引。

1. 如果表定义了PK（Primary Key，主键），那么PK就是聚集索引。  
2. 如果表没有定义PK，则第一个NOT NULL UNIQUE的列就是聚集索引。  
3. 否则InnoDB会另外创建一个隐藏的ROWID作为聚集索引  

![alt text](https://img2018.cnblogs.com/blog/842514/201907/842514-20190726210725867-1130495045.png "title")

InnoDB <font color='yellow'>普通索引的叶子节点存储主键值</font> （MyISAM则是存储的行记录头指针）
普通索引因为无法直接定位行记录，其查询过程在通常情况下是需要扫描两遍索引树的

## InnoDB 索引覆盖
如果一个索引覆盖（包含）了所有需要查询的字段的值，这个索引就是覆盖索引。

因为索引中已经包含了要查询的字段的值，因此查询的时候直接返回索引中的字段值就可以了，不需要再到表中查询，避免了对主键索引的二次查询，也就提高了查询的效率。

要注意的是，不是所有类型的索引都可以成为覆盖索引的。因为覆盖索引必须要存储索引的列值，而哈希索引、空间索引和全文索引等都不存储索引列值，索引MySQL只能使用B-Tree索引做覆盖索引

