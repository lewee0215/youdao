# Sentinel - 动态限流规则
https://www.jb51.net/article/199668.htm

## Http API 推送
原生版本的规则管理通过API 将规则推送至客户端并直接更新到内存中，并不能直接用于生产环境
![](https://img.jbzj.com/file_images/article/202011/2020111314384076.jpg)
<font color='yellow'>
在 sentinel 的控制台设置的规则信息默认都是存在内存当中的,重启会丢失
</font>

## 配置中心
Sentinel提供了扩展读数据源ReadableDataSource，规则中心统一推送，客户端通过注册监听器的方式时刻监听变化，比如使用 Nacos、Zookeeper 等配置中心。这种方式有更好的实时性和一致性保证

* Apollo Sentinel 配置示例
https://blog.csdn.net/weixin_34400525/article/details/91420339
```python
[
    {
        "resource": "/hello",
        "limitApp": "default",
        "grade": 1,
        "count": 5,
        "strategy": 0,
        "controlBehavior": 0,
        "clusterMode": false
    }
]

# resource：资源名，即限流规则的作用对象
# limitApp：流控针对的调用来源，若为 default 则不区分调用来源
# grade：限流阈值类型（QPS 或并发线程数）；0代表根据并发数量来限流，1代表根据QPS来进行流量控制
# count：限流阈值
# strategy：调用关系限流策略
# controlBehavior：流量控制效果（直接拒绝、Warm Up、匀速排队）
# clusterMode：是否为集群模式
```

## spring-cloud-alibaba-sentinel-datasource-0.2.0.RELEASE.jar
```sentinel-datasource.properties
nacos = com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource
file =com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource
apollo = com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource
zk = com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource
```

## FlowRuleManager
```java
public class FlowRuleManager {

    private static final Map<String, List<FlowRule>> flowRules = new ConcurrentHashMap<String, List<FlowRule>>();
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
        new NamedThreadFactory("sentinel-metrics-record-task", true));
    private final static FlowPropertyListener listener = new FlowPropertyListener();
    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<List<FlowRule>>();

    static {
        currentProperty.addListener(listener);
        scheduler.scheduleAtFixedRate(new MetricTimerListener(), 0, 1, TimeUnit.SECONDS);
    }

}
```