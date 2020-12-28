## 消息ID 详解
https://blog.csdn.net/prestigeding/article/details/104739950
从消息发送的结果可以得知，RocketMQ 发送的返回结果会返回msgId 与 offsetMsgId
> msgId：该ID 是消息发送者在消息发送时会首先在客户端生成，全局唯一，在 RocketMQ 中该 ID 还有另外的一个叫法：uniqId，无不体现其全局唯一性。  
> offsetMsgId：消息偏移ID，该 ID 记录了消息所在集群的物理地址，主要包含所存储 Broker 服务器的地址( IP 与端口号)以及所在commitlog 文件的物理偏移量

### uniqId 生成规则
msgId 的前半段FIX_STRING 的主要由：客户端的IP、进程ID、加载 MessageClientIDSetter 的类加载器的 hashcode
msgId 的后半段主要由：当前时间与系统启动时间的差值，以及自增序号

```java
private static byte[] createUniqIDBuffer() {
    ByteBuffer buffer = ByteBuffer.allocate(4 + 2);
    long current = System.currentTimeMillis();
    if (current >= nextStartTime) {
        setStartTime(current);
    }
    buffer.position(0);
    buffer.putInt((int) (System.currentTimeMillis() - startTime));
    buffer.putShort((short) COUNTER.getAndIncrement());
    return buffer.array();
}
```
### offsetMsgId 生成规则
在消息 Broker 服务端将消息追加到内存后会返回其物理偏移量，即在 commitlog 文件中的文件，然后会再次生成一个id，代码中虽然也叫 msgId，其实这里就是我们常说的 offsetMsgId，即记录了消息的物理偏移量
```java
public static String createMessageId(final ByteBuffer input ,final ByteBuffer addr, final long offset) {
	input.flip();
    int msgIDLength = addr.limit() == 8 ? 16 : 28;
    input.limit(msgIDLength);
    input.put(addr);
    input.putLong(offset);
    return UtilAll.bytes2string(input.array());
}
```
* ByteBuffer input
用来存放 offsetMsgId 的字节缓存区( NIO 相关的基础知识)
* ByteBuffer addr
当前 Broker 服务器的 IP 地址与端口号，即通过解析 offsetMsgId 从而得到消息服务器的地址信息。
* long offset
消息的物理偏移量。
即构成 offsetMsgId 的组成部分：Broker 服务器的 IP 与端口号、消息的物理偏移量

> 温馨提示：如果消息消费失败需要重试，RocketMQ 的做法是将消息重新发送到 Broker 服务器，此时全局 msgId 是不会发送变化的，但该消息的 offsetMsgId 会发送变化，因为其存储在服务器中的位置发生了变化    
<br/>

## 消息类型  
a)同步消息
b)异步消息
c)单向消息
d)顺序消息
e)批量消息
f)过滤消息
g)事务消息

### RocketMQ 定时&延迟消息
Rocket 默认定义了18个级别的延时消息，暂不支持指定时间戳的定时机制  
消息队列RocketMQ的阿里云版本（收费版本）才支持到精确到秒级别的延迟消息（没有特定Level的限制）

## RocketMQ 延时队列实现
https://www.cnblogs.com/yizhou35/p/12026078.html

### 消息写入中：
在写入CommitLog之前，如果是延迟消息，替换掉消息的Topic和queueId(被替换为延迟消息特定的Topic，queueId则为延迟级别对应的id)
消息写入CommitLog之后，提交dispatchRequest到DispatchService
因为在第①步中Topic和QueueId被替换了，所以写入的ConsumeQueue实际上非真正消息应该所属的ConsumeQueue，而是写入到ScheduledConsumeQueue中（这个特定的Queue存放不会被消费）

```java
// Delay Delivery
if (msg.getDelayTimeLevel() > 0) {
    if (msg.getDelayTimeLevel() > this.defaultMessageStore.getScheduleMessageService().getMaxDelayLevel()) {
        msg.setDelayTimeLevel(this.defaultMessageStore.getScheduleMessageService().getMaxDelayLevel());
    }
    // public static final String SCHEDULE_TOPIC = "SCHEDULE_TOPIC_XXXX";
    topic = ScheduleMessageService.SCHEDULE_TOPIC; 
    queueId = ScheduleMessageService.delayLevel2QueueId(msg.getDelayTimeLevel());

    // Backup real topic, queueId
    // public static final String PROPERTY_REAL_TOPIC = "REAL_TOPIC";
    MessageAccessor.putProperty(msg, MessageConst.PROPERTY_REAL_TOPIC, msg.getTopic()); 
    // public static final String PROPERTY_REAL_QUEUE_ID = "REAL_QID";
    MessageAccessor.putProperty(msg, MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(msg.getQueueId())); 

    msg.setPropertiesString(MessageDecoder.messageProperties2String(msg.getProperties()));

    msg.setTopic(topic);
    msg.setQueueId(queueId);
}
```

