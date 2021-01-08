# RocketMQ 消息过滤
消息在Broker 端过滤。Broker 只将消息消费者感兴趣的消息发送给消息消费者。

https://www.pianshen.com/article/4525325100/  
消息过滤包含基于表达式和基于类模式两种形式，其中表达式过滤又包含TAG过滤和SQL92过滤
RocketMQ 的消息过滤方式有别于其他消息中间件，是在订阅时，再做过滤

## TAG 表达式过滤
如果一个消息有多个TAG，可以用||分隔  
Consumer端会将这个订阅请求构建成一个 SubscriptionData，发送一个Pull消息的请求给Broker端。  
Broker端从RocketMQ的文件存储层—Store读取数据之前，会用这些数据先构建一个MessageFilter，然后传给Store。    
Store从 ConsumeQueue读取到一条记录后，会用它记录的消息tag hash值去做过滤，由于在服务端只是根据hashcode进行判断，无法精确对tag原始字符串进行过滤，故在消息消费端拉取到消息后，还需要对消息的原始tag字符串进行比对，如果不同，则丢弃该消息，不进行消息消费  
```java
// tagset.contains
List<MessageExt> msgListFilterAgain = msgList;
if (!subscriptionData.getTagsSet().isEmpty() && !subscriptionData.isClassFilterMode()) {
    msgListFilterAgain = new ArrayList<MessageExt>(msgList.size());
    for (MessageExt msg : msgList) {
        if (msg.getTags() != null) {
            if (subscriptionData.getTagsSet().contains(msg.getTags())) {
                msgListFilterAgain.add(msg);
            }
        }
    }
}
```

### ExpressionMessageFilte#isMatchedByConsumeQueue
```java
@Override
public boolean isMatchedByConsumeQueue(Long tagsCode, ConsumeQueueExt.CqExtUnit cqExtUnit){
    if (null == subscriptionData) {
        return true;
    }
    if (subscriptionData.isClassFilterMode()) {
        return true;
    }
    // by tags code.
    if (ExpressionType.isTagType(subscriptionData.getExpressionType())) {
        if (tagsCode == null) {
            return true;
        }
        if (subscriptionData.getSubString().equals(SubscriptionData.SUB_ALL)) {
            return true;
        }
        // 基于TAG 模式消息过滤，还需要在消息消费端对消息tag 进行精确匹配
        return subscriptionData.getCodeSet().contains(tagsCode.intValue()); 
    } else {
        // no expression or no bloom
        if (consumerFilterData == null || consumerFilterData.getExpression() == null
            || consumerFilterData.getCompiledExpression() == null || consumerFilterData.getBloomFilterData() == null) {
            return true;
        }
        // message is before consumer
        if (cqExtUnit == null || !consumerFilterData.isMsgInLive(cqExtUnit.getMsgStoreTime())) {
            log.debug("Pull matched because not in live: {}, {}", consumerFilterData, cqExtUnit);
            return true;
        }
        byte[] filterBitMap = cqExtUnit.getFilterBitMap();
        BloomFilter bloomFilter = this.consumerFilterManager.getBloomFilter();
        if (filterBitMap == null || !this.bloomDataValid
            || filterBitMap.length * Byte.SIZE != consumerFilterData.getBloomFilterData().getBitNum()) {
            return true;
        }
        BitsArray bitsArray = null;
        try {
            bitsArray = BitsArray.create(filterBitMap);
            boolean ret = bloomFilter.isHit(consumerFilterData.getBloomFilterData(), bitsArray);
            log.debug("Pull {} by bit map:{}, {}, {}", ret, consumerFilterData, bitsArray, cqExtUnit);
            return ret;
        } catch (Throwable e) {
            log.error("bloom filter error, sub=" + subscriptionData
                + ", filter=" + consumerFilterData + ", bitMap=" + bitsArray, e);
        }
    }
    return true;
}
```

