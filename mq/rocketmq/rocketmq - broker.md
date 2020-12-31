# BrokerStartup.main() 启动流程
```java
final BrokerConfig brokerConfig = new BrokerConfig();

final NettyServerConfig nettyServerConfig = new NettyServerConfig();
nettyServerConfig.setListenPort(10911);

final NettyClientConfig nettyClientConfig = new NettyClientConfig();
```

# Broker 配置详解
brokerIP1 当前broker监听的IP  
brokerIP2 存在broker主从时，在broker主节点上配置了brokerIP2的话,broker从节点会连接主节点配置的brokerIP2来同步。  

> 默认不配置brokerIP1和brokerIP2时，都会根据当前网卡选择一个IP使用，当你的机器有多块网卡时，很有可能会有问题

# Broker MessageQueue
在创建或更改topic时，需要配置writeQueueNums和readQueueNums数
读写队列，则是在做路由信息时使用。在消息发送时，使用写队列个数返回路由信息，而消息消费时按照读队列个数返回路由信息
```java
topicRouteData = this.mQClientAPIImpl.getDefaultTopicRouteInfoFromNameServer(defaultMQProducer.getCreateTopicKey(), 1000 * 3);
    if (topicRouteData != null) {
        for (QueueData data : topicRouteData.getQueueDatas()) {
            int queueNums = Math.min(defaultMQProducer.getDefaultTopicQueueNums(), data.getReadQueueNums());
            data.setReadQueueNums(queueNums);
            data.setWriteQueueNums(queueNums);
        }
    }
```