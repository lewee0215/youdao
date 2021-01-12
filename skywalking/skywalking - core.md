# SkyWalking 核心模块
https://www.pianshen.com/article/87011016639/
1. Skywalking Agent：链路数据采集tracing（调用链数据）和metric（指标）信息并上报，上报通过HTTP或者gRPC方式发送数据到Skywalking Collector
2. Skywalking Collector ： 链路数据收集器，对agent传过来的tracing和metric数据进行整合分析通过Analysis Core模块处理并落入相关的数据存储中，同时会通过Query Core模块进行二次统计和监控告警
3. Storage： Skywalking的存储，支持以ElasticSearch、Mysql、TiDB、H2等主流存储作为存储介质进行数据存储,H2仅作为临时演示单机用。
4. SkyWalking UI： Web可视化平台，用来展示落地的数据，目前官方采纳了RocketBot作为SkyWalking的主UI