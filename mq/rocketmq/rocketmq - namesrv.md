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
集群信息 : cluster -> broker -> topic -> queue   
活跃Broker信息 ： brokerLiveTable  
消息过滤服务器  :  filterServerTable

```java
public class BrokerData implements Comparable<BrokerData> {
    private String cluster;
    private String brokerName;
    private HashMap<Long/* brokerId */, String/* broker address */> brokerAddrs;
}

public class QueueData implements Comparable<QueueData> {
    private String brokerName;
    private int readQueueNums;
    private int writeQueueNums;
    private int perm;
    private int topicSynFlag;
}

class BrokerLiveInfo {
    private long lastUpdateTimestamp;  // 存储上次收到Broker 心跳包的时间
    private DataVersion dataVersion;
    private Channel channel;
    private String haServerAddr;
}
```

### 路由注册
Broker 启动时向集群中所有的NameServer发送心跳语句，每隔30s向集群中所有NameServer 发送心跳包  
NameServer收到Broker心跳包后更新brokerLiveTable 缓存中BrokerLiveInfo 的lastUpdateTimestamp,
同时维护 cluster -> broker -> topic -> queue  关系  
<code>brokerLiveTable  使用读写锁的方式操作</code>  


### 路由删除
1. NameServer 每隔10s 扫描brokerLiveTable ，如果连续 120s 没有收到心跳包， NameServer 将移除该Broker 的路由信息同时关闭Socket 连接。
2. Broker 正常下线，执行 unregisterBroker 指令

### 路由发现
RocketMQ 路由发现是非实时的，当Topic 路由出现变化后， NameServer 不主动推送给客户端， 而是由客户端定时拉取主题最新的路由

