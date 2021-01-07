# 服务注册
在Spring云应用程序的启动阶段，将监视WebServerInitializedEvent事件。在初始化Web容器后收到WebServerInitializedEvent事件时，将触发注册操作，并调用ServiceRegistry注册方法以将服务注册到Nacos Server

## nacos - springboot
https://blog.csdn.net/cold___play/article/details/108032204  
NacosNamingService的registerInstance()
1. 通过beatReactor.addBeatInfo()创建心跳信息实现健康检测, Nacos Server必须要确保注册的服务实例是健康的,而心跳检测就是服务健康检测的手段
2. serverProxy.registerService()实现服务注册


## nacos - springcloud

## nacos - server
对外提供的服务接口请求地址为nacos/v1/ns/instance，实现代码在nacos-naming模块下的InstanceController类

# 服务发现
NacosServerList实现com.netflix.loadbalancer.ServerList接口并在@ConditionOnMissingBean下自动注入它
> Nacos Discovery Starter默认集成了Ribbon，因此对于使用Ribbon进行负载平衡的组件，可以直接使用Nacos服务发现

## nacos - springboot


## nacos - springcloud

## nacos - server