## Ribbon 使用简介
Ribbon 负载均衡默认使用的策略是 ZoneAvoidanceRule

## Ribbon 主要组件及默认值 (RibbonClientConfiguration)
https://blog.csdn.net/alex_xfboy/article/details/88166216  

| 组件名称              | 默认值                        | 使用说明 |
| :-                   | :-                            |    :-:   |
| IRule                | ZoneAvoidanceRule             | 负载均衡策略         |
| IPing                | DummyPing                     | 服务器健康状态监测方式        |
| ServerList           | ConfigurationBasedServerList  | 定义了获取服务器的列表接口，存储服务列表       |
| ServerListFilter     | ZonePreferenceServerListFilter| 接口使用不同的方法来做动态更新服务器列表        |
| ServerListUpdater    | PollingServerListUpdater      | 使用不同的方法来做动态更新服务器列表       |
| IClientConfig        | DefaultClientConfigImpl       | 用于对客户端或者负载均衡的配置，用来初始化ribbon客户端|
| ILoadBalancer        | ZoneAwareLoadBalancer         | 定义了各种软负载，动态更新一组服务列表及根据指定算法从现有服务器列表中选择一个服务       |

# Ribbon - IRule 负载规则
https://www.cnblogs.com/fx-blog/p/11713872.html

```java
public interface IRule{
    /*
     * choose one alive server from lb.allServers or
     * lb.upServers according to key
     * 
     * @return choosen Server object. NULL is returned if none
     *  server is available 
     */
    public Server choose(Object key);
    
    public void setLoadBalancer(ILoadBalancer lb);
    
    public ILoadBalancer getLoadBalancer();    
}
```

| P规则类型                     | 说明    | 
| :-                           |:-:        | 
| AvailabilityFilteringRule    | 过滤掉一直连接失败的被标记为circuit tripped（电路跳闸）的后端Service，并过滤掉那些高并发的后端Server或者使用一个AvailabilityPredicate来包含过滤Server的逻辑，其实就是检查status的记录的各个Server的运行状态     | 
| BestAvailableRule            | 选择一个最小的并发请求的Server，逐个考察Server，如果Server被tripped了，则跳过      |
| RandomRule                   | 随机选择一个Server     |
| ResponseTimeWeightedRule     | 已废弃，作用同WeightedResponseTimeRule        |
| RetryRule                    | 对选定的负责均衡策略机上充值机制，在一个配置时间段内当选择Server不成功，则一直尝试使用subRule的方式选择一个可用的Server  |
| RoundRobinRule               | 轮询选择，轮询index，选择index对应位置Server  |
| WeightedResponseTimeRule     | 根据相应时间加权，相应时间越长，权重越小，被选中的可能性越低  |
| ZoneAvoidanceRule            | （默认是这个）负责判断Server所Zone的性能和Server的可用性选择Server，在没有Zone的环境下，类似于轮询（RoundRobinRule） |

# Ribbon - IPing 健康检查
https://www.cnblogs.com/li3807/p/8889612.html  
如果单独使用 Ribbon 默认情况下不会激活 Ping 机制，默认的实现类为 DummyPing（不验证）

IPing 是 Ribbon 框架中，负责检查服务实例是否存活（UP）
https://blog.csdn.net/weixin_34148340/article/details/93079295 

```java
public interface IPing {
    
    /**
     * Checks whether the given <code>Server</code> is "alive" i.e. should be
     * considered a candidate while loadbalancing
     * 
     */
    public boolean isAlive(Server server);

    // PingUrl 测试Code
    // false = 是否Https请求
    public static void main(String[] args){
        PingUrl p = new PingUrl(false,"/cs/hostRunning");
        p.setExpectedContent("true");
        Server s = new Server("ec2-75-101-231-85.compute-1.amazonaws.com", 7101);
        
        boolean isAlive = p.isAlive(s);
        System.out.println("isAlive:" + isAlive);
    }
}
```

| Ping类型       | 说明    | 
| :-             |:-:        | 
| DummyPing      | 虚设的IPing实现，永远返回true      | 
| NoOpPing       | 什么也不做，直接返回true      |
| PingConstant   | 一个工具类IPing实现，只要常量参数为true，则表示服务存活     |
| PingUrl        | 通过request访问服务返回的状态码来判定服务是否存活        |
| NIWSDiscoveryPing | 通过Eureka来判定服务实例是否存活  |

# Ribbon - ServerList 存储服务实例
https://blog.csdn.net/alex_xfboy/article/details/88166216  
服务列表分为静态和动态。如果是动态的，后台有个线程会定时刷新和过滤服务列表
默认实现包含 ConfigurationBasedServerList, NacosServerList ,DiscoveryEnabledNIWSServerList等

