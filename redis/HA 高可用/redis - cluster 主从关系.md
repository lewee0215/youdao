## Redis Cluster 主从关系
https://blog.csdn.net/liuxiao723846/article/details/86715614
集群主节点出现故障，发生故障转移，其他主节点会把故障主节点的从节点自动提为主节点，原来的主节点恢复后，自动成为新主节点的从节点

1. 集群间节点支持主从关系，复制的逻辑基本复用了单机版的实现

2. 集群间节点建立主从关系不再使用原有的SLAVEOF命令和SLAVEOF配置，而是通过cluster replicate命令，这保证了主从节点需要先完成握手，才能建立主从关系

3. 集群是不能组成链式主从关系的，也就是说从节点不能有自己的从节点

4. 集群内节点想要复制另一个节点，需要保证本节点不再负责任何slot，不然redis也是不允许的

5. 集群内的从节点在与其他节点通信的时候，传递的消息中数据分布表和epoch是master的值


### Redis Cluster 主从节点选举
https://blog.csdn.net/liuxiao723846/article/details/86715614  
集群内一个master和它的全部slave描述为一个group，故障转移是以group为单位的，集群故障转移的方式跟sentinel的实现类似

新的主节点由已下线主节点属下的所有从节点中自行选举产生，以下是选举的条件：
* 这个节点是已下线主节点的从节点。
* 已下线主节点负责处理的槽数量非空。
* 从节点的数据被认为是可靠的，也即是，主从节点之间的复制连接（replication link）的断线时长不能超过节点超时时限（node timeout）乘以REDIS_CLUSTER_SLAVE_VALIDITY_MULT 常量得出的积。
如果一个从节点满足了以上的所有条件，那么这个从节点将向集群中的其他主节点发送授权请求，询问它们，是否允许自己（从节点）升级为新的主节点。

选举过程：
https://blog.csdn.net/m0_37609579/article/details/100609618
1. slave发现自己的master变为FAIL
2. 将自己记录的集群currentEpoch加1，并广播FAILOVER_AUTH_REQUEST信息
3. 其他节点收到该信息，只有master响应，判断请求者的合法性，并发送FAILOVER_AUTH_ACK，对每一个epoch只发送一次ack
4. 尝试failover的slave收集FAILOVER_AUTH_ACK
5. 超过半数后变成新Master
6. 广播Pong通知其他集群节点

从节点并不是在主节点一进入 FAIL 状态就马上尝试发起选举，而是有一定延迟，一定的延迟确保我们等待FAIL状态在集群中传播，slave如果立即尝试选举，其它masters或许尚未意识到FAIL状态，可能会拒绝投票
> 延迟计算公式： DELAY = 500ms + random(0 ~ 500ms) + SLAVE_RANK * 1000ms
SLAVE_RANK表示此slave已经从master复制数据的总量的rank。Rank越小代表已复制的数据越新。这种方式下，持有最新数据的slave将会首先发起选举（理论上）