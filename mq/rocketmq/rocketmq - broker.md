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
读写队列，则是在做路由信息时使用。

在消息发送时，使用写队列个数返回路由信息，而消息消费时按照读队列个数返回路由信息
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

> message queue 是无限长的数组，一条消息进来下标就会涨1，下标就是 offset，消息在某个 MessageQueue 里的位置，通过 offset 的值可以定位到这条消息，或者指示 Consumer 从这条消息开始向后处理。  

> message queue 中的 maxOffset 表示消息的最大 offset，maxOffset 并不是最新的那条消息的 offset，而是最新消息的 offset+1，minOffset 则是现存在的最小 offset。

> fileReserveTime=48 默认消息存储48小时后，消费会被物理地从磁盘删除，message queue 的minOffset 也就对应增长。所以比 minOffset 还要小的那些消息已经不在 broker上了，就无法被消费