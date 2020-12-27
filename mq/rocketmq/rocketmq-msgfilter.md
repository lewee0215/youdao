# RocketMQ 消息过滤
消息过滤包含基于表达式和基于类模式两种形式，其中表达式过滤又包含TAG过滤和SQL92过滤
RocketMQ 的消息过滤方式有别于其他消息中间件，是在订阅时，再做过滤

## SQL92 表达式过滤
```java
// 生产者指定UserProperty
Message msg = new Message();
...
msg.putUserProperty("status", "1");
SendResult result = producer.send(msg/*,5000L*/); 

// 消费之指定过滤条件
consumer.subscribe("TestTopic", MessageSelector.bySql("(status is not null and status>=1 )"));
```

