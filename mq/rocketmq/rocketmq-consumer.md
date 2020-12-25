
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

# DefaultMQPushConsumer.start()
如果消息消费模式为集群模式，默认为该消费组订阅重试主题 consumer.start() => copySubscription()  
RocketMQ会把认为消费失败的消息发回Broker，在接下来的某个时间点（默认是10秒，可修改）再次投递给消费者。  
如果一直重复消息都失败的话，当失败累积到一定次数后（默认16次）将消息投递到死信队列（Dead Letter Queue）中，此时需要监控死信队列进行人工干预

# DefaultMQPullConsumer.start()
消息分发策略：new AllocateMessageQueueAveragely();






