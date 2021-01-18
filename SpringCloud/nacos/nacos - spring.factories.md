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

## NacosDiscoveryAutoConfiguration
服务注册: 遵循spring-cloud-common标准，实现 AutoServiceRegistration、ServiceRegistry、Registration
```java
@Configuration
@EnableConfigurationProperties
@ConditionalOnNacosDiscoveryEnabled
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter({ AutoServiceRegistrationConfiguration.class,
		AutoServiceRegistrationAutoConfiguration.class })
public class NacosDiscoveryAutoConfiguration {

	@Bean
	public NacosServiceRegistry nacosServiceRegistry(
			NacosDiscoveryProperties nacosDiscoveryProperties) {
		return new NacosServiceRegistry(nacosDiscoveryProperties);
	}

	@Bean
	@ConditionalOnBean(AutoServiceRegistrationProperties.class)
	public NacosRegistration nacosRegistration(
			NacosDiscoveryProperties nacosDiscoveryProperties,
			ApplicationContext context) {
		return new NacosRegistration(nacosDiscoveryProperties, context);
	}

	// 父类 AbstractAutoServiceRegistration 实现时间监听
	// @EventListener(WebServerInitializedEvent.class)
	@Bean
	@ConditionalOnBean(AutoServiceRegistrationProperties.class)
	public NacosAutoServiceRegistration nacosAutoServiceRegistration(
			NacosServiceRegistry registry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			NacosRegistration registration) {
		return new NacosAutoServiceRegistration(registry,
				autoServiceRegistrationProperties, registration);
	}
}
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

// NacosServerList # getServers()
// List<Instance> instances = discoveryProperties.namingServiceInstance().selectInstances(serviceId, true);
@Override
public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy, boolean subscribe) throws NacosException {

	ServiceInfo serviceInfo;
	if (subscribe) {
		serviceInfo = hostReactor.getServiceInfo(NamingUtils.getGroupedName(serviceName, groupName), StringUtils.join(clusters, ","));
	} else {
		serviceInfo = hostReactor.getServiceInfoDirectlyFromServer(NamingUtils.getGroupedName(serviceName, groupName), StringUtils.join(clusters, ","));
	}
	return selectInstances(serviceInfo, healthy);
}
```

## Ribbon 实现 Nacos 权重负载均衡
https://blog.csdn.net/qq_32748869/article/details/107373969  
在Nacos的控制台，可以为每一个实例配置权重，取值在0～1之间，值越大，表示这个被调用的几率越大

## Ribbon 支持 Nacos 集群配置

# Ribbon 集成 Nacos Discovery 服务
https://www.colabug.com/2020/0409/7231968/