###  Schedule过程中：
给每个Level设置定时器，从ScheduledConsumeQueue中读取信息
如果ScheduledConsumeQueue中的元素已近到时，那么从CommitLog中读取消息内容，恢复成正常的消息内容写入CommitLog
写入CommitLog后提交dispatchRequest给DispatchService
因为在写入CommitLog前已经恢复了Topic等属性，所以此时DispatchService会将消息投递到正确的ConsumeQueue中

#### shedule 初始化
https://www.cnblogs.com/heihaozi/p/13259125.html  
遍历所有延迟级别，根据延迟级别获得对应队列的偏移量，如果偏移量不存在，则设置为0。然后为每个延迟级别创建定时任务，第一次启动任务延迟为1秒，第二次及以后的启动任务延迟才是延迟级别相应的延迟时间。  

创建了一个定时任务，用于持久化每个队列消费的偏移量。持久化的频率由flushDelayOffsetInterval属性进行配置，默认为10秒
```java
// 遍历所有延迟级别
for (Map.Entry<Integer, Long> entry : this.delayLevelTable.entrySet()) {
    // key为延迟级别
    Integer level = entry.getKey();
    // value为延迟级别对应的毫秒数
    Long timeDelay = entry.getValue();
    // 根据延迟级别获得对应队列的偏移量
    Long offset = this.offsetTable.get(level);
    // 如果偏移量为null，则设置为0
    if (null == offset) {
        offset = 0L;
    }

    if (timeDelay != null) {
        // 为每个延迟级别创建定时任务，
        // 第一次启动任务延迟为FIRST_DELAY_TIME，也就是1秒
        this.timer.schedule(
                new DeliverDelayedMessageTimerTask(level, offset), FIRST_DELAY_TIME);
    }
}

// 延迟10秒后每隔flushDelayOffsetInterval执行一次任务，
// 其中，flushDelayOffsetInterval默认配置也为10秒
this.timer.scheduleAtFixedRate(new TimerTask() {

    @Override
    public void run() {
        try {
            // 持久化每个队列消费的偏移量
            if (started.get()) ScheduleMessageService.this.persist();
        } catch (Throwable e) {
            log.error("scheduleAtFixedRate flush exception", e);
        }
    }
}, 10000, this.defaultMessageStore
    .getMessageStoreConfig().getFlushDelayOffsetInterval());
```
#### 消息重发
DeliverDelayedMessageTimerTask.executeOnTimeup()

## RokcetMQ 顺序消息  
```java 
// Producer 指定消息存放队列
new MessageQueueSelector(){
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg){
        return mqs.get(0);  //指定 index = 0 队列
    }
}
```
https://www.jianshu.com/p/2838890f3284
注意：把消息发到同一个队列（queue），不是同一个topic，默认情况下一个topic包括4个queue 

Consumer 需要使用 MessageListenerOrderly，它将会定时的向 Broker 申请锁住某些特定的队列，
Broker 的RebalanceLockManager 里的 ConcurrentHashMap mqLockTable 记录着队列与 consumer client 的对应关系

consumer，除了知道自己持有哪些队列的锁，可以对这些队列进行消费外，还需要保证同一时间只有一个线程会消费同一个队列，
所以在本地维护了一个变量，其类型为：MessageQueueLock

对于每一个队列，都有一个 objLock，在消费时对该 objLock 使用 synchronizd 加锁，保证同一时间只有一个线程在消费该队列。 

<code>
正常情况可以保证完全的顺序消息,但如果发生类似Broker宕机等异常，导致队列总数变化，Hash取模后的值会发生改变，然后产生短暂的消息顺序不一致的情况
</code>  
<br/>

## RokcetMQ 事务消息
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

### 事务消息实现原理
https://blog.csdn.net/hosaos/article/details/90240260

#### Producer 发送事务消息
sendMessageInTransaction()
```java
// ignore DelayTimeLevel parameter
if (msg.getDelayTimeLevel() != 0) {
    MessageAccessor.clearProperty(msg, MessageConst.PROPERTY_DELAY_TIME_LEVEL);
}

MessageAccessor.putProperty(msg, MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
MessageAccessor.putProperty(msg, MessageConst.PROPERTY_PRODUCER_GROUP, this.defaultMQProducer.getProducerGroup());  
```

