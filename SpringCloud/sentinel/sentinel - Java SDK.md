# Spring Cloud Alibaba Sentinel组件
https://www.jianshu.com/p/d36f55ef2688

## POM 依赖
https://blog.csdn.net/weixin_47274985/article/details/107244118  
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    <version>0.2.0.RELEASE</version>
</dependency>
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