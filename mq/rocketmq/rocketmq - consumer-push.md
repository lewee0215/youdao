# DefaultMQPushConsumer.start()
https://blog.csdn.net/prestigeding/article/details/78885420  
如果消息消费模式为集群模式，默认为该消费组订阅重试主题 consumer.start() => copySubscription()  
RocketMQ会把认为消费失败的消息发回Broker，在接下来的某个时间点（默认是10秒，可修改）再次投递给消费者。  
如果一直重复消息都失败的话，当失败累积到一定次数后（默认16次）将消息投递到死信队列（Dead Letter Queue）中，此时需要监控死信队列进行人工干预


## start() 启动流程详解
https://blog.csdn.net/prestigeding/article/details/78885420  

1） 构建 RebalanceImpl 
```java  
this.mQClientFactory = MQClientManager.getInstance().getOrCreateMQClientInstance(this.defaultMQPushConsumer, this.rpcHook);

this.rebalanceImpl.setConsumerGroup(this.defaultMQPushConsumer.getConsumerGroup());
this.rebalanceImpl.setMessageModel(this.defaultMQPushConsumer.getMessageModel());
this.rebalanceImpl.setAllocateMessageQueueStrategy(this.defaultMQPushConsumer.getAllocateMessageQueueStrategy());
this.rebalanceImpl.setmQClientFactory(this.mQClientFactory);
```

2）PullAPIWrapper 对象构建
```java
this.pullAPIWrapper = new PullAPIWrapper(
    mQClientFactory,
    this.defaultMQPushConsumer.getConsumerGroup(), isUnitMode());
this.pullAPIWrapper.registerFilterMessageHook(filterMessageHookList);
```

3）消费进度加载
```java
if (this.defaultMQPushConsumer.getOffsetStore() != null) {
    this.offsetStore = this.defaultMQPushConsumer.getOffsetStore();
} else {
    switch (this.defaultMQPushConsumer.getMessageModel()) {
        case BROADCASTING:
            this.offsetStore = new LocalFileOffsetStore(this.mQClientFactory, this.defaultMQPushConsumer.getConsumerGroup());
            break;
        case CLUSTERING:
            this.offsetStore = new RemoteBrokerOffsetStore(this.mQClientFactory, this.defaultMQPushConsumer.getConsumerGroup());
            break;
        default:
            break;
    }
    this.defaultMQPushConsumer.setOffsetStore(this.offsetStore);
}
this.offsetStore.load();
```

4）消费管理ConsumeMessageService
```java
if (this.getMessageListenerInner() instanceof MessageListenerOrderly) {
    this.consumeOrderly = true;
    this.consumeMessageService =
        new ConsumeMessageOrderlyService(this, (MessageListenerOrderly) this.getMessageListenerInner());
} else if (this.getMessageListenerInner() instanceof MessageListenerConcurrently) {
    this.consumeOrderly = false;
    this.consumeMessageService =
        new ConsumeMessageConcurrentlyService(this, (MessageListenerConcurrently) this.getMessageListenerInner());
}

this.consumeMessageService.start();
```

5）MQClientInstance 启动，进入消息消费
```java
boolean registerOK = mQClientFactory.registerConsumer(this.defaultMQPushConsumer.getConsumerGroup(), this);
if (!registerOK) {
    this.serviceState = ServiceState.CREATE_JUST;
    this.consumeMessageService.shutdown();
    throw new MQClientException("The consumer group[" + this.defaultMQPushConsumer.getConsumerGroup()
        + "] has been created before, specify another name please." + FAQUrl.suggestTodo(FAQUrl.GROUP_NAME_DUPLICATE_URL),
        null);
}

mQClientFactory.start();
log.info("the consumer [{}] start OK.", this.defaultMQPushConsumer.getConsumerGroup());
this.serviceState = ServiceState.RUNNING;
break;
```

## Consumer 定时任务
1. 每隔2分钟尝试获取一次NameServer地址
2. 每隔30S尝试更新主题路由信息
3. 每隔30S 进行Broker心跳检测
4. 默认每隔5秒持久化ConsumeOffset
5. 默认每隔1S检查线程池适配


