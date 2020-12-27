# BrokerStartup.main() 启动流程
```java
final BrokerConfig brokerConfig = new BrokerConfig();

final NettyServerConfig nettyServerConfig = new NettyServerConfig();
nettyServerConfig.setListenPort(10911);

final NettyClientConfig nettyClientConfig = new NettyClientConfig();
```java