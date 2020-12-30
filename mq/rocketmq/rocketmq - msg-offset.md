### ConsumeFromWhere
https://blog.csdn.net/prestigeding/article/details/96576932  
对于一个新的消费组，无论是集群模式还是广播模式都不会存储该消费组的消费进度，可以理解为-1
key | remark
:--- | :---
CONSUME_FROM_LAST_OFFSET |默认策略，从该队列最尾开始消费，即跳过历史消息
CONSUME_FROM_FIRST_OFFSET |从队列最开始开始消费，即设置从最小的 Offset开始读取
CONSUME_FROM_TIMESTAMP |从某个时间点开始消费，和setConsumeTimestamp()配合使用，默认为消费者启动之前的30分钟处开始消费

<pre>
注意设置读取位置不是每次都有效，它的优先级默认在 Offset Store后面 ， 比如在 DefaultMQPushConsumer 的 BROADCASTING 方式下 ，默认是从 Broker 里读取某个 Topic 对应 ConsumerGroup 的 Offset， 当读取不到 Offset 的时候， ConsumeFromWhere 的设置才生效 。 

大部分情况下这个设置在 Consumer Group初次启动时有效。 如果 Consumer正常运行后被停止， 然后再启动， 会 接着上次的 Offset开始消费， ConsumeFromWhere 的设置元效。
</pre>

### 消息偏移量 Offset  
https://www.cnblogs.com/jwen1994/p/12369913.html  

```java
/**
 * Offset store interface
 */
public interface OffsetStore {
    /**
     * Load
     */
    // LocalFileOffsetStore  实现 MixAll.file2String(this.storePath) || this.readLocalOffsetBak()
    // RemoteBrokerOffsetStore 实现为空方法
    void load() throws MQClientException;

    /**
     * Update the offset,store it in memory
     */
    void updateOffset(final MessageQueue mq, final long offset, final boolean increaseOnly);

    /**
     * Get offset from local storage
     * @return The fetched offset
     */
    long readOffset(final MessageQueue mq, final ReadOffsetType type);

    /**
     * Persist all offsets,may be in local storage or remote name server
     */
    void persistAll(final Set<MessageQueue> mqs);

    /**
     * Persist the offset,may be in local storage or remote name server
     */
    void persist(final MessageQueue mq);

    /**
     * Remove offset
     */
    void removeOffset(MessageQueue mq);

    /**
     * @return The cloned offset table of given topic
     */
    Map<MessageQueue, Long> cloneOffsetTable(String topic);

    /**
     * @param mq
     * @param offset
     * @param isOneway
     */
    // LocalFileOffsetStore 的实现为空方法
    // RemoteBrokerOffsetStore 同步offset到Broker
    /**
     * Update the Consumer Offset synchronously, once the Master is off, updated to Slave, here need to be optimized.
     */
    void updateConsumeOffsetToBroker(MessageQueue mq, long offset, boolean isOneway) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException;
}
```

DefaultMQPushConsumer的BROADCASTING模式，各个Consumer没有互相干扰，使用LocalFileOffsetStore把Offset存储在本地 
```java
// package org.apache.rocketmq.client.consumer.store;
public class LocalFileOffsetStore implements OffsetStore {
    public final static String LOCAL_OFFSET_STORE_DIR = System.getProperty(
        "rocketmq.client.localOffsetStoreDir",
        System.getProperty("user.home") + File.separator + ".rocketmq_offsets");
    private final static InternalLogger log = ClientLogger.getLog();
    private final MQClientInstance mQClientFactory;
    private final String groupName;
    private final String storePath;
    private ConcurrentMap<MessageQueue, AtomicLong> offsetTable =
        new ConcurrentHashMap<MessageQueue, AtomicLong>();
    // ...
```

DefaultMQPushConsumer的CLUSTERING模式，由Broker端存储和控制Offset的值，使用RemoteBrokerOffsetStore  
```java
// package org.apache.rocketmq.client.consumer.store;
public class RemoteBrokerOffsetStore implements OffsetStore {
    private final static InternalLogger log = ClientLogger.getLog();
    private final MQClientInstance mQClientFactory;
    private final String groupName;
    private ConcurrentMap<MessageQueue, AtomicLong> offsetTable =
        new ConcurrentHashMap<MessageQueue, AtomicLong>();

    //...
}
```

建议采用pushConsumer，RocketMQ自动维护OffsetStore，如果用另外一种pullConsumer需要自己进行维护OffsetStore  
```java
// this.defaultLitePullConsumerImpl.start()
private void initOffsetStore() throws MQClientException {
        if (this.defaultLitePullConsumer.getOffsetStore() != null) {
            this.offsetStore = this.defaultLitePullConsumer.getOffsetStore();
        } else {
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
            this.defaultLitePullConsumer.setOffsetStore(this.offsetStore);
        }
        this.offsetStore.load();
    }
```

> message queue 是无限长的数组，一条消息进来下标就会涨1，下标就是 offset，消息在某个 MessageQueue 里的位置，通过 offset 的值可以定位到这条消息，或者指示 Consumer 从这条消息开始向后处理。  

> message queue 中的 maxOffset 表示消息的最大 offset，maxOffset 并不是最新的那条消息的 offset，而是最新消息的 offset+1，minOffset 则是现存在的最小 offset。

> fileReserveTime=48 默认消息存储48小时后，消费会被物理地从磁盘删除，message queue 的minOffset 也就对应增长。所以比 minOffset 还要小的那些消息已经不在 broker上了，就无法被消费

