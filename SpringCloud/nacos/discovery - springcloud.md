# springcloud使用nacos进行服务注册与发现
https://blog.csdn.net/qq_28851503/article/details/88752734  

## 服务注册
spring-cloud-starter-alibaba-nacos-discovery遵循了spring-cloud-common标准，实现了 AutoServiceRegistration、ServiceRegistry、Registration 

在Spring云应用程序的启动阶段，将监视WebServerInitializedEvent事件。在初始化Web容器后收到WebServerInitializedEvent事件时，将触发注册操作，并调用ServiceRegistry注册方法以将服务注册到Nacos Server

## 服务发现
NacosServerList 实现了 com.netflix.loadbalancer.ServerList 接口，并在 @ConditionOnMissingBean 的条件下进行自动注入，默认集成了Ribbon。

如果需要有更加自定义的可以使用 @Autowired 注入一个 NacosRegistration 实例，通过其持有的 NamingService 字段内容直接调用 Nacos API

