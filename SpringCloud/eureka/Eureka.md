# Eureka 架构图
![](https://img-blog.csdn.net/20180814214831727?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dhdGVyc29u/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## 架构说明
https://blog.csdn.net/waterson/article/details/81675259  
Application Client发起远程调用的时候优先调用本区域内的Application Service; 如果本区内没有可用的Application Service，才会发起对其他区内的Service调用

# Eureka 数据一致性
* Eureka 是弱数据一致性，选择了 CAP 中的 AP。
* Eureka 采用 Peer to Peer 模式进行数据复制。  
  > Eureka Server 在执行复制操作的时候，使用 HEADER_REPLICATION 这个 http header 来区分普通应用实例的正常请求，说明这是一个复制请求  
  其他 peer 节点收到请求时，就不会再对其进行复制操作，从而避免死循环

* Eureka 通过 lastDirtyTimestamp 来解决复制冲突。
* Eureka 通过心跳机制实现数据修复

# Eureka 基础概念
https://blog.csdn.net/forezp/article/details/73017664  

## Register：服务注册
当Eureka客户端向Eureka Server注册时，它提供自身的元数据，比如IP地址、端口，运行状况指示符URL，主页等。

## Renew：服务续约（默认：30s）
Eureka客户会每隔30秒发送一次心跳来续约。 通过续约来告知Eureka Server该Eureka客户仍然存在，没有出现问题。 正常情况下，如果Eureka Server在90秒没有收到Eureka客户的续约，它会将实例从其注册表中删除。 建议不要更改续约间隔。

## Fetch Registries：获取注册列表信息
Eureka客户端从服务器获取注册表信息，并将其缓存在本地。客户端会使用该信息查找其他服务，从而进行远程调用。该注册列表信息定期（每30秒钟）更新一次。每次返回注册列表信息可能与Eureka客户端的缓存信息不同， Eureka客户端自动处理。如果由于某种原因导致注册列表信息不能及时匹配，Eureka客户端则会重新获取整个注册表信息。 Eureka服务器缓存注册列表信息，整个注册表以及每个应用程序的信息进行了压缩，压缩内容和没有压缩的内容完全相同。Eureka客户端和Eureka 服务器可以使用JSON / XML格式进行通讯。在默认的情况下Eureka客户端使用压缩JSON格式来获取注册列表的信息。

## Cancel：服务下线
Eureka客户端在程序关闭时向Eureka服务器发送取消请求。 发送请求后，该客户端实例信息将从服务器的实例注册表中删除。该下线请求不会自动完成，它需要调用以下内容：
DiscoveryManager.getInstance().shutdownComponent()；

## Eviction 服务剔除
在默认的情况下，当Eureka客户端连续90秒没有向Eureka服务器发送服务续约，即心跳，Eureka服务器会将该服务实例从服务注册列表删除，即服务剔除

# Eureka 的自我保护模式
