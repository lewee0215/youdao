# Sentinel使用Apollo存储规则
https://blog.csdn.net/p923284735/article/details/90896686  

## POM 依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-datasource-apollo</artifactId>
        <version>1.5.2</version>
    </dependency>
</dependencies>
```

## apollo-env.properties
```properties
spring.application.name=sentinel-datasource-apollo
server.port=8002

# apollo config
app.id=${spring.application.name}

# sentinel dashboard
spring.cloud.sentinel.transport.dashboard=localhost:8080

# sentinel datasource apollo
spring.cloud.sentinel.datasource.ds.apollo.namespaceName=application
spring.cloud.sentinel.datasource.ds.apollo.flowRulesKey=sentinel.flowRules
spring.cloud.sentinel.datasource.ds.apollo.rule-type=flow
```

* app.id：Apollo中创建的项目名称，这里采用 spring.application.name 参数的引用，从而达到服务名与配置项目名一致的效果。
* spring.cloud.sentinel.transport.dashboard：sentinel dashboard 的访问地址，根据上面的准备工作中启动的实例配置
* spring.cloud.sentinel.datasource.ds.apollo.namespaceName：Apollo的空间名
* spring.cloud.sentinel.datasource.ds.apollo.flowRulesKey：配置规则的key名称
* spring.cloud.sentinel.datasource.ds.apollo.rule-type：该参数是spring cloud alibaba 升级到0.2.2之后的配置，用来定义存储的规则类型;所有的规则类型可查看枚举类：org.springframework.cloud.alibaba.sentinel.datasource.RuleType,每种规则的定义格式可以通过各枚举值中定义的规则对象来查看，比如限流规则可查看：com.alibaba.csp.sentinel.slots.block.flow.FlowRule

## Apollo Sentinel 配置示例
https://blog.csdn.net/weixin_34400525/article/details/91420339
![](https://img-blog.csdnimg.cn/20190605090952382.png)
```python
## 注册 Apollo 数据源 
https://www.cnblogs.com/yinjihuan/p/10634675.html
https://blog.csdn.net/weixin_34290096/article/details/91427306  

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