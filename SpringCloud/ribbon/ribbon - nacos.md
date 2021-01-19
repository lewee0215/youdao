# RibbonNacosAutoConfiguration
https://zhuanlan.zhihu.com/p/28547575  

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-alibaba-nacos-discovery</artifactId>
    </dependency>
</dependencies>
```

## 依赖Jar: spring-cloud-alibaba-nacos-discovery-0.0.2.RELEASE.jar
```xml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.cloud.alibaba.nacos.NacosDiscoveryAutoConfiguration,\
  org.springframework.cloud.alibaba.nacos.ribbon.RibbonNacosAutoConfiguration,\
  org.springframework.cloud.alibaba.nacos.endpoint.NacosDiscoveryEndpointAutoConfiguration,\
  org.springframework.cloud.alibaba.nacos.discovery.NacosDiscoveryClientAutoConfiguration
```

## AutoConfiguration 配置类详解
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

// ConditionalOnRibbonNacos 属性注解,默认启用Ribbon-Nacos
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(value = "ribbon.nacos.enabled", matchIfMissing = true)
public @interface ConditionalOnRibbonNacos {

}
```