## RocketMQ 生产者消息重试
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

