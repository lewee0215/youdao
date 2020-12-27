# Namesrv 功能简介
NameServer负责维护Producer和Consumer的配置信息、状态信息，并且协调各个角色的协同执行。通过NameServer各个角色可以了解到集群的整体信息，并且他们会定期向NameServer上报状态

# NamesrvStartup.main() 启动流程
```java
// NamesrvConfig 默认配置
// private String rocketmqHome = System.getProperty(MixAll.ROCKETMQ_HOME_PROPERTY, System.getenv(MixAll.ROCKETMQ_HOME_ENV));

// private String kvConfigPath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "kvConfig.json";
    
// private String configStorePath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "namesrv.properties";
    
// private String productEnvName = "center";
// private boolean clusterTest = false;
// private boolean orderMessageEnable = false;
final NamesrvConfig namesrvConfig = new NamesrvConfig();

// NettyServerConfig 默认配置
// private int listenPort = 8888;
// private int serverWorkerThreads = 8;
// private int serverCallbackExecutorThreads = 0;
// private int serverSelectorThreads = 3;
// private int serverOnewaySemaphoreValue = 256;
// private int serverAsyncSemaphoreValue = 64;
// private int serverChannelMaxIdleTimeSeconds = 120;

// private int serverSocketSndBufSize = NettySystemConfig.socketSndbufSize;
// private int serverSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;
// private boolean serverPooledByteBufAllocatorEnable = true;
final NettyServerConfig nettyServerConfig = new NettyServerConfig();
nettyServerConfig.setListenPort(9876);
```

# Namesrv 路由功能
https://www.jianshu.com/p/5161c16a0a29
namesrv支持集群模式，但是每个namesrv之间<font color='yellow'>相互独立不进行任何通信</font>，它的多点容灾通过producer/consumer在访问namesrv的时候轮询获取信息（当前节点访问失败就转向下一个）

namesrv作为注册中心，负责接收broker定期的注册信息并维持在内存当中，<font color='yellow'>namesrv是没有持久化功能的，所有数据都保存在内存当中</font>，broker的注册过程也是循环遍历所有namesrv进行注册

## Namesrv 路由信息管理类
org.apache.rocketmq.namesrv.routeinfo。RouteInfoManager
```java
    private final static long BROKER_CHANNEL_EXPIRED_TIME = 1000 * 60 * 2;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final HashMap<String/* topic */, List<QueueData>> topicQueueTable;
    private final HashMap<String/* brokerName */, BrokerData> brokerAddrTable;
    private final HashMap<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;
    private final HashMap<String/* brokerAddr */, BrokerLiveInfo> brokerLiveTable;
    private final HashMap<String/* brokerAddr */, List<String>/* Filter Server */> filterServerTable;
```

### 路由注册

### 路由删除

### 路由发现


