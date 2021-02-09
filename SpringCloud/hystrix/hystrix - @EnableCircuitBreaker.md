# @EnableCircuitBreaker
标识注解
``` java 
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class EnableCircuitBreakerImportSelector extends SpringFactoryImportSelector<EnableCircuitBreaker> {

	@Override
	protected boolean isEnabled() {
		return getEnvironment().getProperty(
				"spring.cloud.circuit.breaker.enabled", Boolean.class, Boolean.TRUE);
	}
}
```