
# RocketMQ.Consumer
如果消息消费模式为集群模式，默认为该消费组订阅重试主题 consumer.start() => copySubscription()
```java
// DefaultMQPullConsumer.start().initOffsetStore()
switch (this.defaultLitePullConsumer.getMessageModel()) {
    case BROADCASTING:
        this.offsetStore = new LocalFileOffsetStore(this.mQClientFactory, this.defaultLitePullConsumer.getConsumerGroup());
        break;
    case CLUSTERING:
        this.offsetStore = new RemoteBrokerOffsetStore(this.mQClientFactory, this.defaultLitePullConsumer.getConsumerGroup());
        break;
    default:
        break;
}
```
如果是集群模式，使用远程存储 RemoteBrokerOffsetStore implements OffsetStore  
如果是广播模式，则使用本地存储 LocalFileOffsetStore implements OffsetStore 

this.offsetStore.load();

# DefaultMQPullConsumer.start()
消息分发策略：new AllocateMessageQueueAveragely();

## RokcetMQ 集群消费 & 广播消费
与集群消费不同的是，广播消息 consumer 的消费进度是存储在各个 consumer 实例上
1. 容易造成消息重复
2. 对于广播消费来说，是不会进行消费失败重投的，所以在 consumer 端消费逻辑处理时，需要额外关注消费失败的情况

对于一个ConsumerGroupName来说，可以布置一个消费者实例，也可以布置多个消费者实例  
https://help.aliyun.com/document_detail/43523.html?spm=a2c4g.11186623.6.749.53e84e0eXIri6a  
订阅关系一致指的是同一个消费者 Group ID 下所有 Consumer 实例所订阅的 Topic、Group ID、Tag 必须完全一致。一旦订阅关系不一致，消息消费的逻辑就会混乱，甚至导致消息丢失  

### 消息偏移量 Offset  
https://www.cnblogs.com/jwen1994/p/12369913.html  
* message queue 是无限长的数组，一条消息进来下标就会涨1，下标就是 offset，消息在某个 MessageQueue 里的位置，通过 offset 的值可以定位到这条消息，或者指示 Consumer 从这条消息开始向后处理。  
* message queue 中的 maxOffset 表示消息的最大 offset，maxOffset 并不是最新的那条消息的 offset，而是最新消息的 offset+1，minOffset 则是现存在的最小 offset。
* fileReserveTime=48 默认消息存储48小时后，消费会被物理地从磁盘删除，message queue 的 minOffset 也就对应增长。所以比 minOffset 还要小的那些消息已经不在 broker上了，就无法被消费

### ConsumeFromWhere
https://blog.csdn.net/prestigeding/article/details/96576932  
对于一个新的消费组，无论是集群模式还是广播模式都不会存储该消费组的消费进度，可以理解为-1
key | remark
:--- | :---
CONSUME_FROM_LAST_OFFSET |默认策略，从该队列最尾开始消费，即跳过历史消息
CONSUME_FROM_FIRST_OFFSET |从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
CONSUME_FROM_TIMESTAMP |从某个时间点开始消费，和setConsumeTimestamp()配合使用，默认为消费者启动之前的30分钟处开始消费

### 集群消费模拟广播消费
如果业务上确实需要使用广播消费，那么我们可以通过创建多个 consumer 实例，每个 consumer 实例属于不同的 consumer group，但是它们都订阅同一个 topic

### 消息堆积和延迟问题
RocketMQ 采用零拷贝原理实现超大的消息的堆积能力  
https://help.aliyun.com/document_detail/193875.html?spm=a2c4g.11186623.6.751.24486145hwrGdZ  
<br/>
|消息类型       | 消费并发度   
:---           | :---
普通消息        | 单节点线程数*节点数量  
定时和延时消息  | 同上  
事务消息        | 同上  
顺序消息        |Min(单节点线程数*节点数量，分区数)  






