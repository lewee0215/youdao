## RocketMQ Producer 

### producer.setVipChannelEnabled(true);
配置说明：是否启用vip netty通道以发送消息
默认值：-D com.rocketmq.sendMessageWithVIPChannel参数的值，若无则是true

broker的netty server会起两个通信服务。两个服务除了服务的端口号不一样，其他都一样。  
其中一个的端口（配置端口-2）作为vip通道，客户端可以启用本设置项把发送消息此vip通道

## RocketMQ Producer Group 
发送分布式事务消息时，如果Producer中途意外宕机，Broker会主动回调Producer Group内的任意一台机器来确认事务状态

## RocketMQ SendStatus：
send消息方法，只要不抛异常，就代表发送成功。但是发送成功会有多个状态，在sendResult里定义。
SEND_OK：消息发送成功
FLUSH_DISK_TIMEOUT：消息发送成功，但是服务器刷盘超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失
FLUSH_SLAVE_TIMEOUT：消息发送成功，但是服务器同步到Slave时超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失
SLAVE_NOT_AVAILABLE：消息发送成功，但是此时slave不可用，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失

### System busy & Broker busy
https://blog.csdn.net/prestigeding/article/details/109335880  

#### 1. PageCache 压力较大
判断 PageCache 是否忙的依据就是，在写入消息、向内存追加消息时加锁的时间，默认的判断标准是加锁时间超过 1s，就认为是 PageCache 压力大

#### 2. 发送线程池挤压的拒绝策略
在 RocketMQ 中处理消息发送的，是一个只有一个线程的线程池，内部会维护一个有界队列，默认长度为 1W。如果当前队列中挤压的数量超过 1w，执行线程池的拒绝策略，从而抛出 [too many requests and system thread pool busy] 错误

#### 3. Broker 端快速失败
默认情况下 Broker 端开启了快速失败机制，就是在 Broker 端还未发生 PageCache 繁忙（加锁超过 1s）的情况，但存在一些请求在消息发送队列中等待 200ms 的情况，RocketMQ 会不再继续排队，直接向客户端返回 System busy，但由于 RocketMQ 客户端目前对该错误没有进行重试处理，所以在解决这类问题的时候需要额外处理。

#### 解决方案
1. transientStorePoolEnable ， 配置文件添加：  
<code> 
transientStorePoolEnable=true  
</code>
消息先写入到堆外内存中，该内存由于启用了内存锁定机制，故消息的写入是接近直接操作内存，性能可以得到保证。  
消息进入到堆外内存后，后台会启动一个线程，一批一批将消息提交到 PageCache，即写消息时对 PageCache 的写操作由单条写入变成了批量写入，降低了对 PageCache 的压力  

2. 扩容
对集群中的 Topic 进行拆分，即将一部分 Topic 迁移到其他集群中，降低集群的负载

## Producer端 Broker故障延迟机制
Mq在发送端引入了Broker故障转移机制，能够在某个Broker异常时，根据当次请求RT时间，预估出Broker的故障持续时间，在这段持续时间内暂时屏蔽该Broker，将消息发往其他Broker
https://blog.csdn.net/hosaos/article/details/99624467

在默认的消息发送方法前会调用 selectOneMessageQueue 方法  
无论消息发送成功或是抛出异常都会调用 updateFaultItem 方法用于维护可用的 Broker 池

```java
private SendResult sendDefaultImpl(Message msg,final CommunicationMode communicationMode,
        final SendCallback sendCallback,
        final long timeout){

    //定义重试次数
    int timesTotal = communicationMode == CommunicationMode.SYNC ? 1 + this.defaultMQProducer.getRetryTimesWhenSendFailed() : 1;
    int times = 0;
    ...

    for (; times < timesTotal; times++) {
        String lastBrokerName = null == mq ? null : mq.getBrokerName();
        MessageQueue mqSelected = this.selectOneMessageQueue(topicPublishInfo, lastBrokerName);
        ...
        try{
            if (times > 0) {
                //Reset topic with namespace during resend.
                msg.setTopic(this.defaultMQProducer.withNamespace(msg.getTopic()));
            }
            ......
            sendResult = this.sendKernelImpl(msg, mq, communicationMode, sendCallback, topicPublishInfo, timeout - costTime);
            this.updateFaultItem(mq.getBrokerName(), endTimestamp - beginTimestampPrev, false);
        }
        catch(RemotingException,MQClientException,MQBrokerException){
            .....
            this.updateFaultItem(mq.getBrokerName(), endTimestamp - beginTimestampPrev, true);
        }
    }
}

```

