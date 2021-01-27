# Redis Cluster Sharding
http://doc.redisfans.com/topic/cluster-tutorial.html#redis-guarantee  
https://www.cnblogs.com/williamjie/p/11132211.html  

Redis Cluster集群有多个shard组成，每个shard可以有一个master和多个slaves构成，数据根据hash slots配额分布在多个shard节点上

redis-cluster把所有的物理节点映射到[0-16383]slot上,cluster 负责维护node<->slot<->value

### Redis Cluster 哈希槽结构 
https://www.cnblogs.com/kaleidoscope/p/9635163.html  

一个 Redis 集群包含 16384 个哈希槽（hash slot）， 数据库中的每个键都属于这 16384 个哈希槽的其中一个， 
集群使用公式 CRC16(key) % 16384 来计算键 key 属于哪个槽， 其中 CRC16(key) 语句用于计算键 key 的 CRC16 校验和

Redis 集群不像单机 Redis 那样支持多数据库功能， 集群只使用默认的 0 号数据库， 并且不能使用 SELECT index 命令

https://blog.csdn.net/liuxiao723846/article/details/86715614
集群数据以数据分布表的方式保存在各个slot上。集群只有在16384个slot都有对应的节点才能正常工作

很容易添加或者删除节点. 比如如果我想新添加个节点D, 我需要从节点 A, B, C中得部分槽到D上. 如果我像移除节点A,需要将A中得槽移到B和C节点上,然后将没有任何槽的A节点从集群中移除即可. 
由于从一个节点将哈希槽移动到另一个节点并不会停止服务,所以无论添加删除或者改变某个节点的哈希槽的数量都不会造成集群不可用的状态.
http://shift-alt-ctrl.iteye.com/blog/2285470

每一个数据的键被哈希函数映射到一个槽位：HASH_SLOT = CRC16(key) mod 16384。

## Redis Cluster 节点ID：
http://shift-alt-ctrl.iteye.com/blog/2285470  

每个节点在集群中由一个独一无二的ID标识，该ID是一个十六进制表示的160位随机数，在节点第一次启动时由/dev/urandom生成。

节点会将它的ID保存到配置文件，只要这个配置文件不被删除，节点就会一直沿用这个ID。一个节点可以改变它的IP和端口号，而不改变节点ID。
集群可以自动识别出IP/端口号的变化，并将这一信息通过Gossip协议广播给其他节点，节点信心包括：

a）节点所使用的IP地址和TCP端口号。
b）节点的标志（flags）比如表示此node是maste、slave等。
c）节点负责处理的哈希槽。
d）节点最新一次使用集群连接发送PING数据包（packet）的时间。
e）节点最近一次在回复中接收到PONG数据包的时间。
f）集群将该节点标记为下线的时间。
g）该节点的从节点数量。

// https://www.cnblogs.com/kaleidoscope/p/9635163.html
h) currentEpoch和configEpoch

如果该节点是从节点的话，那么它会记录主节点的节点ID。如果这是一个主节点的话，那么主节点ID这一栏的值为0000000
节点组成集群的方式使用cluster meet命令，meet命令可以让两个节点相互握手，然后通过gossip协议交换信息

### gossip协议
节点间状态同步：gossip协议，最终一致性  
https://www.cnblogs.com/kaleidoscope/p/9635163.html

>  gossip：最终一致性，分布式服务数据同步算法，node首选需要知道（可以读取配置）集群中至少一个seed node，此node向seed发送ping请求，此时seed节点pong返回自己已知的所有nodes列表，然后node解析nodes列表并与它们都建立tcp连接，同时也会向每个nodes发送ping，并从它们的pong结果中merge出全局nodes列表，并逐步与所有的nodes建立连接.......数据传输的方式也是类似，网络拓扑结构为full mesh

### Redis Cluster 命令支持
> Redis 集群不支持那些需要同时处理多个键的 Redis 命令， 因为执行这些命令需要在多个 Redis 节点之间移动数据， 并且在高负载的情况下， 这些命令将降低 Redis 集群的性能， 并导致不可预测的行为  

Redis单实例支持的命令，Cluster也都支持，但是对于“multi-key”操作（即一次RPC调用中需要进行多个key的操作）比如Set类型的交集、并集等，则要求这些key必须属于同一个node。

Cluster不能进行跨Nodes操作，也没有nodes提供merge层代理。

Cluster中实现了一个称为“hash tags”的概念，每个key都可以包含一个自定义的“tags”，在存储时将根据tags计算此key应该分布在哪个nodes上（而不是使用key计算，但是存储层面仍然是key）；

但是在Cluster环境下，将不支持SELECT命令，所有的key都将保存在默认的database中。

1. 不支持多key操作
2. 如果一定要使用多key操作，请确保所有的key都在一个node上，具体方法是使用“hash tag”方案
hash tag方案是一种数据分布的例外情况

### hash tags
https://www.cnblogs.com/kaleidoscope/p/9635163.html  
在计算hash slots时有一个意外的情况，用于支持“hash tags”；hash tags用于确保多个keys能够被分配在同一个hash slot中，用于支持multi-key操作。

即“foo”与“{foo}.student”将得到相同的slot值，不过“{foo}.student”仍作为key来保存数据，即redis中数据的key仍为“{foo}.student” 

关于负载均衡，集群的Redis Instance之间可以迁移数据，以Slot为单位，但不是自动的，需要外部命令触发。

需要注意的是：必须要3个或以上的主节点，否则在创建集群时会失败，并且当存活的主节点数小于总节点数的一半时，整个集群就无法提供服务了。

集群由N组主从Redis Instance组成。主可以没有从，但是没有从 意味着主宕机后主负责的Slot读写服务不可用。一个主可以有多个从，主宕机时，某个从会被提升为主，具体哪个从被提升为主，协议类似于Raft

