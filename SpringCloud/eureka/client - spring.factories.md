# Eureka - Client 启动流程 spring.factories
```yaml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.netflix.eureka.config.EurekaClientConfigServerAutoConfiguration,\
org.springframework.cloud.netflix.eureka.config.EurekaDiscoveryClientConfigServiceAutoConfiguration,\
org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration,\
org.springframework.cloud.netflix.ribbon.eureka.RibbonEurekaAutoConfiguration

org.springframework.cloud.bootstrap.BootstrapConfiguration=\
org.springframework.cloud.netflix.eureka.config.EurekaDiscoveryClientConfigServiceBootstrapConfiguration

org.springframework.cloud.client.discovery.EnableDiscoveryClient=\
org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration
```

## EurekaDiscoveryClientConfiguration
org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration
```java
public class EurekaDiscoveryClientConfiguration implements SmartLifecycle, Ordered{

}
```

## ConfigServicePropertySourceLocator
org.springframework.cloud.config.client.ConfigServicePropertySourceLocator