# skywalking 监控视图
https://www.pianshen.com/article/87011016639/  
Skywalking已经支持从6个可视化维度剖析分布式系统的运行情况

1. 总览视图(Global view)是应用和组件的全局视图，其中包括组件和应用数量，应用的告警波动，慢服务列表以及应用吞吐量；
2. 拓扑图(topology view)从应用依赖关系出发，展现整个应用的拓扑关系；
3. 应用视图()则是从单个应用的角度，展现应用的上下游关系，TopN的服务和服务器，JVM的相关信息以及对应的主机信息。- - 服务视图关4. 注单个服务入口的运行情况以及此服务的上下游依赖关系，依赖度，帮助用户针对单个服务的优化和监控；
5. 调用链(trace)展现了调用的单次请求经过的所有埋点以及每个埋点的执行时长；
6. 告警视图(alarm)根据配置阈值针对应用、服务器、服务进行实时告警

## 仪表盘：查看全局服务基本性能指标
仪表盘主要包含Service Dashboard和Database Dashboard
* Service Dashboard：有Global、Service、Endpoint、Instance面板，展示了全局以及服务、端点、实例的详细信息
* Database Dashboard：展示数据库的响应时间、响应时间分布、吞吐量、SLA、慢SQL等详细信息，便于直观展示数据库状态

## 拓扑图：展示分布式服务之间调用关系


## 链路追踪：可以根据需求，查看链路调用过程

## 告警提示
https://blog.csdn.net/wsyyyyy/article/details/101690014  
skywalking发送告警的基本原理是每隔一段时间轮询skywalking-collector收集到的链路追踪的数据，再根据所配置的告警规则（如服务响应时间、服务响应时间百分比）等，如果达到阈值则发送响应的告警信息   

> 发送告警信息是以线程池异步的方式调用webhook接口完成

### 告警规则配置
https://blog.51cto.com/zero01/2463976  
开启skywalking相关告警配置，编辑 config/alarm-settings.yml  
```yml
rules:
  # Rule unique name, must be ended with `_rule`.
  # 规则名称，也是在告警信息中显示的唯一名称。必须以_rule结尾，前缀可自定义
  endpoint_percent_rule:
    # Metrics value need to be long, double or int
    # 度量名称，取值为oal脚本中的度量名，目前只支持long、double和int类型
    metrics-name: service_resp_time
    threshold: 1000
    op: >

    # The length of time to evaluate the metrics
    period: 10
    # How many times after the metrics match the condition, will trigger alarm
    # 在一个Period窗口中，如果values超过Threshold值（按op），达到Count值，需要发送警报
    count: 3
    # How many times of checks, the alarm keeps silence after alarm triggered, default as same as period.
    # 在时间N中触发报警后，在TN -> TN + period这个阶段不告警
    silence-period: 10
    message: Response time of service {name} is more than 1000ms in 3 minutes of last 10 minutes.
    
  service_percent_rule:
    metrics-name: service_percent
    # [Optional] Default, match all services in this metrics
    include-names:
      - service_a
      - service_b
    threshold: 85
    op: <
    period: 10
    count: 4
 
webhooks:
# 一定要注意url的缩进，之前缩进两个空格，一直没生效
 - http://127.0.0.1//alarm/test
```
### 告警webhook接口对接
webhook接口调用的方式是post+requestbody，而body中的内容如下：
```json
[{
    "scopeId": 1, // scopeId、scope：所有可用的 Scope 详见 org.apache.skywalking.oap.server.core.source.DefaultScopeDefine
    "scope": "SERVICE",  // 目标 Scope 的实体名称
    "name": "serviceA",  
    "id0": 12,  // Scope 实体的 ID
    "id1": 0,   // 保留字段，目前暂未使用
    "ruleName": "service_resp_time_rule",  // 告警规则名称
    "alarmMessage": "alarmMessage xxxx",
    "startTime": 1560524171000
}, {
    "scopeId": 1,
    "scope": "SERVICE",
    "name": "serviceB",
    "id0": 23,
    "id1": 0,
    "ruleName": "service_resp_time_rule",
    "alarmMessage": "alarmMessage yyy",
    "startTime": 1560524171000
}]
```
