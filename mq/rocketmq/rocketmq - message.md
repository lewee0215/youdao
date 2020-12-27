## 消息类型
a)     同步消息
b)     异步消息
c)     单向消息
d)     顺序消息
e)     批量消息
f)     过滤消息
g)     事务消息

### RocketMQ 定时&延迟消息
Rocket 默认定义了18个级别的延时消息，暂不支持指定时间戳的定时机制  
但是阿里云ONS - RocketMQ 可以实现定时消息，实现原理暂不明确。。。

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
https://www.jianshu.com/p/2838890f3284
注意：把消息发到同一个队列（queue），不是同一个topic，默认情况下一个topic包括4个queue 

Consumer 需要使用 MessageListenerOrderly，它将会定时的向 Broker 申请锁住某些特定的队列，
Broker 的RebalanceLockManager 里的 ConcurrentHashMap mqLockTable 记录着队列与 consumer client 的对应关系

consumer，除了知道自己持有哪些队列的锁，可以对这些队列进行消费外，还需要保证同一时间只有一个线程会消费同一个队列，
所以在本地维护了一个变量，其类型为：MessageQueueLock

对于每一个队列，都有一个 objLock，在消费时对该 objLock 使用 synchronizd 加锁，保证同一时间只有一个线程在消费该队列。 

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