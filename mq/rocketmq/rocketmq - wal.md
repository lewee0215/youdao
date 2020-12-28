# RocketMQ WAL 模式
目前MQ的方案中都是基于WAL的方式实现的（RocketMQ、Kafka），日志文件会被过期删除，一般会保留最近一段时间的数据

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