## SQL92 表达式过滤
SQL expression 的构建和执行由rocketmq-filter模块负责的。每次过滤都去执行SQL表达式会影响效率，所以RocketMQ使用了BloomFilter避免了每次都去执行
```java
// 生产者指定UserProperty
Message msg = new Message();
...
msg.putUserProperty("status", "1");
SendResult result = producer.send(msg/*,5000L*/); 

// 消费之指定过滤条件
consumer.subscribe("TestTopic", MessageSelector.bySql("(status is not null and status>=1 )"));
```
## Class 类模式过滤
基于类模式过滤是指在Broker 端运行1 个或多个消息过滤服务器（ FilterServer ), RocketMQ 允许消息消费者自定义消息过滤实现类并将其代码上传到FilterServer 上，消息消费者向FilterServer 拉取消息， FilterServer 将消息消费者的拉取命令转发到Broker ，然后对返回的消息执行消息过滤逻辑，最终将消息返
回给消费端

### FilterServer 注册
FilterServer 在启动时会创建一个定时调度任务，每隔10 s 向Broker 注册自己  
请求命令类型RequestCode.REGISTER_ FILTER_SERVER

### 
在消息拉取时， 如果发现消息过滤模式为classFilter ，将拉取消息服务器地址由
原来的Broker 地址转换成该Broker 服务器所对应的FilterServer
```java
public PullResult pullKernelImpl(
    final MessageQueue mq,
    final String subExpression,
    final String expressionType,
    final long subVersion,
    final long offset,
    final int maxNums,
    final int sysFlag,
    final long commitOffset,
    final long brokerSuspendMaxTimeMillis,
    final long timeoutMillis,
    final CommunicationMode communicationMode,
    final PullCallback pullCallback
) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
    FindBrokerResult findBrokerResult =
        this.mQClientFactory.findBrokerAddressInSubscribe(mq.getBrokerName(),
            this.recalculatePullFromWhichNode(mq), false);
    if (null == findBrokerResult) {
        this.mQClientFactory.updateTopicRouteInfoFromNameServer(mq.getTopic());
        findBrokerResult =
            this.mQClientFactory.findBrokerAddressInSubscribe(mq.getBrokerName(),
                this.recalculatePullFromWhichNode(mq), false);
    }

    if (findBrokerResult != null) {
        {
            // check version
            if (!ExpressionType.isTagType(expressionType)
                && findBrokerResult.getBrokerVersion() < MQVersion.Version.V4_1_0_SNAPSHOT.ordinal()) {
                throw new MQClientException("The broker[" + mq.getBrokerName() + ", "
                    + findBrokerResult.getBrokerVersion() + "] does not upgrade to support for filter message by " + expressionType, null);
            }
        }
        int sysFlagInner = sysFlag;

        if (findBrokerResult.isSlave()) {
            sysFlagInner = PullSysFlag.clearCommitOffsetFlag(sysFlagInner);
        }

        PullMessageRequestHeader requestHeader = new PullMessageRequestHeader();
        requestHeader.setConsumerGroup(this.consumerGroup);
        requestHeader.setTopic(mq.getTopic());
        requestHeader.setQueueId(mq.getQueueId());
        requestHeader.setQueueOffset(offset);
        requestHeader.setMaxMsgNums(maxNums);
        requestHeader.setSysFlag(sysFlagInner);
        requestHeader.setCommitOffset(commitOffset);
        requestHeader.setSuspendTimeoutMillis(brokerSuspendMaxTimeMillis);
        requestHeader.setSubscription(subExpression);
        requestHeader.setSubVersion(subVersion);
        requestHeader.setExpressionType(expressionType);

        // 判断从Broker || FilterServer 拉取消息
        String brokerAddr = findBrokerResult.getBrokerAddr();
        if (PullSysFlag.hasClassFilterFlag(sysFlagInner)) {
            brokerAddr = computPullFromWhichFilterServer(mq.getTopic(), brokerAddr);
        }

        PullResult pullResult = this.mQClientFactory.getMQClientAPIImpl().pullMessage(
            brokerAddr,
            requestHeader,
            timeoutMillis,
            communicationMode,
            pullCallback);

        return pullResult;
    }

    throw new MQClientException("The broker[" + mq.getBrokerName() + "] not exist", null);
}
```