### Redis - Cluster 数据迁移
http://itindex.net/detail/51378-redis-cluster-redis
Redis Cluster支持在线增/减节点。 基于桶的数据分布方式大大降低了迁移成本，只需将数据桶从一个Redis Node迁移到另一个Redis Node即可完成迁移。 
当桶从一个Node A向另一个Node B迁移时，Node A和Node B都会有这个桶，Node A上桶的状态设置为MIGRATING，Node B上桶的状态被设置为IMPORTING 
当客户端请求时： 所有在Node A上的请求都将由A来处理，所有不在A上的key都由Node B来处理。同时，Node A上将不会创建新的key 

http://www.cnblogs.com/zhoujinyi/p/6477133.html
port 7000
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
**  cluster-conf-file 选项则设定了保存节点配置文件的路径， 默认值为 nodes.conf.节点配置文件无须人为修改，它由 Redis 集群在启动时创建，在需要时自动进行更新。

### Redis - Cluster 集群一致性
https://blog.csdn.net/u011535541/article/details/78834565
主从和slot的一致性是由epoch来管理的. epoch就像Raft中的term, 但仅仅是像. 每个节点有一个自己独特的epoch和整个集群的epoch, 为简化下面都称为node epoch和cluster epoch. 
node epoch一直递增, 其表示某节点最后一次变成主节点或获取新slot所有权的逻辑时间. 
cluster epoch则是整个集群中最大的那个node epoch. 我们称递增node epoch为bump epoch, 它会用当前的cluster epoch加一来更新自己的node epoch.

### Redis - Cluster 失效检测
Redis集群失效检测是用来识别出大多数节点何时无法访问某一个主节点或从节点。
每个节点都有一份跟其他已知节点相关的标识列表。其中有两个标志是用于失效检测，分别是PFAIL和FAIL。PFAIL表示可能失效，这是一个非公认的失效类型。FAIL表示一个节点已经失效，而这个情况已经被大多数节点在，某段时间内确认过了。
PFAIL标识：
当一个节点在超过NODE_TIMEOUT时间后仍然无法访问某个节点（发送一个ping包已经等待了超过NODE_TIMEOUT时间，若是经过一半NODE_TIMEOUT时间还没收到回复，尝试重新连接），那么它会用PFAIL来标识这个不可达的节点。无论节点类型是什么，主节点和从节点都能标识其他节点为PFAIL。
FAIL标识：
单独一个PFAIL标识只是每个节点的一些关于其他节点的本地信息，它不是为了起作用而使用的，也不足够触发从节点的提升。要让一个节点被认为失效了，那需要让PFAIL上升为FAIL状态。前面提到过节点之间通过gossip消息来交互随机的已知节点的状态信息。最中每个节点都能收到一份其他每个节点标识。当下面条件满足时，PFAIL状态升级为FAIL：
某个节点A，标记另一个节点B为PFAIL。
节点A通过gossip字段收集到的集群中大部分主节点标识的B的状态信息。
大部分主节点标记B为PFAIL状态，或者在NODE_TIMEOUT*FAIL_REPORT_VALIDITY_MULT这个时间内是处于PFAIL状态。
如果以上条件都满足了，那么节点A会：
标记节点B为FAIL。
向所有节点发送一个FAIL消息。
FAIL消息会强制每个接收到这消息的节点把节点B标记为FAIL状态。
FAIL标识基本是单向的，一个节点能从PFAIL升级到FAIL状态，但要清除FAIL标识只有以下两种可能方法：
节点已经恢复可达，并且它是一个从节点。在这种情况下，FAIL标识可以清除掉，因为从节点并没有被故障转移。
节点已经恢复可达，而且他是一个主节点，但经过了很长时间（N*NODE_TIMEOUT）后也没有检查到任何从节点被提升了。
PFAIL->FAIL的转变使用一种弱协议（agreement）：
1）节点是在一段时间内收集其他几点信息，所以即使大多数主节点要去“同意”标记某节点为FAIL，实际上这只是表明说我们在不同时间里从不同节点收集了信息，得出当前的状态不一定是稳定的。
2）当每个节点检测到FAIL节点的时候会强制集群里的其他节点把各自对该节点的记录更新为FAIL，但没有一种方式能保证这个消息能到达所有节点。
然而Redis集群失效检测有一个要求：最终所有节点都应该同意给定节点的状态是FAIL，或者小部分节点相信该节点处于FAIL状态，或者相信节点不处于FAIL状态。在这两种情况中，最后集群都会认为给定的节点只有一个状态：
第一种情况：如果大多数节点都标记了某节点为FAIL，由于链条反应，这个主节点最终会被标记为FAIL。
第二种情况：当只有小部分的主节点标记某个节点为FAIL的时候，从节点的提升并不会发生，并且每个节点都会根据上面的清除规则（在经过了一段时间>N*NODE_TIMEOUT后仍然没有从节点提升，使用一个更正式的算法来保证每个节点最终都会知道节点的提升）清除FAIL状态。
本质上来说，FAIL标识只是用来触发从节点提升算法的安全部分。理论上一个从节点会在它的主节点不可达的时候独立起作用并且启动从节点提升程序，然后等待主节点来拒绝认可该提升。PFAIL-FAIL的状态变化、弱协议、强制在集群的可达部分用最短时间传播状态变更的FAIL消息，这些东西增加的复杂性有实际的好处。由于这种机制，如果集群处于错误状态时，所有节点都会在同一时间停止接收写入操作，者从使用redis集群的应用角度来看是个很好的特性。还有非必要的选举，是从节点在无法访问主节点时发起，若该节点能被其他大多数主节点访问的话，这个选举会被拒绝掉

