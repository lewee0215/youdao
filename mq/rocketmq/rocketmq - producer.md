## RocketMQ Producer Group 
发送分布式事务消息时，如果Producer中途意外宕机，Broker会主动回调Producer Group内的任意一台机器来确认事务状态

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

##  死信消息
* 不会再被消费者正常消费。
* 有效期与正常消息相同，均为 3 天，3 天后会被自动删除。因此，请在死信消息产生后的 3 天内及时处理。