### Broker 处理事务消息
TransactionalMessageBridge#putHalfMessage
```java
public PutMessageResult putHalfMessage(MessageExtBrokerInner messageInner) {
    return store.putMessage(parseHalfMessageInner(messageInner));
}

private MessageExtBrokerInner parseHalfMessageInner(MessageExtBrokerInner msgInner) {
    MessageAccessor.putProperty(msgInner, MessageConst.PROPERTY_REAL_TOPIC, msgInner.getTopic());
    MessageAccessor.putProperty(msgInner, MessageConst.PROPERTY_REAL_QUEUE_ID,
        String.valueOf(msgInner.getQueueId()));
    msgInner.setSysFlag(
        MessageSysFlag.resetTransactionValue(msgInner.getSysFlag(), MessageSysFlag.TRANSACTION_NOT_TYPE));
    msgInner.setTopic(TransactionalMessageUtil.buildHalfTopic());
    msgInner.setQueueId(0);
    msgInner.setPropertiesString(MessageDecoder.messageProperties2String(msgInner.getProperties()));
    return msgInner;
}
```
修改消息topic为RMQ_SYS_TRANS_HALF_TOPIC，并备份消息原有topic，供后续commit消息时还原消息topic使用
修改消息queueId为0，并备份消息原有queueId，供后续commit消息时还原消息queueId使用

### Broker 处理事务消息rollback and commit
https://blog.csdn.net/hosaos/article/details/90240260

## RocketMQ Producer 生产者消息重试
https://www.cnblogs.com/qdhxhz/p/11117379.html
消息重试只针对 sendResult.getSendStatus() != SendStatus.SEND_OK 的情况

```java
int timesTotal = communicationMode == CommunicationMode.SYNC ? 1 + this.defaultMQProducer.getRetryTimesWhenSendFailed() : 1;
switch (communicationMode) {
    case ASYNC:
        return null;
    case ONEWAY:
        return null;
    case SYNC:
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            if (this.defaultMQProducer.isRetryAnotherBrokerWhenNotStoreOK()) {
                continue;
            }
        }

        return sendResult;
    default:
        break;
}
```

# RocketMQ Consumer 消费之消息重试
https://help.aliyun.com/document_detail/43490.html?spm=a2c4g.11186623.4.4.609c52c3hxFwDV  

## 顺序消息的重试
对于顺序消息，当消费者消费消息失败后，消息队列RocketMQ版会自动不断地进行消息重试（每次间隔时间为1秒），这时，应用会出现消息消费被阻塞的情况

## 无序消息的重试
对于无序消息（普通、定时、延时、事务消息），当消费者消费消息失败时，您可以通过设置返回状态达到消息重试的结果。

无序消息的重试只针对集群消费方式生效；广播方式不提供失败重试特性，即消费失败后，失败消息不再重试，继续消费新的消息

### 消息重试时间机制
消息队列RocketMQ版默认允许每条消息最多重试16次，每次重试的间隔时间如下
第N次 | 重试时间 | 第N次 | 重试时间
:--:  | :--:   |:--:   | :--:
1     | 10s    | 9     | 7 m
2     | 30s    | 10    | 8m 
3     | 1m     | 11    | 9m
4     | 2m     | 12    | 10m
5     | 3m     | 13    | 20m
6     | 4m     | 14    | 30m
7     | 5m     | 15    | 1h
8     | 6m     | 16    | 2h

https://www.cnblogs.com/allenwas3/p/11922650.html
消息消费超过最大次数或者客户端配置了直接发送到死信队列（%DLQ%+consumerGroup），则把消息发送到死信队列，否则把消息发送 retry topic (%RETRY% +consumerGroup)

虽然看起来是把消息直接写入 %RETRY% + consumerGroup,但其实在 putMessage 的时候，会把消息写入 SCHEDULE_TOPIC_XXXX
### SCHEDULE_TOPIC_XXXX
broker 并没有显式创建这个 topic，即 nameserver 和 broker 没有保存这个 broker 的元数据，topic 的数据会正常写入 commitLog，一个 delay 等级对应一个 queue，queueId = delayLevel - 1，所以 SCHEDULE_TOPIC_XXXX 最多有 18 个 queue。
ScheduleMessageService 针对每一个 level 创建一个定时任务，遍历 consume queue，判断消息是否到期，到期则把消息写入真实 topic

##  死信消息
* 不会再被消费者正常消费。
* 有效期与正常消息相同，均为 3 天，3 天后会被自动删除。因此，请在死信消息产生后的 3 天内及时处理。