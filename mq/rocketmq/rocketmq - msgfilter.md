# RocketMQ 消息过滤
消息在Broker 端过滤。Broker 只将消息消费者感兴趣的消息发送给消息消费者。

https://www.pianshen.com/article/4525325100/  
消息过滤包含基于表达式和基于类模式两种形式，其中表达式过滤又包含TAG过滤和SQL92过滤
RocketMQ 的消息过滤方式有别于其他消息中间件，是在订阅时，再做过滤

## TAG 表达式过滤
如果一个消息有多个TAG，可以用||分隔
Consumer端会将这个订阅请求构建成一个 SubscriptionData，发送一个Pull消息的请求给Broker端。Broker端从RocketMQ的文件存储层—Store读取数据之前，会用这些数据先构建一个MessageFilter，然后传给Store。  
Store从 ConsumeQueue读取到一条记录后，会用它记录的消息tag hash值去做过滤，由于在服务端只是根据hashcode进行判断，无法精确对tag原始字符串进行过滤，故在消息消费端拉取到消息后，还需要对消息的原始tag字符串进行比对，如果不同，则丢弃该消息，不进行消息消费

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

