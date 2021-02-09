# Sentinel 与 Hystrix 功能对比
https://blog.csdn.net/lichao920926/article/details/105295988/

| 功能项        | Sentinel     | Hystrix   |
| :-           | :-:        | :-:       |
| 隔离策略      | 信号量隔离        | 	线程池隔离/信号量隔离       |
| 熔断降级策略  | 基于响应时间或失败比率        | 	基于失败比率        |
| 实时指标实现  | 	滑动窗口        | 滑动窗口（基于 RxJava）        |
| 规则配置      | 	支持多种数据源        | 	支持多种数据源        |
| 扩展性        | 多个扩展点        | 插件的形式        |
| 基于注解的支持 | 支持        | 支持        |
| 限流          | 基于 QPS，支持基于调用关系的限流        | 不支持        |
| 流量整形      | 支持慢启动、匀速器模式        | 不支持        |
| 系统负载保护   | 支持        | 不支持        |
| 控制台        | 	开箱即用，可配置规则、查看秒级监控、机器发现等        | 	不完善        |
| 框架适配       | Servlet、Spring Cloud、Dubbo、gRPC 等        | Servlet、Spring Cloud Netflix        |

@ Sentinel-与-Hystrix-的对比详情： https://github.com/alibaba/Sentinel/wiki/Sentinel-%E4%B8%8E-Hystrix-%E7%9A%84%E5%AF%B9%E6%AF%94