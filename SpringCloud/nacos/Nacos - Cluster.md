# Nacos Mysql高可用配置
https://vlambda.com/wz_wLcPMODfDE.html

```java
db.num=3  
db.url.0=jdbc:mysql://xxx/nacos_config?useUnicode=true&characterEncoding=utf8&useAffectedRows=true&allowMultiQueries=true  
db.user.0=xx 
db.password.0=*****  

db.url.1=jdbc:mysql://xxx2/nacos_config?useUnicode=true&characterEncoding=utf8&useAffectedRows=true&allowMultiQueries=true
db.user.1=xx
db.password.1=*****  

db.url.2=jdbc:mysql://xxx3/nacos_config?useUnicode=true&characterEncoding=utf8&useAffectedRows=true&allowMultiQueries=true
db.user.2=xx
db.password.2=*****  
```
通过执行一条Delete语句判断数据库是否有写权限，从而判定是主节点，并记录主节点索引
同时init方法最后有TimerTaskService 每隔10S就会检测一次主节点状态，并且设置新的主节点

# Nacos 集群选举

在没有leader 产生之前， 集群会进行多次的选举。 每次的选举 任期会加1  

如果某一个或某几个节点都挂了， 只要剩余节点不少于 1+ 1/N ，那么 集群仍然能够正常运行； 挂掉的节点重新加入集群后，如果此时集群已经有了leader， 那么它的角色一般是follower， 它的任期是0（ 就跟一个新节点一样的）  

当然，如果集群的剩余节点少于 1+ 1/N，集群仍然是可以工作的，只是已经无法保证 高可用

任期低节点一般是没有资格参与选举的，leader 一般是在 任期高的几个节点之中产生（至少会有2个节点）

# Nacos 数据一致性
Nacos同时使用Raft协议和 Distro协议维护数据一致性的

# Nacos Server 运行模式
https://www.cnblogs.com/xuweiweiwoaini/p/13858963.html  
Nacos Server 可以运行在多种模式下，当前支持三种模式：AP、CP和 MIXED

* AP模式
如果不需要存储服务级别的信息且服务实例是通过Nacos Client注册，并能够保持心跳上报，那么就可以选择AP模式。当前主流的服务如Spring Cloud和Dubbo服务，都适用于AP模式，AP模式为了服务的可用性而减弱了一致性，因此AP模式下只支持临时实例。

* CP模式
如果需要在服务级别编辑或者存储配置信息，那么CP是必须，K8s服务和DNS服务则使用于CP模式。CP模式下支持注册服务化实例，此时则是以Raft协议为集群运行模式，该模式下注册实例之前必须先注册服务，如果服务不存在，则会返回错误