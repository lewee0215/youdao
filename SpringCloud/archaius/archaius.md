SpringCloud -Archaius
https://blog.csdn.net/yang75108/article/details/86990136

Archaius是一款非常优秀的生产级配置客户端组件，比较可惜的是Netflix没有开源它的配置中心的服务器端
在Archaius中，可以通过设置archaius.deployment.environment启动参数，来激活不同环境的配置

# Archaius + Apollo
1. 通过设置archaius.configurationSource.additionalUrls启动参数，设置为Apollo的配置文件拉取端点，激活Archaius的远程配置数据源
2. Apollo的配置文件拉取端点为：{config_server_url}/configfiles/{appId}/{clusterName}/{namespaceName}，其中：config_server_url是配置中心地址，appId是应用(例如Zuul)在Apollo中的唯一标识，clusterName是应用在Apollo中的集群名，一般用缺省default，namespaceName是应用在Apollo中的名字空间，一般用缺省applicatio
3. 对于不同的环境(TEST，UAT，PROD等)，Apollo配置中心的地址一般不同
4. Archaius动态拉取配置的周期缺省是60秒，可以调整

