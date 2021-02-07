# Apollo & Nacos 服务配置中心对比
https://www.jianshu.com/p/afd7776a64c6

| 配置中心        | apollo    | nacos  |
| :-             | :-:        | :-:       |
| 配置实时推送    | 支持（HTTP长轮询1s内）        | 支持（HTTP长轮询1s内）        |
| 版本管理        | 自动管理        | 自动管理        |
| 配置回滚        | 支持        | 支持        |
| 权限管理        | 支持        | - 待支持 -       |
| 多集群多环境    | 支持        | 支持        |
| 监听查询        | 支持        | 支持        |
| 多语言          | Go,C++,Python,Java,.net,OpenAPI        | Python,Java,Nodejs,OpenAPI        |
| 最小集群数量     | Config2+Admin3+Portal*2+Mysql=8        | Nacos*3+MySql=4        |
| 配置格式校验     | 支持        |支持       |
| 通信协议        | HTTP        | HTTP      |
| 数据一致性      | 数据库模拟消息队列，Apollo定时读消息        | HTTP异步通知        |
| 单机读（tps）    | 9000        | 15000        |
| 单机写（tps）    | 1100       | 1800        |