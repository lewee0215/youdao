# Ribbon @LoadBalanced 详解
https://blog.csdn.net/liuccc1/article/details/88905023
```java
/**
 * Annotation to mark a RestTemplate bean to be configured to use a LoadBalancerClient
 * @author Spencer Gibb
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
public @interface LoadBalanced {
}

```
// public interface LoadBalancerClient extends ServiceInstanceChooser  
LoadBalancerClient 是实现负载均衡的基础

RibbonLoadBalancerClient 是在 RibbonAutoConfiguration 类注入到 spring 容器中的(目前Spring体系中的唯一实现)

```java
@Configuration
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(LoadBalancerClient.class)
@EnableConfigurationProperties(LoadBalancerRetryProperties.class)
public class LoadBalancerAutoConfiguration {

	@LoadBalanced  // 获取所有使用LoadBalanced注解标记的 RestTemplate 的对象
	@Autowired(required = false)
	private List<RestTemplate> restTemplates = Collections.emptyList();

    ...

    @Configuration
	@ConditionalOnMissingClass("org.springframework.retry.support.RetryTemplate")
	static class LoadBalancerInterceptorConfig {
		@Bean
		public LoadBalancerInterceptor ribbonInterceptor(
				LoadBalancerClient loadBalancerClient,
				LoadBalancerRequestFactory requestFactory) {
			return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
		}

		@Bean
		@ConditionalOnMissingBean
		public RestTemplateCustomizer restTemplateCustomizer(
				final LoadBalancerInterceptor loadBalancerInterceptor) {
			return restTemplate -> {
                List<ClientHttpRequestInterceptor> list = new ArrayList<>(
                        restTemplate.getInterceptors());
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            };
		}
	}
}
```
LoadBalancerInterceptor 是个 ClientHttpRequestInterceptor 客户端请求拦截器。  作用是在客户端发起请求之前拦截，进而实现客户端的负载均衡