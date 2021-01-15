### Nacos-Server 获取注册信息
对外提供的服务接口请求地址为nacos/v1/ns/instance，实现代码在nacos-naming模块下的InstanceController类

### Nacos-Server 运行模式切换
```python
# Nacos Server 运行模式切换
curl -X PUT '$NACOS_SERVER:8848/nacos/v1/ns/operator/switches?entry=serverMode&value=CP'

# 微服务的bootstrap.properties需要配置如下选择指明注册为临时/永久实例（AP模式不支持数据一致性，所以只支持服务注册的临时实例，CP模式支持服务注册的永久实例）

#false为永久实例，true表示临时实例开启，注册为临时实例
spring.cloud.nacos.discovery.ephemeral=false
```