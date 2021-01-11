# Ribbon - Ping 机制
https://www.cnblogs.com/li3807/p/8889612.html  
如果单独使用 Ribbon 默认情况下不会激活 Ping 机制，默认的实现类为 DummyPing（不验证）

## Ribbon Ping 类型
https://blog.csdn.net/weixin_34148340/article/details/93079295  
IPing是Ribbon 框架中，负责检查服务实例是否存活（UP）

| Ping类型       | 说明    | 
| :-             |:-:        | 
| DummyPing      | 虚设的IPing实现，永远返回true      | 
| NoOpPing       | 什么也不做，直接返回true      |
| PingConstant   | 一个工具类IPing实现，只要常量参数为true，则表示服务存活     |
| PingUrl        | 通过request访问服务返回的状态码来判定服务是否存活        |
| NIWSDiscoveryPing | 通过Eureka来判定服务实例是否存活  |

# Ribbon 负载规则
https://www.cnblogs.com/fx-blog/p/11713872.html
| P规则类型                     | 说明    | 
| :-                           |:-:        | 
| AvailabilityFilteringRule    | 过滤掉一直连接失败的被标记为circuit tripped（电路跳闸）的后端Service，并过滤掉那些高并发的后端Server或者使用一个AvailabilityPredicate来包含过滤Server的逻辑，其实就是检查status的记录的各个Server的运行状态     | 
| BestAvailableRule            | 选择一个最小的并发请求的Server，逐个考察Server，如果Server被tripped了，则跳过      |
| RandomRule                   | 随机选择一个Server     |
| ResponseTimeWeightedRule     | 已废弃，作用同WeightedResponseTimeRule        |
| RetryRule                    | 对选定的负责均衡策略机上充值机制，在一个配置时间段内当选择Server不成功，则一直尝试使用subRule的方式选择一个可用的Server  |
| RoundRobinRule               | 轮询选择，轮询index，选择index对应位置Server  |
| WeightedResponseTimeRule     | 根据相应时间加权，相应时间越长，权重越小，被选中的可能性越低  |
| ZoneAvoidanceRule            | （默认是这个）负责判断Server所Zone的性能和Server的可用性选择Server，在没有Zone的环境下，类似于轮询（RoundRobinRule） |

# Ribbon 支持 Nacos 权重
在Nacos的控制台，可以为每一个实例配置权重，取值在0～1之间，值越大，表示这个被调用的几率越大

