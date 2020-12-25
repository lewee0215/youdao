# Master-Slave HA
https://blog.csdn.net/prestigeding/article/details/93672079

Broker分为Master与Slave（Slave不可写，但可读，类似于MySQL的主备方式），一个Master可以对应多个Slave，但是一个Slave只能对应一个Master，Master与Slave的对应关系通过指定相同的BrokerName，不同的BrokerId来定义，BrokerId为0表示Master，非0表示Slave。Master也可以部署多个。每个Broker与NameServer集群中的所有节点建立长连接，定时注册Topic信息到所有 NameServer。
> 1）单个master：  
这是一种风险比较大的集群方式，因为一旦Borker重启或宕机期间，将会导致这个服务不可用，因此是不建议线上环境去使用的。

> 2）多个master：  
一个集群全部都是Master，没有Slave。   
　　优点：配置简单，单个Master宕机或者是重启维护对应用没有什么影响的，在磁盘配置为RAID10时，即使机器宕机不可恢复的情况下，消息也不会丢失（异步刷盘丢失少量消息，同步刷盘则是一条都不会丢失），性能最高  
　　缺点：当单个Broker宕机期间，这台机器上未被消费的消息在机器恢复之前不可订阅，消息的实时性会受到影响

> 3）多master多salve异步复制，  
每个Master配置一个Slave,有多对的Master-Slave,HA采用的是异步复制方式，主备有短暂的消息延迟，毫秒级别的（Master收到消息之后立刻向应用返回成功标识，同时向Slave写入消息）。  
　　优点：即使是磁盘损坏了，消息丢失的非常少，且消息实时性不会受到影响，因为Master宕机之后，消费者仍然可以从Slave消费，此过程对应用透明，不需要人工干预，性能同多个Master模式机会一样。  
　　缺点：Master宕机，磁盘损坏的情况下，会丢失少量的消息

> 4）多master多salve同步双写，每个Master配置一个Slave,有多对的Master-Slave，
　　HA采用的是同步双写模式，主备都写成功，才会向应用返回成功。  
　　优点：数据与服务都无单点，Master宕机的情况下，消息无延迟，服务可用性与数据可用性都非常高  
　　缺点：性能比异步复制模式略低，大约低10%左右，发送单个Master的RT会略高，目前主机宕机后，Slave不能自动切换为主机，后续会支持自动切换功能。 

Producer与NameServer集群中的其中一个节点（随机选择）建立长连接，定期从Name Server取Topic路由信息，并向提供Topic服务的Master建立长连接，且定时向Master发送心跳。Producer完全无状态，可集群部署。
Consumer与NameServer集群中的其中一个节点（随机选择）建立长连接，定期从Name Server取Topic路由信息，并向提供Topic服务的Master、Slave建立长连接，且定时向Master、Slave发送心跳。Consumer既可以从Master订阅消息，也可以从Slave订阅消息，订阅规则由Broker配置决定


## master-slave 运行规则
RocketMQ的主从同步，在默认情况下RocketMQ会优先选择从主服务器进行拉取消息  
默认情况下，RocketMQ消息消费者从主服务器拉取，当主服务器积压的消息超过了物理内存的40%，则建议从从服务器拉取。但如果slaveReadEnable为false，表示从服务器不可读，从服务器也不会接管消息拉取


# 消息消费进度同步机制

消息消费进度的同步时单向的，从服务器开启一个定时任务，定时从主服务器同步消息消费进度  

无论消息消费者是从主服务器拉的消息还是从从服务器拉取的消息，在向Broker反馈消息消费进度时，优先向主服务器汇报；消息消费者向主服务器拉取消息时，如果消息消费者内存中存在消息消费进度时，主会尝试跟新消息消费进度


## RocketMQ消息消费进度管理（集群模式）
集群模式下消息消费进度存储文件位于服务端${ROCKETMQ_HOME}/store/config/consumerOffset.json。
消息消费者从服务器拉取一批消息后提交到消费组特定的线程池中处理消息，当消息消费成功后会向Broker发送ACK消息，告知消费端的消费进度，Broker收到消息消费进度反馈后，首先存储在内存中，然后定时持久化到consumeOffset.json文件中  

客户端定时向Broker端发送更新消息消费进度的请求，其入口为：RemoteBrokerOffsetStore#updateConsumeOffsetToBroker

-- 如果主服务器存活，则选择主服务器，如果主服务器宕机，则选择从服务器

如果Broker角色为从服务器，会通过定时任务调用syncAll，从主服务器定时同步topic路由信息、消息消费进度、延迟队列处理进度、消费组订阅信息

### RocketMQ提供了两种机制来确保不丢失消息消费进度

第一种，消息消费者在内存中存在最新的消息消费进度，继续以该进度去服务器拉取消息后，消息处理完后，会定时向Broker服务器反馈消息消费进度，在上面也提到过，在反馈消息消费进度时，会优先选择主服务器，此时主服务器的消息消费进度就立马更新了，从服务器此时只需定时同步主服务器的消息消费进度即可。

第二种是，消息消费者在向主服务器拉取消息时，如果是是主服务器，在处理消息拉取时，也会更新消息消费进度。

