# Ribbon 集成 Nacos Discovery 服务
https://www.colabug.com/2020/0409/7231968/

## nacos-discovery-spring-boot-autoconfiguration-0.2.1.jar
```spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.alibaba.boot.nacos.discovery.autoconfigure.NacosDiscoveryAutoConfiguration
```

## spring-cloud-alibaba-nacos-discovery-0.2.2.RELEASE.jar
```spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.cloud.alibaba.nacos.NacosDiscoveryAutoConfiguration,\
  org.springframework.cloud.alibaba.nacos.ribbon.RibbonNacosAutoConfiguration,\
  org.springframework.cloud.alibaba.nacos.endpoint.NacosDiscoveryEndpointAutoConfiguration,\
  org.springframework.cloud.alibaba.nacos.discovery.NacosDiscoveryClientAutoConfiguration
```

## RibbonNacosAutoConfiguration
```java
@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(SpringClientFactory.class)
@ConditionalOnRibbonNacos
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = NacosRibbonClientConfiguration.class)
public class RibbonNacosAutoConfiguration {
}

@Configuration
@ConditionalOnRibbonNacos
public class NacosRibbonClientConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ServerList<?> ribbonServerList(IClientConfig config, NacosDiscoveryProperties nacosDiscoveryProperties) {
		NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
		serverList.initWithNiwsConfig(config);
		return serverList;
	}
}
```

## Ribbon 实现 Nacos 权重负载均衡
https://blog.csdn.net/qq_32748869/article/details/107373969  
在Nacos的控制台，可以为每一个实例配置权重，取值在0～1之间，值越大，表示这个被调用的几率越大

## Ribbon 支持 Nacos 集群配置