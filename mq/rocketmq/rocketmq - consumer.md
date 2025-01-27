# RocketMQ 消息订阅
消息拉取命令Code : RequestCode.PULL_MESSAGE  
同一消费者也可以同时订阅同一个Topic的多个Tag，多个Tag之间通过||进行分隔

## 消费者核心拉取方法： PullAPIWrapper.pullKernelImpl
```java
// 计算数据来源的 Broker
FindBrokerResult findBrokerResult =
            this.mQClientFactory.findBrokerAddressInSubscribe(mq.getBrokerName(),
                this.recalculatePullFromWhichNode(mq), false);

// private ConcurrentMap<MessageQueue, AtomicLong/* brokerId */> pullFromWhichNodeTable =
//         new ConcurrentHashMap<MessageQueue, AtomicLong>(32);
public long recalculatePullFromWhichNode(final MessageQueue mq) {
    if (this.isConnectBrokerByUser()) {
        return this.defaultBrokerId;
    }

    // 建议值
    AtomicLong suggest = this.pullFromWhichNodeTable.get(mq);
    if (suggest != null) {
        return suggest.get();
    }

    return MixAll.MASTER_ID;
}

// 优先选取 brokerName 对应的主节点，如果不存在则从从节点拉取
// private final ConcurrentMap<String/* Broker Name */, HashMap<Long/* brokerId */, String/* address */>> brokerAddrTable =
//         new ConcurrentHashMap<String, HashMap<Long, String>>();
public FindBrokerResult findBrokerAddressInSubscribe(final String brokerName,final long brokerId,final boolean onlyThisBroker) {
    String brokerAddr = null;
    boolean slave = false;
    boolean found = false;

    HashMap<Long/* brokerId */, String/* address */> map = this.brokerAddrTable.get(brokerName);
    if (map != null && !map.isEmpty()) {
        brokerAddr = map.get(brokerId);
        slave = brokerId != MixAll.MASTER_ID;
        found = brokerAddr != null;

        // 如果主节点掉线
        if (!found && !onlyThisBroker) {
            Entry<Long, String> entry = map.entrySet().iterator().next();
            brokerAddr = entry.getValue();
            slave = entry.getKey() != MixAll.MASTER_ID;
            found = true;
        }
    }
    if (found) {
        return new FindBrokerResult(brokerAddr, slave, findBrokerVersion(brokerName, brokerAddr));
    }
    return null;
}
```

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

# DefaultLitePullConsumer.start()
消息拉取核心类： PullMessageService
消息分发策略：new AllocateMessageQueueAveragely();

https://blog.csdn.net/qq_21383435/article/details/101113808
pull方式里，取消息的过程需要用户自己写，首先通过打算消费的Topic拿到MessageQueue的集合，遍历MessageQueue集合，然后针对每个MessageQueue批量取消息，一次取完后，记录该队列下一次要取的开始offset，直到取完了，再换另一个MessageQueue

```java
// PullMessageService.run()
public void run() {
    log.info(this.getServiceName() + " service started");

    while (!this.isStopped()) {
        // private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();
        try {
            PullRequest pullRequest = this.pullRequestQueue.take();
            this.pullMessage(pullRequest);
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            log.error("Pull Message Service Run Method exception", e);
        }
    }

    log.info(this.getServiceName() + " service end");
}
```

## DefaultLitePullConsumer # poll() // 拉取消息的方法
```java
// private final BlockingQueue<ConsumeRequest> consumeRequestCache = new LinkedBlockingQueue<ConsumeRequest>();
ConsumeRequest consumeRequest = consumeRequestCache.poll(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

if (endTime - System.currentTimeMillis() > 0) {
    while (consumeRequest != null && consumeRequest.getProcessQueue().isDropped()) {
        consumeRequest = consumeRequestCache.poll(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        if (endTime - System.currentTimeMillis() <= 0)
            break;
    }
}

if (consumeRequest != null && !consumeRequest.getProcessQueue().isDropped()) {
    List<MessageExt> messages = consumeRequest.getMessageExts();
    long offset = consumeRequest.getProcessQueue().removeMessage(messages);

    // private final ConcurrentHashMap<MessageQueue, MessageQueueState> assignedMessageQueueState;
    assignedMessageQueue.updateConsumeOffset(consumeRequest.getMessageQueue(), offset);

    //If namespace not null , reset Topic without namespace.
    this.resetTopic(messages);
    return messages;
}
```

## DefaultLitePullConsumer # commitSync() // 同步消息进度
```java
public synchronized void commitSync() {
    try {
        for (MessageQueue messageQueue : assignedMessageQueue.messageQueues()) {
            long consumerOffset = assignedMessageQueue.getConusmerOffset(messageQueue);
            if (consumerOffset != -1) {
                ProcessQueue processQueue = assignedMessageQueue.getProcessQueue(messageQueue);
                long preConsumerOffset = this.getOffsetStore().readOffset(messageQueue, ReadOffsetType.READ_FROM_MEMORY);
                if (processQueue != null && !processQueue.isDropped() && consumerOffset != preConsumerOffset) {
                    updateConsumeOffset(messageQueue, consumerOffset);
                    updateConsumeOffsetToBroker(messageQueue, consumerOffset, false);
                }
            }
        }
        if (defaultLitePullConsumer.getMessageModel() == MessageModel.BROADCASTING) {
            offsetStore.persistAll(assignedMessageQueue.messageQueues());
        }
    } catch (Exception e) {
        log.error("An error occurred when update consume offset synchronously.", e);
    }
}
```

## RokcetMQ 集群消费 & 广播消费
与集群消费不同的是，广播消息 consumer 的消费进度是存储在各个 consumer 实例上
1. 容易造成消息重复
2. 对于广播消费来说，是不会进行消费失败重投的，所以在 consumer 端消费逻辑处理时，需要额外关注消费失败的情况

对于一个ConsumerGroupName来说，可以布置一个消费者实例，也可以布置多个消费者实例  
https://help.aliyun.com/document_detail/43523.html?spm=a2c4g.11186623.6.749.53e84e0eXIri6a  
订阅关系一致指的是同一个消费者 Group ID 下所有 Consumer 实例所订阅的 Topic、Group ID、Tag 必须完全一致。一旦订阅关系不一致，消息消费的逻辑就会混乱，甚至导致消息丢失  

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









