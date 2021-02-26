# Sentinel - 动态限流规则
https://www.jb51.net/article/199668.htm

## 原始内存模式 - Http API 推送
原生版本的规则管理通过API 将规则推送至客户端并直接更新到内存中，并不能直接用于生产环境
![](https://camo.githubusercontent.com/7f890e43f78808a19f1fd2041aedd4a8434dc41217bd91ce581116e683d5bb69/68747470733a2f2f63646e2e6e6c61726b2e636f6d2f6c61726b2f302f323031382f706e672f34373638382f313533363636303239363237332d34663434306262612d356239652d343230352d393430322d6662363038336236363931322e706e67)
<font color='yellow'>
在 sentinel 的控制台设置的规则信息默认都是存在内存当中的,重启会丢失
</font>

## Pull模式
https://github.com/alibaba/Sentinel/wiki/%E5%9C%A8%E7%94%9F%E4%BA%A7%E7%8E%AF%E5%A2%83%E4%B8%AD%E4%BD%BF%E7%94%A8-Sentinel  
pull 模式的数据源（如本地文件、RDBMS 等）一般是可写入的。使用时需要在客户端注册数据源：将对应的读数据源注册至对应的 RuleManager，将写数据源注册至 transport 的 WritableDataSourceRegistry 中
![](https://camo.githubusercontent.com/477f711417de1a652dd08e4ab157a80b0db90565bbbbac8f0889b657002bb54d/68747470733a2f2f63646e2e6e6c61726b2e636f6d2f6c61726b2f302f323031382f706e672f34373638382f313533363636303331313832362d61646466346666362d396663392d343538362d626138622d3463616633613931343537642e706e67)

```java
public class FileDataSourceInit implements InitFunc {

    @Override
    public void init() throws Exception {
        String flowRulePath = "xxx";

        ReadableDataSource<String, List<FlowRule>> ds = new FileRefreshableDataSource<>(
            flowRulePath, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {})
        );
        // 将可读数据源注册至 FlowRuleManager.
        FlowRuleManager.register2Property(ds.getProperty());

        WritableDataSource<List<FlowRule>> wds = new FileWritableDataSource<>(flowRulePath, this::encodeJson);
        // 将可写数据源注册至 transport 模块的 WritableDataSourceRegistry 中.
        // 这样收到控制台推送的规则时，Sentinel 会先更新到内存，然后将规则写入到文件中.
        WritableDataSourceRegistry.registerFlowDataSource(wds);
    }

    private <T> String encodeJson(T t) {
        return JSON.toJSONString(t);
    }
}
```

## Push 模式 - 配置中心
Sentinel提供了扩展读数据源ReadableDataSource，规则中心统一推送，客户端通过注册监听器的方式时刻监听变化，比如使用 Nacos、Zookeeper 等配置中心。这种方式有更好的实时性和一致性保证

![](https://user-images.githubusercontent.com/9434884/53381986-a0b73f00-39ad-11e9-90cf-b49158ae4b6f.png)

部署多个控制台实例时，通常需要将规则存至 DB 中，规则变更后同步向配置中心推送规则