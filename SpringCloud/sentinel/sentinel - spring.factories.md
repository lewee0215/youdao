# Sentinel限流实现原理
https://www.cnblogs.com/wuzhenzhao/p/11453649.html

## POM 依赖
https://blog.csdn.net/weixin_47274985/article/details/107244118  
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    <version>0.2.0.RELEASE</version>
</dependency>
```

## spring-cloud-alibaba-sentinel-0.2.0.RELEASE.jar
```spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.alibaba.sentinel.SentinelWebAutoConfiguration,\
org.springframework.cloud.alibaba.sentinel.endpoint.SentinelEndpointAutoConfiguration,\
org.springframework.cloud.alibaba.sentinel.custom.SentinelAutoConfiguration

org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker=\
org.springframework.cloud.alibaba.sentinel.custom.SentinelCircuitBreakerConfiguration
```
### SentinelWebAutoConfiguration
```java
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelWebAutoConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(SentinelWebAutoConfiguration.class);

	@Autowired
	private SentinelProperties properties;

	// 注册 com.alibaba.csp.sentinel.adapter.servlet.CommonFilter 
	// 实现 URL 限流控制
	// For REST APIs, you have to clean the URL (e.g. `/foo/1` and `/foo/2` -> `/foo/:id`)
	@Bean
	public FilterRegistrationBean servletRequestListener() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
		SentinelProperties.Filter filterConfig = properties.getFilter();

		if (null == filterConfig) {
			filterConfig = new SentinelProperties.Filter();
			properties.setFilter(filterConfig);
		}

		if (filterConfig.getUrlPatterns() == null
				|| filterConfig.getUrlPatterns().isEmpty()) {
			List<String> defaultPatterns = new ArrayList<>();
			defaultPatterns.add("/*");
			filterConfig.setUrlPatterns(defaultPatterns);
		}

		registration.addUrlPatterns(filterConfig.getUrlPatterns().toArray(new String[0]));
		Filter filter = new CommonFilter();
		registration.setFilter(filter);
		registration.setOrder(filterConfig.getOrder());
		logger.info("[Sentinel Starter] register Sentinel with urlPatterns: {}.",filterConfig.getUrlPatterns());
		return registration;

	}
}
```

### SentinelEndpointAutoConfiguration
Endpoint for Sentinel, contains ans properties and rules

```java
@ConditionalOnClass(Endpoint.class)
@EnableConfigurationProperties({ SentinelProperties.class })
public class SentinelEndpointAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledEndpoint
	public SentinelEndpoint sentinelEndPoint() {
		return new SentinelEndpoint();
	}

}

@Endpoint(id = "sentinel")
public class SentinelEndpoint {

	@Autowired
	private SentinelProperties sentinelProperties;

	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>();

		List<FlowRule> flowRules = FlowRuleManager.getRules();
		List<DegradeRule> degradeRules = DegradeRuleManager.getRules();
		List<SystemRule> systemRules = SystemRuleManager.getRules();
		result.put("properties", sentinelProperties);
		result.put("FlowRules", flowRules);
		result.put("DegradeRules", degradeRules);
		result.put("SystemRules", systemRules);
		return result;
	}

}
```

### SentinelAutoConfiguration
```java
@Configuration
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelAutoConfiguration {

	@Value("${project.name:${spring.application.name:}}")
	private String projectName;

	@Autowired
	private SentinelProperties properties;

	@Autowired
	private Optional<UrlCleaner> urlCleanerOptional;

	@Autowired
	private Optional<UrlBlockHandler> urlBlockHandlerOptional;

	@PostConstruct
	private void init() {

		if (StringUtils.isEmpty(System.getProperty(AppNameUtil.APP_NAME))&& StringUtils.hasText(projectName)) {
			System.setProperty(AppNameUtil.APP_NAME, projectName);
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.SERVER_PORT))&& StringUtils.hasText(properties.getTransport().getPort())) {
			System.setProperty(TransportConfig.SERVER_PORT,properties.getTransport().getPort());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))&& StringUtils.hasText(properties.getTransport().getDashboard())) {
			System.setProperty(TransportConfig.CONSOLE_SERVER,properties.getTransport().getDashboard());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.HEARTBEAT_INTERVAL_MS))&& StringUtils.hasText(properties.getTransport().getHeartbeatIntervalMs())) {
			System.setProperty(TransportConfig.HEARTBEAT_INTERVAL_MS,properties.getTransport().getHeartbeatIntervalMs());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.CHARSET))&& StringUtils.hasText(properties.getCharset())) {
			System.setProperty(SentinelConfig.CHARSET, properties.getCharset());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.SINGLE_METRIC_FILE_SIZE))&& StringUtils.hasText(properties.getMetric().getFileSingleSize())) {
			System.setProperty(SentinelConfig.SINGLE_METRIC_FILE_SIZE,properties.getMetric().getFileSingleSize());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.TOTAL_METRIC_FILE_COUNT))&& StringUtils.hasText(properties.getMetric().getFileTotalCount())) {
			System.setProperty(SentinelConfig.TOTAL_METRIC_FILE_COUNT,properties.getMetric().getFileTotalCount());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.COLD_FACTOR))&& StringUtils.hasText(properties.getFlow().getColdFactor())) {
			System.setProperty(SentinelConfig.COLD_FACTOR,properties.getFlow().getColdFactor());
		}
		if (StringUtils.hasText(properties.getServlet().getBlockPage())) {
			WebServletConfig.setBlockPage(properties.getServlet().getBlockPage());
		}

		// DefaultUrlBlockHandler
		urlBlockHandlerOptional.ifPresent(WebCallbackManager::setUrlBlockHandler);
		// DefaultUrlCleaner
		urlCleanerOptional.ifPresent(WebCallbackManager::setUrlCleaner);

		// earlier initialize
		if (properties.isEager()) {
			InitExecutor.doInit();
		}
	}

	// 实现 @SentinelResource 注解功能
	@Bean
	@ConditionalOnMissingBean
	public SentinelResourceAspect sentinelResourceAspect() {
		return new SentinelResourceAspect();
	}

	// 实现出站流控
	// 提供 SentinelProtectInterceptor 类型 RestTemplate 拦截器
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
	public SentinelBeanPostProcessor sentinelBeanPostProcessor() {
		return new SentinelBeanPostProcessor();
	}

	// find all fields using by @SentinelDataSource annotation
	// @SentinelDataSource An annotation to inject {@link ReadableDataSource} instance into a Spring Bean
	@Bean
	@ConditionalOnMissingBean
	public SentinelDataSourcePostProcessor sentinelDataSourcePostProcessor() {
		return new SentinelDataSourcePostProcessor();
	}
}
```


