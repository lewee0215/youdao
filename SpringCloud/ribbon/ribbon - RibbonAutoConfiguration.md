# spring-cloud-netflix-ribbon # spring.factories

```java spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration
```

## RibbonAutoConfiguration
```java
@Configuration
@ConditionalOnClass({ IClient.class, RestTemplate.class, AsyncRestTemplate.class, Ribbon.class})
@RibbonClients  // RibbonClientConfigurationRegistrar 

// @AutoConfigureBefore和@AutoConfigureAfter 两个注解 针对使用spring.factories加载的配置,由于自定义的实现类是在项目中被自动扫描后进行配置的，所以注解没有生效
@AutoConfigureAfter(name = "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration")
@AutoConfigureBefore({LoadBalancerAutoConfiguration.class, AsyncLoadBalancerAutoConfiguration.class})

@EnableConfigurationProperties({RibbonEagerLoadProperties.class, ServerIntrospectorProperties.class})
public class RibbonAutoConfiguration {

	// 为每个@RibbonClient创建一个子容器，并通过serviceId获取子容器中的IClient、ILoadBalancer、IClientConfig、RibbonLoadBalancerContext、AnnotationConfigApplicationContext 信息
	// A factory that creates client, load balancer and client configuration instances
	// It creates a Spring ApplicationContext per client name, and extracts the beans that it needs from there
	@Bean
	public SpringClientFactory springClientFactory() {
		SpringClientFactory factory = new SpringClientFactory();
		// List<RibbonClientSpecification> configurations
		factory.setConfigurations(this.configurations);
		return factory;
	}

	@Bean
	@ConditionalOnMissingBean(LoadBalancerClient.class)
	public LoadBalancerClient loadBalancerClient() {
		// RibbonLoadBalancerClient类实现了LoadBalancerClient接口(目前Spring体系中的唯一实现)
		return new RibbonLoadBalancerClient(springClientFactory());
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.retry.support.RetryTemplate")
	@ConditionalOnMissingBean
	public LoadBalancedRetryFactory loadBalancedRetryPolicyFactory(final SpringClientFactory clientFactory) {
		return new RibbonLoadBalancedRetryFactory(clientFactory);
	}

	// Ribbon Clients 饥饿加载模式
	// Ribbon Client 并不是在服务启动的时候就初始化好的，而是在调用的时候才会去创建相应的Client
	@Bean
	@ConditionalOnProperty(value = "ribbon.eager-load.enabled")
	public RibbonApplicationContextInitializer ribbonApplicationContextInitializer() {
		return new RibbonApplicationContextInitializer(springClientFactory(),
				ribbonEagerLoadProperties.getClients());
	}

	@Configuration
	@ConditionalOnClass(HttpRequest.class)
	@ConditionalOnRibbonRestClient
	protected static class RibbonClientHttpRequestFactoryConfiguration {

		@Autowired
		private SpringClientFactory springClientFactory;

		@Bean
		public RestTemplateCustomizer restTemplateCustomizer(
				final RibbonClientHttpRequestFactory ribbonClientHttpRequestFactory) {
			return restTemplate -> restTemplate.setRequestFactory(ribbonClientHttpRequestFactory);
		}

		@Bean
		public RibbonClientHttpRequestFactory ribbonClientHttpRequestFactory() {
			return new RibbonClientHttpRequestFactory(this.springClientFactory);
		}
	}

	//TODO: support for autoconfiguring restemplate to use apache http client or okhttp
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Conditional(OnRibbonRestClientCondition.class)
	@interface ConditionalOnRibbonRestClient { }

	private static class OnRibbonRestClientCondition extends AnyNestedCondition {
		public OnRibbonRestClientCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@Deprecated //remove in Edgware"
		@ConditionalOnProperty("ribbon.http.client.enabled")
		static class ZuulProperty {}

		@ConditionalOnProperty("ribbon.restclient.enabled")
		static class RibbonProperty {}
	}
}
```

### LoadBalancerClient 接口详情
```java
public class RibbonLoadBalancerClient implements LoadBalancerClient

public interface ServiceInstanceChooser {
    // 根据传入服务名serviceId,从负载均衡器中挑选一个对应服务的实例
	// 筛选策略根据 ILoadBalancer 决定, 如 ZoneAwareLoadBalancer
    ServiceInstance choose(String serviceId);
}

// LoadBalancerClient 仅实现URL重构和请求的执行
public interface LoadBalancerClient extends ServiceInstanceChooser {
    // 执行请求内容
    <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;
    <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException;

    // 返回的URI内容则是通过ServiceInstance的服务实例详情拼接出的具体host:port的请求地址
    URI reconstructURI(ServiceInstance instance, URI original);
}
```

## @RibbonClients 方法实现
注册 RibbonClientSpecification

```java
@Configuration
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Import(RibbonClientConfigurationRegistrar.class)
public @interface RibbonClients {

	RibbonClient[] value() default {};

	Class<?>[] defaultConfiguration() default {};

}

// RibbonClientConfigurationRegistrar#registerBeanDefinitions
private void registerClientConfiguration(BeanDefinitionRegistry registry,Object name, Object configuration) {
	BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RibbonClientSpecification.class);
	builder.addConstructorArgValue(name);
	builder.addConstructorArgValue(configuration);
	registry.registerBeanDefinition(name + ".RibbonClientSpecification",builder.getBeanDefinition());
}
```

