# Redis - Sentinel 哨兵模式
https://www.cnblogs.com/kevingrace/p/9004460.html  

当用Redis做Master-slave的高可用方案时，假如master宕机了，Redis本身(包括它的很多客户端)都没有实现自动进行主备切换，而Redis-sentinel本身也是一个独立运行的进程，它能监控多个master-slave集群，发现master宕机后能进行自动切换

Sentinel由一个或多个Sentinel 实例组成的Sentinel 系统可以监视任意多个主服务器，以及这些主服务器属下的所有从服务器，并在被监视的主服务器进入下线状态时，自动将下线主服务器属下的某个从服务器升级为新的主服务器

## Sentinel 一致性
Sentinel 自动故障迁移使用 Raft 算法来选举领头（leader）Sentinel ， 从而确保在一个给定的纪元（epoch）里， 只有一个领头产生

## Sentinel状态持久化
snetinel的状态会被持久化地写入sentinel的配置文件中。每次当收到一个新的配置时，或者新创建一个配置时，配置会被持久化到硬盘中，并带上配置的版本戳。这意味着，可以安全的停止和重启sentinel进程

Master-Slave切换后，master_redis.conf、slave_redis.conf和sentinel.conf的内容都会发生改变，即master_redis.conf中会多一行slaveof的配置，sentinel.conf的监控目标会随之调换

## Sentinel 3个内部定时任务
1. 每10秒每个sentinel会对master和slave执行info命令，这个任务达到两个目的：
a）发现slave节点
b）确认主从关系
2. 每2秒每个sentinel通过master节点的channel交换信息（pub/sub）。master节点上有一个发布订阅的频道(__sentinel__:hello)。sentinel节点通过__sentinel__:hello频道进行信息交换(对节点的"看法"和自身的信息)，达成共识。
3. 每1秒每个sentinel对其他sentinel和redis节点执行ping操作（相互监控）

## 主观下线 & 客观下线
1. 主观下线（Subjectively Down， 简称 SDOWN）指的是单个 Sentinel 实例对服务器做出的下线判断。
2. 客观下线（Objectively Down， 简称 ODOWN）指的是多个 Sentinel 实例在对同一个服务器做出 SDOWN 判断， 并且通过SENTINEL is-master-down-by-addr 命令互相交流之后， 得出的服务器下线判断。

系统保证的是单个redis实例的高可用，所以适合业务比较小的应用。如果业务比较大，并发量比较高，建议搭建redis集群
主观下线过程：
```
sentinel通过发送 SENTINEL is-master-down-by-addr ip port current_epoch runid，（ip：主观下线的服务id，port：主观下线的服务端口，current_epoch：sentinel的纪元，runid：*表示检测服务下线状态，如果是sentinel 运行id，表示用来选举领头sentinel）来询问其它sentinel是否同意服务下线。

一个sentinel接收另一个sentinel发来的is-master-down-by-addr后，提取参数，根据ip和端口，检测该服务时候在该sentinel主观下线，并且回复is-master-down-by-addr，回复包含三个参数：down_state（1表示已下线，0表示未下线），leader_runid（领头sentinal id），leader_epoch（领头sentinel纪元）。

sentinel接收到回复后，根据配置设置的下线最小数量，达到这个值，既认为该服务客观下线
```
> 客观下线条件只适用于主服务器： 对于任何其他类型的 Redis 实例， Sentinel 在将它们判断为下线前不需要进行协商， 所以从服务器或者其他 Sentinel 永远不会达到客观下线条件

# Slave 选举与优先级
当一个sentinel准备好了要进行failover，并且收到了其他sentinel的授权，那么就需要选举出一个合适的slave来做为新的master  
slave的选举主要会评估slave的以下几个方面：
1）与master断开连接的次数
2）Slave的优先级
3）数据复制的下标(用来评估slave当前拥有多少master的数据)
4）进程ID

如果一个slave与master失去联系超过10次，并且每次都超过了配置的最大失联时间(down-after-milliseconds)，如果sentinel在进行failover时发现slave失联，那么这个slave就会被sentinel认为不适合用来做新master的。
更严格的定义是，如果一个slave持续断开连接的时间超过
(down-after-milliseconds * 10) + milliseconds_since_master_is_in_SDOWN_state
就会被认为失去选举资格。

符合上述条件的slave才会被列入master候选人列表，并根据以下顺序来进行排序：
1）sentinel首先会根据slaves的优先级来进行排序，优先级越小排名越靠前。
2）如果优先级相同，则查看复制的下标，哪个从master接收的复制数据多，哪个就靠前。
3）如果优先级和下标都相同，就选择进程ID较小的那个。

一个redis无论是master还是slave，都必须在配置中指定一个slave优先级。要注意到master也是有可能通过failover变成slave的。
如果一个redis的slave优先级配置为0，那么它将永远不会被选为master。但是它依然会从master哪里复制数据。