```java
com.netflix.loadbalancer.ConfigurationBasedServerList 

// 从配置文件中获取所有服务列表，比如：
// kxtx-oms.ribbon.listOfServers=www.microsoft.com:80,www.yahoo.com:80,www.google.com:80

com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList 
// 从Eureka Client中获取服务列表。此值必须通过属性中的VipAddress来标识服务器集群

org.springframework.cloud.netflix.ribbon.eureka.DomainExtractingServerList
// 代理类，根据ServerList的值实现具体的逻辑
```

# Ribbon - ServerListFilter 过滤组件
https://blog.csdn.net/alex_xfboy/article/details/88166216  
ServerListFilter是DynamicServerListLoadBalancer用于过滤从ServerList实现返回的服务器的组件

> ZoneAffinityServerListFilter 

过滤掉所有的不和客户端在相同zone的服务，如果和客户端相同的zone不存在，才不过滤不同zone有服务。启用此配置使用：
<pre>
kxtx-oms.ribbon.EnableZoneAffinity=true
</pre>

> ZonePreferenceServerListFilter

ZoneAffinityServerListFilter的子类，但是比较的zone是发布环境里面的zone。  
过滤掉所有和客户端环境里的配置的zone的不同的服务，如果和客户端相同的zone不存在，才不进行过滤

> ServerListSubsetFilter

ZoneAffinityServerListFilter的子类，确保客户端仅看到由ServerList实现返回的整个服务器的固定子集。 它还可以定期用新服务器替代可用性差的子集中的服务器

```properties
# 选择ServerList获取模式
kxtx-oms.ribbon.NIWSServerListClassName=com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList 

# the server must register itself with Eureka server with VipAddress "myservice"
kxtx-oms.ribbon.DeploymentContextBasedVipAddresses=myservice
kxtx-oms.ribbon.NIWSServerListFilterClassName=com.netflix.loadbalancer.ServerListSubsetFilter

# only show client 5 servers. default is 20.
kxtx-oms.ribbon.ServerListSubsetFilter.size=5
```

# Ribbon - ServerListUpdater 更新器
被DynamicServerListLoadBalancer用于动态的更新服务列表。

> PollingServerListUpdater  

默认的实现策略。此对象会启动一个定时线程池，定时执行更新策略

> EurekaNotificationServerListUpdater  

当收到缓存刷新的通知，会更新服务列表

# Ribbon - ILoadBalancer 负载均衡器
https://mp.weixin.qq.com/s/ganchvrJRwzE8ph20psXqA  

![alt text](https://img-blog.csdn.net/20170913083353943 "title")

```java
public interface ILoadBalancer {
    // 向负载均衡器中添加服务实例
    public void addServers(List<Server> newServers);

    // 根据负载均衡策略，从负载均衡器中挑选出一个服务实例
    public Server chooseServer(Object key);

    // 下线负载均衡器中的某个具体实例
    public void markServerDown(Server server);

    // 返回当前可正常服务的实例列表
    public List<Server> getReachableServers();

    // 返回所有的服务实例列表
    public List<Server> getAllServers();
}

// BaseLoadBalancer 是负载均衡器的基础实现类，这个类对于接口ILoadBalancer的所有方法都给予了基础的实现
// DynamicServerListLoadBalancer 是 BaseLoadBalancer 子类, 扩展功能（1）服务实例运行期间的动态更新 （2）服务实例的过滤
// ZoneAwareLoadBalancer 则是对 DynamicServerListLoadBalancer 的扩展，它主要增加了区域过滤的功能
```
> DynamicServerListLoadBalancer

DynamicServerListLoadBalancer组合Rule、IPing、ServerList、ServerListFilter、ServerListUpdater 实现类，实现动态更新和过滤更新服务列表

> ZoneAwareLoadBalancer

DynamicServerListLoadBalancer的子类，主要加入zone的因素。统计每个zone的平均请求的情况，保证从所有zone选取对当前客户端服务最好的服务组列表

```java
public class ZoneAwareLoadBalancer<T extends Server> extends DynamicServerListLoadBalancer<T> {

    @Override
    public Server chooseServer(Object key) {
        ....

        // 获取可用的服务区 Zone
        Set<String> availableZones = ZoneAvoidanceRule.getAvailableZones(zoneSnapshot, triggeringLoad.get(), triggeringBlackoutPercentage.get());
        logger.debug("Available zones: {}", availableZones);

        if (availableZones != null &&  availableZones.size() < zoneSnapshot.keySet().size()) {

            // 随机挑选可用的服务区 Zone
            String zone = ZoneAvoidanceRule.randomChooseZone(zoneSnapshot, availableZones);
            logger.debug("Zone chosen: {}", zone);
            if (zone != null) {
                BaseLoadBalancer zoneLoadBalancer = getLoadBalancer(zone);

                // BaseLoadBalancer.chooseServer(key) 轮询获取可用实例
                server = zoneLoadBalancer.chooseServer(key);
            }
        }

        ...
    }
}
```