# Eureka 数据一致性
* Eureka 是弱数据一致性，选择了 CAP 中的 AP。
* Eureka 采用 Peer to Peer 模式进行数据复制。  
  > Eureka Server 在执行复制操作的时候，使用 HEADER_REPLICATION 这个 http header 来区分普通应用实例的正常请求，说明这是一个复制请求  
  其他 peer 节点收到请求时，就不会再对其进行复制操作，从而避免死循环

* Eureka 通过 lastDirtyTimestamp 来解决复制冲突。
* Eureka 通过心跳机制实现数据修复
