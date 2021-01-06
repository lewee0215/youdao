# 服务注册
在Spring云应用程序的启动阶段，将监视WebServerInitializedEvent事件。在初始化Web容器后收到WebServerInitializedEvent事件时，将触发注册操作，并调用ServiceRegistry注册方法以将服务注册到Nacos Server
## nacos-discovery-spring-boot-starter pom.xml依赖
```java
<!-- Nacos -->
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-spring-context</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-discovery-spring-boot-autoconfigure</artifactId>
</dependency>
/** SPI文件: spring.facoties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.alibaba.boot.nacos.discovery.autoconfigure.NacosDiscoveryAutoConfiguration
*/

<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-spring-boot-base</artifactId>
</dependency>
```

Nacos SpringBoot 启动源代码
```java
// NacosDiscoveryAutoConfiguration
@ConditionalOnProperty(name = NacosDiscoveryConstants.ENABLED, matchIfMissing = true)
@ConditionalOnMissingBean(name = DISCOVERY_GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
@EnableNacosDiscovery
@EnableConfigurationProperties(value = NacosDiscoveryProperties.class)
@ConditionalOnClass(name = "org.springframework.boot.context.properties.bind.Binder")
public class NacosDiscoveryAutoConfiguration {

}

// EnableNacosDiscovery
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NacosDiscoveryBeanDefinitionRegistrar.class)
public @interface EnableNacosDiscovery {}

// NacosDiscoveryBeanDefinitionRegistrar
public class NacosDiscoveryBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableNacosDiscovery.class.getName()));
        // Register Global Nacos Properties Bean
        registerGlobalNacosProperties(attributes, registry, environment, DISCOVERY_GLOBAL_NACOS_PROPERTIES_BEAN_NAME);
        // Register Nacos Common Beans
        // CacheableEventPublishingNacosServiceFactory.class
        // AnnotationNacosInjectedBeanPostProcessor.class
        registerNacosCommonBeans(registry);
        // Register Nacos Discovery Beans (NamingServiceBeanBuilder.class)
        registerNacosDiscoveryBeans(registry);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

```
## nacos - springcloud

# 服务发现
NacosServerList实现com.netflix.loadbalancer.ServerList接口并在@ConditionOnMissingBean下自动注入它
> Nacos Discovery Starter默认集成了Ribbon，因此对于使用Ribbon进行负载平衡的组件，可以直接使用Nacos服务发现

## nacos - springboot


## nacos - springcloud