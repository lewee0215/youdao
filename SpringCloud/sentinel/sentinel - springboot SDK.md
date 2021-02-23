# Sentinel - InitFunc
/META-INF/services/com.alibaba.csp.sentinel.init.InitFunc
```java
com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc
com.alibaba.csp.sentinel.transport.init.HeartbeatSenderInitFunc
```

# Sentinel - CommandHandler
/META-INF/services/com.alibaba.csp.sentinel.command.CommandHandler
```java
com.alibaba.csp.sentinel.command.handler.BasicInfoCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchActiveRuleCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchClusterNodeByIdCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchClusterNodeHumanCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchJsonTreeCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchOriginCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchSimpleClusterNodeCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchSystemStatusCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchTreeCommandHandler
com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler
com.alibaba.csp.sentinel.command.handler.OnOffGetCommandHandler
com.alibaba.csp.sentinel.command.handler.OnOffSetCommandHandler
com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler
com.alibaba.csp.sentinel.command.handler.VersionCommandHandler
```

# Sentinel - JDBC DataSource
https://blog.csdn.net/weixin_33940102/article/details/91665317  

# Sentinel - File DataSource
https://blog.csdn.net/qq_29064815/article/details/107163048

# Sentinel - 规则持久化
https://blog.csdn.net/weixin_34357962/article/details/87993984  
PS：需要注意的是，我们需要在系统启动的时候调用该数据源注册的方法，否则不会生效的。
具体的方式有多种实现方式
1. Spring 来初始化该方法
2. 自定义一个类来实现 Sentinel 中的 InitFunc 接口来完成初始化




