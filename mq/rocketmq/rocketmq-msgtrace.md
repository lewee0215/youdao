# RocketMQ消息轨迹
https://www.cnblogs.com/dingwpmz/p/11923876.html

public DefaultMQProducer(final String producerGroup, boolean enableMsgTrace, final String customizedTraceTopic)

* String producerGroup  生产者所属组名。
* boolean enableMsgTrace 是否开启跟踪消息轨迹，默认为false。
* String customizedTraceTopic 如果开启消息轨迹跟踪，用来存储消息轨迹数据所属的主题名称，默认为：RMQ_SYS_TRACE_TOPIC。

```java
public DefaultMQProducer(final String producerGroup, RPCHook rpcHook, boolean enableMsgTrace,final String customizedTraceTopic) {      // @1
    this.producerGroup = producerGroup;
    defaultMQProducerImpl = new DefaultMQProducerImpl(this, rpcHook);
    //if client open the message trace feature
    if (enableMsgTrace) { 
        // @2
        try {
            AsyncTraceDispatcher dispatcher = new AsyncTraceDispatcher(customizedTraceTopic, rpcHook;                                                         
            dispatcher.setHostProducer(this.getDefaultMQProducerImpl());
            traceDispatcher = dispatcher;
            this.getDefaultMQProducerImpl().registerSendMessageHook(
                new SendMessageTraceHookImpl(traceDispatcher));
        // @3
        } catch (Throwable e) {
            log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
        }
    }
}
```