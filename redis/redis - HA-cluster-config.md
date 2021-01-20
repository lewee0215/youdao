## Redis-Cluster 集群配置
```yml
# 集群开关，默认是不开启集群模式。
cluster-enabled yes

# 集群配置文件的名称，每个节点都有一个集群相关的配置文件，持久化保存集群的信息。这个文件并不需要手动配置，这个配置文件有Redis生成并更新，每个Redis集群节点需要一个单独的配置文件，请确保与实例运行的系统中配置文件名称不冲突
cluster-config-file nodes-7021.conf

# 节点互连超时的阀值。集群节点超时毫秒数
cluster-node-timeout 30000

# 在进行故障转移的时候，全部slave都会请求申请为master，但是有些slave可能与master断开连接一段时间了，导致数据过于陈旧，这样的slave不应该被提升>为master。该参数就是用来判断slave节点与master断线的时间是否过长。判断方法是：
# 比较slave断开连接的时间和(node-timeout * slave-validity-factor) + repl-ping-slave-period
# 如果节点超时时间为三十秒, 并且slave-validity-factor为10,假设默认的repl-ping-slave-period是10秒，即如果超过310秒slave将不会尝试进行故障转移
# 可能出现由于某主节点失联却没有从节点能顶上的情况，从而导致集群不能正常工作，在这种情况下，只有等到原来的主节点重新回归到集群，集群才恢复运作
# 如果设置成０，则无论从节点与主节点失联多久，从节点都会尝试升级成主节
cluster-slave-validity-factor 10

# master的slave数量大于该值，slave才能迁移到其他孤立master上，如这个参数若被设为2，那么只有当一个主节点拥有2 个可工作的从节点时，它的一个从节>点会尝试迁移。
# 主节点需要的最小从节点数，只有达到这个数，主节点失败时，它从节点才会进行迁移。
# cluster-migration-barrier 1

# 默认情况下，集群全部的slot有节点分配，集群状态才为ok，才能提供服务。设置为no，可以在slot没有全部分配的时候提供服务。不建议打开该配置，这样会
# 造成分区的时候，小分区的master一直在接受写请求，而造成很长时间数据不一致。
# 在部分key所在的节点不可用时，如果此参数设置为”yes”(默认值), 则整个集群停止接受操作；如果此参数设置为”no”，则集群依然为可达节点上的key提供读>操作
cluster-require-full-coverage yes
```