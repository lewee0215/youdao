## Ribbon 使用简介
Ribbon 负载均衡默认使用的策略是 ZoneAvoidanceRule

## Ribbon主要组件及默认值
https://blog.csdn.net/alex_xfboy/article/details/88166216  

| 组件名称              | 默认值                        | 使用说明 |
| :-                   | :-                               |    :-:   |
| IRule                | ZoneAvoidanceRule             | 负载均衡策略         |
| IPing                | DummyPing                     | 服务器健康状态监测方式        |
| ServerList           | ConfigurationBasedServerList  | 服务器健康状态监测方式        |
| ServerListFilter     | ZonePreferenceServerListFilter| 接口使用不同的方法来做动态更新服务器列表        |
| ServerListUpdater    | PollingServerListUpdater      | 使用不同的方法来做动态更新服务器列表       |
| IClientConfig        | DefaultClientConfigImpl       | 定义了各种api所使用的客户端配置，用来初始化ribbon客户端和负载均衡器，默认实现是DefaultClientConfigImpl        |
| ILoadBalancer        | ZoneAwareLoadBalancer         | 定义了各种软负载，动态更新一组服务列表及根据指定算法从现有服务器列表中选择一个服务       |