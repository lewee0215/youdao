## nacos-discovery-springcloud 配置文件信息
https://blog.csdn.net/weixin_40826349/article/details/90750041  

|配置项	|键	|默认值	|描述|
| :-                     |  :-  | :-    | :- |
|服务器地址	|spring.cloud.nacos.discovery.server-addr|	-	|nacos注册中心地址|
|服务名	|spring.cloud.nacos.discovery.service|	spring.application.name	|服务名|
|权重|	spring.cloud.nacos.discovery.weight	|1|	值从1到100，值越大，重量越大|
|IP	|spring.cloud.nacos.discovery.ip|	-	|ip address to registry，最高优先级|
|网络接口|	spring.cloud.nacos.discovery.network-interface|	-	|未配置IP时，注册的IP地址为网络接口对应的IP地址。如果未配置此项，则默认采用第一个网络接口的地址。|
|端口	|spring.cloud.nacos.discovery.port|	-1	|注册端口，无需配置即可自动检测|
|namesapce	|spring.cloud.nacos.discovery.namespace|	-	|开发环境（dev、pro等）|
|accesskey|	spring.cloud.nacos.discovery.access-key|	-	||
|secretkey|	spring.cloud.nacos.discovery.secret-key	|	-||
|元数据|	spring.cloud.nacos.discovery.metadata|	-	|扩展数据，使用Map格式配置|
|日志名称|	spring.cloud.nacos.discovery.log-name|- ||		
|端点|	spring.cloud.nacos.discovery.endpoint|	-	|服务的域名，通过该域名可以动态获取服务器地址。|
|集成功能区	|ribbon.nacos.enabled|	true||	

