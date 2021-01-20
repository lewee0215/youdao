# Ribbon-Eureka pom依赖  
com.netflix.ribbon#ribbon-eureka;2.2.0

```xml
<dependency>
  <groupId>com.netflix.ribbon</groupId>
  <artifactId>ribbon-eureka</artifactId>
  <version>2.3.0</version>
</dependency>
```

# Ribbon-Eureka 核心组件

## LegacyEurekaClientProvider 单例客户端
class LegacyEurekaClientProvider implements Provider<EurekaClient>

## NIWSDiscoveryPing
public class NIWSDiscoveryPing extends AbstractLoadBalancerPing

## DiscoveryEnabledNIWSServerList
public class DiscoveryEnabledNIWSServerList extends AbstractServerList<DiscoveryEnabledServer>

## DefaultNIWSServerListFilter
public class DefaultNIWSServerListFilter<T extends Server> extends ZoneAffinityServerListFilter<T>

## EurekaNotificationServerListUpdater
public class EurekaNotificationServerListUpdater implements ServerListUpdater
``` java
// 监听Eureka Client 的 CacheRefreshedEvent 事件后 通过 ServerList 获取最新实例
// servers = serverListImpl.getUpdatedListOfServers();
this.updateListener = new EurekaEventListener() {
    @Override
    public void onEvent(EurekaEvent event) {
        if (event instanceof CacheRefreshedEvent) {
            refreshExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        updateAction.doUpdate();
                        lastUpdated.set(System.currentTimeMillis());
                    } catch (Exception e) {
                        logger.warn("Failed to update serverList", e);
                    }
                }
            });  // fire and forget
        }
    }
};
```

# spring-cloud-netflix-eureka-client-1.2.5.RELEASE.jar
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.netflix.eureka.config.EurekaClientConfigServerAutoConfiguration,\
org.springframework.cloud.netflix.eureka.config.EurekaDiscoveryClientConfigServiceAutoConfiguration,\
org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration,\
org.springframework.cloud.netflix.ribbon.eureka.RibbonEurekaAutoConfiguration
```

```java
@Configuration
@EnableConfigurationProperties
@RibbonEurekaAutoConfiguration.ConditionalOnRibbonAndEurekaEnabled
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = EurekaRibbonClientConfiguration.class)
public class RibbonEurekaAutoConfiguration {}

@Configuration
@CommonsLog
public class EurekaRibbonClientConfiguration {

    @Bean
	@ConditionalOnMissingBean
	public IPing ribbonPing(IClientConfig config) {
		if (this.propertiesFactory.isSet(IPing.class, serviceId)) {
			return this.propertiesFactory.get(IPing.class, config, serviceId);
		}
		NIWSDiscoveryPing ping = new NIWSDiscoveryPing();
		ping.initWithNiwsConfig(config);
		return ping;
	}

    @Bean
	@ConditionalOnMissingBean
	public ServerList<?> ribbonServerList(IClientConfig config, Provider<EurekaClient> eurekaClientProvider) {
		if (this.propertiesFactory.isSet(ServerList.class, serviceId)) {
			return this.propertiesFactory.get(ServerList.class, config, serviceId);
		}
		DiscoveryEnabledNIWSServerList discoveryServerList = new DiscoveryEnabledNIWSServerList(
				config, eurekaClientProvider);
		DomainExtractingServerList serverList = new DomainExtractingServerList(
				discoveryServerList, config, this.approximateZoneFromHostname);
		return serverList;
	}
}
```