即如果消息发送时间越久，mq会认为broker不可用的时长越久，broker不可用时长是个经验值，
如果传入isolation为true，即发送异常的情况,表示默认当前发送时长为30000L，即broker不可用时长为600000L
```java
/**
 * currentLatency : 正常发送消息耗时
 * isolation:是否需要隔离 (false = 正常发送 ，true = 发送异常)
 */
public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation) {
    if (this.sendLatencyFaultEnable) {
        // 根据消息异常情况更新 FaultItem 的 duration (暂停使用时间)
        long duration = computeNotAvailableDuration(isolation ? 30000 : currentLatency);
        this.latencyFaultTolerance.updateFaultItem(brokerName, currentLatency, duration);
    }
}

/**
 * 计算分档的不可用时间
 * currentLatency : 发送当前消息的耗时时间
 */
private long computeNotAvailableDuration(final long currentLatency) {
    // private long[] latencyMax = {50L, 100L, 550L, 1000L, 2000L, 3000L, 15000L};
    // private long[] notAvailableDuration = {0L, 0L, 30000L, 60000L, 120000L, 180000L, 600000L};
    for (int i = latencyMax.length - 1; i >= 0; i--) {
        if (currentLatency >= latencyMax[i])
            return this.notAvailableDuration[i];
    }

    return 0;
}

/**
 * 维护 Broker 的不可用列表
 */
@Override
public void updateFaultItem(final String name, final long currentLatency, final long notAvailableDuration){
    // faultItemTable = new ConcurrentHashMap<String, FaultItem>(16);
    FaultItem old = this.faultItemTable.get(name);
    if (null == old) {
        final FaultItem faultItem = new FaultItem(name);
        faultItem.setCurrentLatency(currentLatency);
        faultItem.setStartTimestamp(System.currentTimeMillis() + notAvailableDuration);

        old = this.faultItemTable.putIfAbsent(name, faultItem);
        if (old != null) {
            old.setCurrentLatency(currentLatency);
            old.setStartTimestamp(System.currentTimeMillis() + notAvailableDuration);
        }
    } else {
        old.setCurrentLatency(currentLatency);
        old.setStartTimestamp(System.currentTimeMillis() + notAvailableDuration);
    }
}

class FaultItem implements Comparable<FaultItem>{

    @Override
    public int compareTo(final FaultItem other){
        // 根据 currentLatency(当前耗时) -> startTimestamp(可开始使用时间戳)
        // startTimestamp = updateFaultItem 方法调用时 ： System.currentTimeMillis() + notAvailableDuration,
    }

    // 判断 Broker 是否可用
    public boolean isAvailable() {
        return (System.currentTimeMillis() - startTimestamp) >= 0;
    }
}

/**
 * 队列选择方法
 */
public MessageQueue selectOneMessageQueue(final TopicPublishInfo tpInfo, final String lastBrokerName) {
    // 判断是否开始延时容错, 默认是不启用故障延迟机制的
    if (this.sendLatencyFaultEnable) {
        try {
            // 轮训获取 Queue
            int index = tpInfo.getSendWhichQueue().getAndIncrement();
            for (int i = 0; i < tpInfo.getMessageQueueList().size(); i++) {
                int pos = Math.abs(index++) % tpInfo.getMessageQueueList().size();
                if (pos < 0)
                    pos = 0;
                MessageQueue mq = tpInfo.getMessageQueueList().get(pos);
                if (latencyFaultTolerance.isAvailable(mq.getBrokerName())) {
                    // 如果上一次发送的Broker是可用的，则从当前Broker选择遍历循环选择一个
                    if (null == lastBrokerName || mq.getBrokerName().equals(lastBrokerName))
                        return mq;
                }
            }

            // 如果不存在上诉最优解，取延时最小的Broker
            final String notBestBroker = latencyFaultTolerance.pickOneAtLeast();
            int writeQueueNums = tpInfo.getQueueIdByBroker(notBestBroker);
            if (writeQueueNums > 0) {
                final MessageQueue mq = tpInfo.selectOneMessageQueue();
                if (notBestBroker != null) {
                    mq.setBrokerName(notBestBroker);
                    mq.setQueueId(tpInfo.getSendWhichQueue().getAndIncrement() % writeQueueNums);
                }
                return mq;
            } else {
                // 排除没有可写队列的Broker
                latencyFaultTolerance.remove(notBestBroker);
            }
        } catch (Exception e) {
            log.error("Error occurred when selecting message queue", e);
        }

        // 如果没有最优项，则轮训取下一个
        return tpInfo.selectOneMessageQueue();
    }

    return tpInfo.selectOneMessageQueue(lastBrokerName);
}

// 排除上次发送的Broker
// 如果取到的消息队列还是上次发送失败的broker，则重新对sendWhichQueue+1
public MessageQueue selectOneMessageQueue(final String lastBrokerName) {
    if (lastBrokerName == null) {
        return selectOneMessageQueue();
    } else {
        int index = this.sendWhichQueue.getAndIncrement();
        for (int i = 0; i < this.messageQueueList.size(); i++) {
            int pos = Math.abs(index++) % this.messageQueueList.size();
            if (pos < 0)
                pos = 0;
            MessageQueue mq = this.messageQueueList.get(pos);
            if (!mq.getBrokerName().equals(lastBrokerName)) {
                return mq;
            }
        }
        return selectOneMessageQueue();
    }
}

// 默认根据 sendWhichQueue 自增特性轮训发送
// private volatile ThreadLocalIndex sendWhichQueue = new ThreadLocalIndex();
public MessageQueue selectOneMessageQueue() {
    int index = this.sendWhichQueue.getAndIncrement();
    int pos = Math.abs(index) % this.messageQueueList.size();
    if (pos < 0)
        pos = 0;
    return this.messageQueueList.get(pos);
}

```




