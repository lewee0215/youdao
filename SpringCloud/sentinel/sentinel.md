# Sentinel限流实现原理
https://www.cnblogs.com/wuzhenzhao/p/11453649.html
https://baijiahao.baidu.com/s?id=1652047491406252263&wfr=spider&for=pc

FlowRuleManager.loadRules(List<FlowRule> rules); // 修改流控规则
DegradeRuleManager.loadRules(List<DegradeRule> rules); // 修改降级规则
SystemRuleManager.loadRules(List<SystemRule> rules); // 修改系统规则