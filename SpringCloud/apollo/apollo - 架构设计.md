# Apollo - Server 架构演进
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