
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

# DefaultMQPullConsumer.start()
消息分发策略：new AllocateMessageQueueAveragely();






