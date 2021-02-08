# Ribbon支持 Nacos 权重的三种方式
https://cloud.tencent.com/developer/article/1460975

## 1.自定义 IRule
```java
@Slf4j
public class NacosWeightRandomV1Rule extends AbstractLoadBalancerRule {

    @Override
    public Server choose(Object key) {
        List<Server> servers = this.getLoadBalancer().getReachableServers();

        List<InstanceWithWeight> instanceWithWeights = servers.stream()
                .map(server -> {
                    // 注册中心只用Nacos，没同时用其他注册中心（例如Eureka），理论上不会实现
                    if (!(server instanceof NacosServer)) {
                        log.error("参数非法，server = {}", server);
                        throw new IllegalArgumentException("参数非法，不是NacosServer实例！");
                    }

                    NacosServer nacosServer = (NacosServer) server;
                    Instance instance = nacosServer.getInstance();
                    double weight = instance.getWeight();
                    return new InstanceWithWeight(server,Double.valueOf(weight).intValue());
                })
                .collect(Collectors.toList());

        Server server = this.weightRandom(instanceWithWeights);

        log.info("选中的server = {}", server);
        return server;
    }

    /**
     * 根据权重随机
     * 算法参考 https://blog.csdn.net/u011627980/article/details/79401026
     *
     * @param list 实例列表
     * @return 随机出来的结果
     */
    private Server weightRandom(List<InstanceWithWeight> list) {
        List<Server> instances = Lists.newArrayList();
        for (InstanceWithWeight instanceWithWeight : list) {
            int weight = instanceWithWeight.getWeight();
            for (int i = 0; i <= weight; i++) {
                instances.add(instanceWithWeight.getServer());
            }
        }
        int i = new Random().nextInt(instances.size());
        return instances.get(i);
    }
}
```

## 2.利用 Nacos Client - NamingService 接口负载均衡方法
```java
@Slf4j
public class NacosWeightRandomV2Rule extends AbstractLoadBalancerRule {
    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Override
    public Server choose(Object key) {
        DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
        String name = loadBalancer.getName();
        try {
            Instance instance = discoveryProperties.namingServiceInstance()
                    .selectOneHealthyInstance(name);

            log.info("选中的instance = {}", instance);

            /*
             * instance转server的逻辑参考自：
             * org.springframework.cloud.alibaba.nacos.ribbon.NacosServerList.instancesToServerList
             */
            return new NacosServer(instance);
        } catch (NacosException e) {
            log.error("发生异常", e);
            return null;
        }
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
    }
}

// com.alibaba.nacos.client.naming.NacosNamingService
public class NacosNamingService implements NamingService {
    @Override
    public Instance selectOneHealthyInstance(String serviceName) {
        return selectOneHealthyInstance(serviceName, new ArrayList<String>());
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) {
        return Balancer.RandomByWeight.selectHost(hostReactor.getServiceInfo(serviceName, StringUtils.join(clusters, ",")));
    }
}
```