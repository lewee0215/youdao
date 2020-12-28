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