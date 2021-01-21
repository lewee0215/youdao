# Apollo - cluster (集群)
https://ctripcorp.github.io/apollo/#/zh/design/apollo-introduction  

1. 通过添加集群，可以使同一份程序在不同的集群（如不同的数据中心）使用不同的配置  
如果不同集群使用一样的配置，则没有必要创建集群  
2. Apollo默认会读取机器上/opt/settings/server.properties(linux)或C:\opt\settings\server.properties(windows)文件中的idc属性作为集群名字， 如SHAJQ（金桥数据中心）、SHAOY（欧阳数据中心)  
在这里创建的集群名字需要和机器上server.properties中的idc属性一致  

```yml
# Apollo会默认使用应用实例所在的数据中心作为cluster，所以如果两者一致的话，不需要额外配置。
# 如果cluster和数据中心不一致的话，那么就需要通过System Property方式来指定运行时cluster：
-Dapollo.cluster=SomeCluster
```

# Apollo - namespace (命名空间)
https://ctripcorp.github.io/apollo/#/zh/design/apollo-introduction  
应用可以通过关联公共namespace来覆盖公共Namespace的配置
如果应用不需要覆盖公共Namespace的配置，那么无需关联公共Namespace


# Apollo - Server 架构设计
https://www.cnblogs.com/qdhxhz/p/13394182.html  

![alt text](https://img2020.cnblogs.com/blog/1090617/202007/1090617-20200728215539079-1535962969.jpg "title")

> 1> ConfigService  

Config Service提供配置的读取、推送等功能，服务对象是Apollo客户端

> 2> Admin Service 

Admin Service提供配置的修改、发布等功能，服务对象是Apollo Portal（管理界面）

> 3> Client (客户端)

Client通过域名访问Meta Server获取Config Service服务列表（IP+Port），而后直接通过IP+Port访问服务，同时在Client侧会做load balance、错误重试

> 4> Porta (Web界面供用户管理配置)

Portal通过域名访问Meta Server获取Admin Service服务列表（IP+Port），而后直接通过IP+Port访问服务，同时在Portal侧会做load balance、错误重试

\## Config Service和Admin Service都是多实例、无状态部署，所以需要将自己注册到Eureka中并保持心跳  
\## 在Eureka之上我们架了一层Meta Server用于封装Eureka的服务发现接口  
\## 为了简化部署，我们实际上会把Config Service、Eureka和Meta Server三个逻辑角色部署在同一个JVM进程中  

# Apollo - Client 客户端设计
https://ctripcorp.github.io/apollo/#/zh/design/apollo-introduction  

1. 客户端和服务端保持了一个长连接，从而能第一时间获得配置更新的推送。
2. 客户端还会定时从Apollo配置中心服务端拉取应用的最新配置。
    * 这是一个fallback机制，为了防止推送机制失效导致配置不更新
    * 客户端定时拉取会上报本地版本，所以一般情况下，对于定时拉取的操作，服务端都会返回304 - Not Modified
    * 定时频率默认为每5分钟拉取一次，客户端也可以通过在运行时指定System Property: apollo.refreshInterval来覆盖，单位为分钟。
3. 客户端从Apollo配置中心服务端获取到应用的最新配置后，会保存在内存中
4. 客户端会把从服务端获取到的配置在本地文件系统缓存一份,在遇到服务不可用，或网络不通的时候，依然能从本地恢复配置,应用程序从Apollo客户端获取最新的配置、订阅配置更新通知
