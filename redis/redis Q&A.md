1. 缓存穿透, 缓存击穿, 缓存雪崩, 热点Key重建, BigKey , 热点Key
https://www.jianshu.com/p/c7c352cb14fe

## 缓存穿透
> 缓存穿透是指缓存和数据库中都没有的数据,导致每次请求直接落到存储层

1. 缓存空对象
2. 布隆过滤器

## 缓存击穿
> 缓存击穿是指缓存中没有但数据库中有的数据（一般是缓存时间到期），这时由于并发用户特别多，同时读缓存没读到数据，又同时去数据库去取数据，引起数据库压力瞬间增大，造成过大压力

1. 分布式锁
2. 设置热点数据永远不过期

## 缓存雪崩
> 指缓存由于某些原因（比如宕机、cache服务不响应，缓存中数据大批量到期）导致整体crash，导致大量请求到达后端数据库，从而导致数据库崩溃，整个系统崩溃，发生灾难
和缓存击穿不同的是，缓存击穿指并发查同一条数据，缓存雪崩是不同数据都过期了，很多数据都查不到从而查数据库

1. 缓存数据的过期时间设置随机，防止同一时间大量数据过期现象发生。
2. 如果缓存数据库是分布式部署，将热点数据均匀分布在不同得缓存数据库中。
3. 设置热点数据永远不过期


4. 多级缓存，如Eurka注册中心的GuavaCache二级缓存等
5. 对资源服务访问进行 限流、资源隔离（熔断）、Stubbed 降级

## 热点Key重建
1. 分布式锁
2. 缓存值永不过期

## Bigkey 

## 微博大V关注内容实现