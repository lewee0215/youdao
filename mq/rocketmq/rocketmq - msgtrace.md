# RocketMQ消息轨迹
https://www.cnblogs.com/dingwpmz/p/11923876.html

public DefaultMQProducer(final String producerGroup, boolean enableMsgTrace, final String customizedTraceTopic)

* String producerGroup  生产者所属组名。
* boolean enableMsgTrace 是否开启跟踪消息轨迹，默认为false。
* String customizedTraceTopic 如果开启消息轨迹跟踪，用来存储消息轨迹数据所属的主题名称，默认为：RMQ_SYS_TRACE_TOPIC。

```java
public DefaultMQProducer(final String producerGroup, RPCHook rpcHook, boolean enableMsgTrace,
    final String customizedTraceTopic) {
    this.producerGroup = producerGroup;
    defaultMQProducerImpl = new DefaultMQProducerImpl(this, rpcHook);
    //if client open the message trace feature
    if (enableMsgTrace) {
        try {
            AsyncTraceDispatcher dispatcher = new AsyncTraceDispatcher(customizedTraceTopic, rpcHook);
            dispatcher.setHostProducer(this.defaultMQProducerImpl);
            traceDispatcher = dispatcher;
            this.defaultMQProducerImpl.registerSendMessageHook(
                new SendMessageTraceHookImpl(traceDispatcher));
        } catch (Throwable e) {
            log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
        }
    }
}
```

### TraceDispatcher  
消息轨迹转发处理器，其默认实现类AsyncTraceDispatcher，异步实现消息轨迹数据的发送。  
<code> 
int queueSize   
异步转发，队列长度，默认为2048，当前版本不能修改。  
 
int batchSize  
批量消息条数，消息轨迹一次消息发送请求包含的数据条数，默认为100，当前版本不能修改。  

int maxMsgSize   
消息轨迹一次发送的最大消息大小，默认为128K，当前版本不能修改。 

DefaultMQProducer traceProducer   
用来发送消息轨迹的消息发送者。  

ThreadPoolExecutor traceExecuter   
线程池，用来异步执行消息发送。  

AtomicLong discardCount   
记录丢弃的消息个数。  

Thread worker   
woker线程，主要负责从追加队列中获取一批待发送的消息轨迹数据，提交到线程池中执行。  

ArrayBlockingQueue< TraceContext> traceContextQueue  
消息轨迹TraceContext队列，用来存放待发送到服务端的消息。  

ArrayBlockingQueue< Runnable> appenderQueue  
线程池内部队列，默认长度1024。 

DefaultMQPushConsumerImpl hostConsumer  
消费者信息，记录消息消费时的轨迹信息。  

String traceTopicName  
用于跟踪消息轨迹的topic名称。  
</code> 