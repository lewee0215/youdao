# 分库分表平滑扩容
https://www.cnblogs.com/barrywxx/p/11532122.html  

## 分片策略
https://blog.csdn.net/kefengwang/article/details/81213050  

### 3.1 连续分片
根据特定字段(比如用户ID、订单时间)的范围，值在该区间的，划分到特定节点。
优点：集群扩容后，指定新的范围落在新节点即可，无需进行数据迁移。
缺点：如果按时间划分，数据热点分布不均(历史数冷当前数据热)，导致节点负荷不均。

### 3.2 ID取模分片
缺点：扩容后需要迁移数据。

### 3.3 一致性Hash算法
优点：扩容后无需迁移数据。

### 3.4 Snowflake 分片
优点：扩容后无需迁移数据

## 历史数据归档

## 停服迁移

## 升级从库
1. 同步配置，解除主从关系,从库升级为主库
2. 修改Sharding 路由规则
3. 清理冗余数据
4. 为新的数据节点搭建新的从库

## 双写迁移
