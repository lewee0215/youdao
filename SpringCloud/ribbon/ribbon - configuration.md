## spring-cloud-netflix-ribbon # spring.factories
```java
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration
```

### RibbonAutoConfiguration
```java
@Configuration
@ConditionalOnClass({ IClient.class, RestTemplate.class, AsyncRestTemplate.class, Ribbon.class})
@RibbonClients  // RibbonClientConfigurationRegistrar 

// @AutoConfigureBefore和@AutoConfigureAfter 两个注解 针对使用spring.factories加载的配置,由于自定义的实现类是在项目中被自动扫描后进行配置的，所以注解没有生效
@AutoConfigureAfter(name = "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration")
@AutoConfigureBefore({LoadBalancerAutoConfiguration.class, AsyncLoadBalancerAutoConfiguration.class})

@EnableConfigurationProperties({RibbonEagerLoadProperties.class, ServerIntrospectorProperties.class})
public class RibbonAutoConfiguration {

	@Bean
	public SpringClientFactory springClientFactory() {
		SpringClientFactory factory = new SpringClientFactory();
		factory.setConfigurations(this.configurations);
		return factory;
	}

	@Bean
	@ConditionalOnMissingBean(LoadBalancerClient.class)
	public LoadBalancerClient loadBalancerClient() {
		return new RibbonLoadBalancerClient(springClientFactory());
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.retry.support.RetryTemplate")
	@ConditionalOnMissingBean
	public LoadBalancedRetryFactory loadBalancedRetryPolicyFactory(final SpringClientFactory clientFactory) {
		return new RibbonLoadBalancedRetryFactory(clientFactory);
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
##　@AutoConfigureBefore & @AutoConfigureAfter 实现原理
https://mingyang.blog.csdn.net/article/details/110448731  
EnableAutoConfiguration 加载实现类 AutoConfigurationImportSelector 
```java
// 自动化配置类加载顺序实现: AutoConfigurationImportSelector.AutoConfigurationGroup#sortAutoConfigurations
public class AutoConfigurationImportSelector
		implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware,
		BeanFactoryAware, EnvironmentAware, Ordered {
	...

	//autoConfigurationMetadata：自动化配置注解元数据对象，是PropertiesAutoConfigurationMetadata的实例对象
	private List<String> sortAutoConfigurations(Set<String> configurations,AutoConfigurationMetadata autoConfigurationMetadata) {
		//此处调用实际排序的方法 getInPriorityOrder
		//MetadataReaderFactory是MetadataReader元数据读取器工厂类
		return new AutoConfigurationSorter(getMetadataReaderFactory(), autoConfigurationMetadata).getInPriorityOrder(configurations);
	}

	public List<String> getInPriorityOrder(Collection<String> classNames) {
		// 读取并Check AutoConfigurationClass 可用性
		final AutoConfigurationClasses classes = new AutoConfigurationClasses(this.metadataReaderFactory, this.autoConfigurationMetadata, classNames);
		List<String> orderedClassNames = new ArrayList<String>(classNames);
		// Initially sort alphabetically
		Collections.sort(orderedClassNames);
		// Then sort by order
		Collections.sort(orderedClassNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int i1 = classes.get(o1).getOrder();
				int i2 = classes.get(o2).getOrder();
				return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
			}

		});
		// Then respect @AutoConfigureBefore @AutoConfigureAfter
		orderedClassNames = sortByAnnotation(classes, orderedClassNames);
		return orderedClassNames;
	}

	// 判定 AutoConfiguration 可用状态
	//获取自动化配置类实例对象
	AutoConfigurationClass autoConfigurationClass = new AutoConfigurationClass(className,metadataReaderFactory, autoConfigurationMetadata);
    //判定配置类是否可用
	boolean available = autoConfigurationClass.isAvailable();

}

// 判定可用的AutoConfiguration
private static class AutoConfigurationClasses{

	// 内存存储可用类信息
	private final Map<String, AutoConfigurationClass> classes = new HashMap<>();

	private void addToClasses(MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata,
				Collection<String> classNames, boolean required) {
			for (String className : classNames) {
				if (!this.classes.containsKey(className)) {
					AutoConfigurationClass autoConfigurationClass = new AutoConfigurationClass(
							className, metadataReaderFactory, autoConfigurationMetadata);
					boolean available = autoConfigurationClass.isAvailable();
					if (required || available) {
						this.classes.put(className, autoConfigurationClass);
					}
					if (available) {
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getBefore(), false);
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getAfter(), false);
					}
				}
			}
	}
}
```

## @RibbonClients 方法实现
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
```
RibbonLoadBalancerClient类实现了LoadBalancerClient接口(目前Spring体系中的唯一实现)

```java
public interface ServiceInstanceChooser {
    // 根据传入服务名serviceId,从负载均衡器中挑选一个对应服务的实例
    ServiceInstance choose(String serviceId);
}

public interface LoadBalancerClient extends ServiceInstanceChooser {
    // 执行请求内容
    <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;
    <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException;

    // 返回的URI内容则是通过ServiceInstance的服务实例详情拼接出的具体host:port的请求地址
    URI reconstructURI(ServiceInstance instance, URI original);
}
```

