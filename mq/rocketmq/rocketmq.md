RocketMQ 基础概念信息
## RokcetMQ 集群消费 & 广播消费
与集群消费不同的是，广播消息 consumer 的消费进度是存储在各个 consumer 实例上
1. 容易造成消息重复
2. 对于广播消费来说，是不会进行消费失败重投的，所以在 consumer 端消费逻辑处理时，需要额外关注消费失败的情况

对于一个ConsumerGroupName来说，可以布置一个消费者实例，也可以布置多个消费者实例  
https://help.aliyun.com/document_detail/43523.html?spm=a2c4g.11186623.6.749.53e84e0eXIri6a  
订阅关系一致指的是同一个消费者 Group ID 下所有 Consumer 实例所订阅的 Topic、Group ID、Tag 必须完全一致。一旦订阅关系不一致，消息消费的逻辑就会混乱，甚至导致消息丢失

### Consumer.start()
如果消息消费模式为集群模式，默认为该消费组订阅重试主题 consumer.start() => copySubscription()

### Consumer.offsetStore
如果是集群模式，使用远程存储 RemoteBrokerOffsetStore  
如果是广播模式，则使用本地存储LocalFileOffsetStore  

###  消息偏移量 Offset  
https://www.cnblogs.com/jwen1994/p/12369913.html  
* message queue 是无限长的数组，一条消息进来下标就会涨1，下标就是 offset，消息在某个 MessageQueue 里的位置，通过 offset 的值可以定位到这条消息，或者指示 Consumer 从这条消息开始向后处理。  
* message queue 中的 maxOffset 表示消息的最大 offset，maxOffset 并不是最新的那条消息的 offset，而是最新消息的 offset+1，minOffset 则是现存在的最小 offset。
* fileReserveTime=48 默认消息存储48小时后，消费会被物理地从磁盘删除，message queue 的 minOffset 也就对应增长。所以比 minOffset 还要小的那些消息已经不在 broker上了，就无法被消费

### ConsumeFromWhere
CONSUME_FROM_LAST_OFFSET 默认策略，从该队列最尾开始消费，即跳过历史消息
CONSUME_FROM_FIRST_OFFSET 从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
CONSUME_FROM_TIMESTAMP 从某个时间点开始消费，和setConsumeTimestamp()配合使用，默认为消费者启动之前的30分钟处开始消费

### 集群消费模拟广播消费
如果业务上确实需要使用广播消费，那么我们可以通过创建多个 consumer 实例，每个 consumer 实例属于不同的 consumer group，但是它们都订阅同一个 topic

## RokcetMQ 顺序消息  
Producer 指定消息存放队列
<code>
<pre> 
new MessageQueueSelector(){
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg){
        return mqs.get(0);  //指定 index = 0 队列
    }
}
</pre>
</code>
消费者注册消息监听器为MessageListenerOrderly

https://www.jianshu.com/p/2838890f3284
注意：把消息发到同一个队列（queue），不是同一个topic，默认情况下一个topic包括4个queue  

## RokcetMQ 事务消息
1. 同步消息无法保证事务性
https://www.jianshu.com/p/d8a21ab2c2d3  
使用RocketMQ发送消息的3种方法：可靠同步发送、可靠异步发送和单向发送

2. 事务消息
Producer向Broker投递一个事务消息，并且带有唯一的key作为参数（幂等性)
Broker预提交消息（在Broker本地做了存储，但是该消息的状态对Consumer不可见）

Broker预提交成功后回调Producer的executeLocalTransaction方法
Broker超时未接受到Producer的反馈，会定时重试调用Producer.checkLocalTransaction，Producer会根据自己的执行情况Ack给Broker

Producer提交业务(比如记录最终成功投递的日志），并根据业务提交的执行情况，向Broker反馈Commit 或者回滚

### 事务消息状态
TransactionStatus.CommitTransaction:   
commit transaction，it means that allow consumers to consume this message.

TransactionStatus.RollbackTransaction:   
rollback transaction，it means that the message will be deleted and not allowed to consume.

TransactionStatus.Unknown:   
intermediate state，it means that MQ is needed to check back to determine the status.

### RocketMQ CommitLog
RocketMQ 解析 - 沐之橙 - 博客园 (cnblogs.com)
CommitLog是用于存储真实的物理消息的结构，保存消息元数据，所有消息到达Broker后都会保存到commitLog文件，这里需要强调的是所有topic的消息都会统一保存在commitLog中
CommitLog.this.topicQueueTable.put(key, queueOffset)，其中的key是 topic-queueId, queueOffset是当前这个key中的消息数，每增加一个消息增加一(不会自减)；

CommitLog的清理机制：
● 按时间清理，rocketmq默认会清理3天前的commitLog文件；
● 按磁盘水位清理：当磁盘使用量到达磁盘容量75%，开始清理最老的commitLog文件。
 ConsumeQueue文件组织
ConsumerQueue相当于CommitLog的索引文件，消费者消费时会先从ConsumerQueue中查找消息的在commitLog中的offset，再去CommitLog中找元数据。
如果某个消息只在CommitLog中有数据，没在ConsumerQueue中， 则消费者无法消费，Rocktet的事务消息就是这个原理
Consumequeue类对应的是每个topic和queuId下面的所有文件，相当于字典的目录用来指定消息在消息的真正的物理文件commitLog上的位置
消息的起始物理偏移量physical offset(long 8字节)+消息大小size(int 4字节)+tagsCode(long 8字节)

● 每个topic下的每个queue都有一个对应的consumequeue文件。
● 文件默认存储路径：${user.home} \store\consumequeue\${topicName}\${queueId}\${fileName}
● 每个文件由30W条数据组成，每条数据的大小为20个字节，从而每个文件的默认大小为600万个字节（consume queue中存储单元是一个20字节定长的数据）是顺序写顺序读

### RocketMQ 高可用
https://www.cnblogs.com/xuwc/p/9043764.html

### 消息堆积和延迟问题
RocketMQ 采用零拷贝原理实现超大的消息的堆积能力  
https://help.aliyun.com/document_detail/193875.html?spm=a2c4g.11186623.6.751.24486145hwrGdZ  
 消息类型   | 消费并发度  
 :---: | :---: 
普通消息    |单节点线程数*节点数量
定时和延时消息  | 同上
事务消息  | 同上
顺序消息    |Min(单节点线程数*节点数量，分区数)

## RocketMQ 功能概览
Apache RocketMQ是一个分布式消息传递和流媒体平台，具有低延迟，高性能和可靠性，万亿级容量和灵活的可扩展性。它提供了多种功能:

发布/订阅消息模型和点对点
预定的消息传递
消息追溯性按时间或偏移量
记录流媒体的中心
大数据集成
可靠的FIFO和严格的有序消息传递在同一队列中
高效的推拉消费模式
单个队列中的百万级消息累积容量
多种消息传递协议，如JMS和OpenMessaging
灵活的分布式横向扩展部署架构
Lightning-fast批处理消息交换系统
各种消息过滤器机制，如SQL和Tag
Docker图像用于隔离测试和云隔离集群
功能丰富的管理仪表板，用于配置，指标和监控
访问控制列表
消息跟踪
上面都是官方列举出来了,还有如下特点:

支持Broker和Consumer端消息过滤
支持拉pull和推push两种消费模式,也就是上面说的推拉消费模式
支持单master节点,多master节点,多master节点多slave节点
消息失败重试机制,支持特定level的定时消息
新版本底层采用Netty

