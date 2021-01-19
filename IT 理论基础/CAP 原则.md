# CAP 定理（CAP theorem）
https://blog.csdn.net/weixin_30439031/article/details/97040778  

In a distributed system (a collection of interconnected nodes that share data.), you can only have two out of the following three guarantees across a write/read pair: Consistency, Availability, and Partition Tolerance - one of them must be sacrificed.  

在一个分布式系统（指互相连接并共享数据的节点的集合）中，当涉及读写操作时，只能保证一致性（Consistence）、可用性（Availability）、分区容错性（Partition Tolerance）三者中的两个，另外一个必须被牺牲

* 一致性（Consistency）: 对某个指定的客户端来说，读操作保证能够返回最新的写操作结果   
https://blog.csdn.net/weixin_30439031/article/details/97040778  
事务在执行过程中，client 是无法读取到未提交的数据的，只有等到事务提交后，client 才能读取到事务写入的数据，而如果事务失败则会进行回滚，client 也不会读取到事务中间写入的数据

* 可用性（Availability）: 每个请求都能得到成功或者失败的响应  
https://blog.csdn.net/qq40988670/article/details/105966202  
当用户访问一个正常工作的节点时，系统保证该节点必须给用户一个响应，可以是正确的响应，也可以是一个老的甚至错误的响应，但是不能没有响应  
从客户端视角来看，发出的请求总有响应，不会出现整个服务集群无法连接、超时、无响应的情况

* 分区容忍性（Partition Tolerance）: 出现消息丢失或者分区错误时系统能够继续运行  

虽然 CAP 理论定义是三个要素中只能取两个，但放到分布式环境下来思考，我们会发现必须选择 P（分区容忍）要素，因为网络本身无法做到 100% 可靠，有可能出故障，所以分区是一个必然的现象。  

如果我们选择了 CA 而放弃了 P，那么当发生分区现象时，为了保证 C，系统需要禁止写入，当有写入请求时，系统返回 error（例如，当前系统不允许写入），这又和 A 冲突了，因为 A 要求返回 no error 和 no timeout。因此，分布式系统理论上不可能选择 CA 架构，只能选择 CP 或者 AP 架构

## AP 架构
选择可用性A(Availability)，此时，那个失去联系的节点依然可以向系统提供服务，不过它的数据就不能保证是同步的了（失去了C属性）。
![alt text](https://images2018.cnblogs.com/blog/352885/201808/352885-20180830093556898-1392086034.png "title")

## CP 架构
选择一致性C(Consistency)，为了保证数据库的一致性，我们必须等待失去联系的节点恢复过来，在这个过程中，那个节点是不允许对外提供服务的，这时候系统处于不可用状态(失去了A属性)
![alt text](https://images2018.cnblogs.com/blog/352885/201808/352885-20180830093611050-529146466.png "title")