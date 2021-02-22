# Sentinel限流实现原理
https://www.cnblogs.com/wuzhenzhao/p/11453649.html
https://baijiahao.baidu.com/s?id=1652047491406252263&wfr=spider&for=pc

## 限流规则定义
FlowRuleManager.loadRules(List<FlowRule> rules); // 修改流控规则  

## 降级规则定义
DegradeRuleManager.loadRules(List<DegradeRule> rules); // 修改降级规则  

## 系统规则定义
SystemRuleManager.loadRules(List<SystemRule> rules); // 修改系统规则  
* LOAD    
* RT    
* 线程数    
* 入口 QPS    
* CPU 使用率  

## Sentinel - SlotChain
https://www.cnblogs.com/wuzhenzhao/p/11453649.html  
Entry 创建的时候，同时也会创建一系列功能插槽（slot chain），这些插槽有不同的职责，例如:

* NodeSelectorSlot ：收集资源的路径，并将这些资源的调用路径，以树状结构存储起来，用于根据调用路径来限流降级；
* ClusterBuilderSlot ：用于存储资源的统计信息以及调用者信息，例如该资源的 RT, QPS, thread count 等等，这些信息将用作为多维度限流，降级的依据；
* StatisticSlot ：用于记录、统计不同纬度的 runtime 指标监控信息；
* SystemSlot ：通过系统的状态，例如 load1 等，来控制总的入口流量；
* AuthoritySlot ：根据配置的黑白名单和调用来源信息，来做黑白名单控制；
* FlowSlot ：用于根据预设的限流规则以及前面 slot 统计的状态，来进行流量控制；
* DegradeSlot ：通过统计信息以及预设的规则，来做熔断降级

![](https://img2018.cnblogs.com/blog/1383365/201909/1383365-20190903163200661-600499825.png)

## 监控
https://www.jb51.net/article/199668.htm  
Sentinel 会记录资源访问的秒级数据（若没有访问则不进行记录）并保存在本地日志中。Sentinel 控制台可以通过 Sentinel 客户端预留的 HTTP API 从秒级监控日志中拉取监控数据，并进行聚合。

目前 Sentinel 控制台中监控数据聚合后直接存在内存中，未进行持久化，且仅保留最近 5 分钟的监控数据。若需要监控数据持久化的功能，可以自行扩展实现

