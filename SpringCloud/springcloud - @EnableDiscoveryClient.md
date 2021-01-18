# @EnableDiscoveryClient
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EnableDiscoveryClientImportSelector.class)
public @interface EnableDiscoveryClient {

}

// 标识注解,设置 spring.cloud.discovery.enabled = true
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class EnableDiscoveryClientImportSelector
		extends SpringFactoryImportSelector<EnableDiscoveryClient> {

	@Override
	protected boolean isEnabled() {
		return new RelaxedPropertyResolver(getEnvironment()).getProperty(
				"spring.cloud.discovery.enabled", Boolean.class, Boolean.TRUE);
	}

	@Override
	protected boolean hasDefaultFactory() {
		return true;
	}

}
```