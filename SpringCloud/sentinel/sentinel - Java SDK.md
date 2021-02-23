# Spring Cloud Alibaba Sentinel组件
https://www.jianshu.com/p/d36f55ef2688  
https://www.cnblogs.com/wuzhenzhao/p/11453649.html  
限流比较主流的三种算法：漏桶，令牌桶，滑动窗口。而Sentinel采用的是最后一种，滑动窗口来实现限流的  

## POM 依赖
https://blog.csdn.net/weixin_47274985/article/details/107244118  
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    <version>0.2.0.RELEASE</version>
</dependency>
```

## Sentinel 配置文件
```properties
spring.application.name=blog

# 配置会在应用对应的机器上启动一个 Http Server，该 Server 会与 Sentinel 控制台做交互。比如 Sentinel 控制台添加了1个限流规则，会把规则数据 push 给这个 Http Server 接收，Http Server 再将规则注册到 Sentinel 中
spring.cloud.sentinel.transport.port=8720

# 测试请替换为自己的地址
spring.cloud.sentinel.transport.dashboard=116.190.247.112:8084
```

## Serntinel 基础配置类
```java
@Configuration
public class SentileConfig {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @PostConstruct
    private void initRules() throws Exception {
        FlowRule rule1 = new FlowRule();
        rule1.setResource("test.hello");
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setCount(1);   // 每秒调用最大次数为 1 次

        List<FlowRule> rules = new ArrayList<>();
        rules.add(rule1);

        // 将控制规则载入到 Sentinel
        com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules(rules);
    }
}
```

## Sentinel Main 
```java
public class SentinelApplication {
	public static void main(String[] args) throws Exception {
        initFlowRules(); //初始化一个规则
        for(int i=0;i<5;i++){
            Entry entry=null;
            try{
            	String resource = "test.hello";
                entry= SphU.entry(resource); //它做了什么
                System.out.println("Hello Word");
            }catch (BlockException e){//如果被限流了，那么会抛出这个异常
                e.printStackTrace();
            }finally {
                if(entry!=null){
                    entry.exit();// 释放
                }
            }
        }
    }
	
    private static void initFlowRules() throws Exception {
        FlowRule rule1 = new FlowRule();
        rule1.setResource("test.hello");
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setCount(3);   // 每秒调用最大次数为 1 次

        List<FlowRule> rules = new ArrayList<>();
        rules.add(rule1);

        // 将控制规则载入到 Sentinel
        // private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<List<FlowRule>>();
        com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules(rules);
    }
}
```

### SphU.entry 初始化操作
```java
// 类静态代码实现初始化
public class Env {
    public static final NodeBuilder nodeBuilder = new DefaultNodeBuilder();
    public static final Sph sph = new CtSph();
    static {
        // If init fails, the process will exit.
        InitExecutor.doInit();
    }
}

public final class InitExecutor {
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    /**
     * If one {@link InitFunc} throws an exception, the init process
     * will immediately be interrupted and the application will exit.
     * The initialization will be executed only once.
     */
    public static void doInit() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        try {
            // /META-INF/services/com.alibaba.csp.sentinel.init.InitFunc
            //com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc
            //com.alibaba.csp.sentinel.transport.init.HeartbeatSenderInitFunc  在客户端首次调用后，默认为每隔10秒向控制台发送心跳包
            ServiceLoader<InitFunc> loader = ServiceLoader.load(InitFunc.class);
            List<OrderWrapper> initList = new ArrayList<OrderWrapper>();
            for (InitFunc initFunc : loader) {
                RecordLog.info("[Sentinel InitExecutor] Found init func: " + initFunc.getClass().getCanonicalName());
                insertSorted(initList, initFunc);
            }
            for (OrderWrapper w : initList) {
                w.func.init();
                RecordLog.info(String.format("[Sentinel InitExecutor] Initialized: %s with order %d",
                    w.func.getClass().getCanonicalName(), w.order));
            }
        } catch (Exception ex) {
            RecordLog.info("[Sentinel InitExecutor] Init failed", ex);
            ex.printStackTrace();
        }
    }
}

com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter

com.alibaba.csp.sentinel.transport.heartbeat.SimpleHttpHeartbeatSender

```

### SphU.entry 执行逻辑
```java
@Override
public Entry entry(Method method, EntryType type, int count, Object... args) throws BlockException {
    MethodResourceWrapper resource = new MethodResourceWrapper(method, type);
    return entry(resource, count, args);
}

@Override
public Entry entry(String name, EntryType type, int count, Object... args) throws BlockException {
    StringResourceWrapper resource = new StringResourceWrapper(name, type);
    return entry(resource, count, args);
}
```