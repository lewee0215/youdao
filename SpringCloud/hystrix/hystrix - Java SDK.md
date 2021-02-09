# Hystrix 的基本使用
https://blog.csdn.net/qq_34978129/article/details/112359199

## Maven POM
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```


## spring-cloud-netflix-core-2.0.0.RELEASE
<code>
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.netflix.hystrix.HystrixAutoConfiguration,\
org.springframework.cloud.netflix.hystrix.security.HystrixSecurityAutoConfiguration

org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker=\
org.springframework.cloud.netflix.hystrix.HystrixCircuitBreakerConfiguration
</code>


### HystrixCircuitBreakerConfiguration
```java
//org.springframework.cloud.netflix.hystrix.HystrixCircuitBreakerConfiguration
@Configuration
public class HystrixCircuitBreakerConfiguration {
	@Bean
	public HystrixCommandAspect hystrixCommandAspect() {
		return new HystrixCommandAspect();
	}
    ...
}
```

## CommandExecutor

```java
public class CommandExecutor {
	// Calls a method of {@link HystrixExecutable} in accordance with specified execution type
	public static Object execute(HystrixInvokable invokable, ExecutionType executionType, MetaHolder metaHolder) throws RuntimeException{}
}

public interface HystrixExecutable<R> extends HystrixInvokable<R> {

    /**Used for synchronous execution of command*/
    public R execute();

    /**
     * Used for asynchronous execution of command.
     * <p>
     * This will queue up the command on the thread pool and return an {@link Future} to get the result once it completes.
     * <p>
     * NOTE: If configured to not run in a separate thread, this will have the same effect as {@link #execute()} and will block.
     * <p>
     * We don't throw an exception in that case but just flip to synchronous execution so code doesn't need to change in order to switch a circuit from running a separate thread to the calling thread.
     */
    public Future<R> queue();

    /**
     * Used for asynchronous execution of command with a callback by subscribing to the {@link Observable}.
     * <p>
     * This eagerly starts execution of the command the same as {@link #queue()} and {@link #execute()}.
     * A lazy {@link Observable} can be obtained from {@link HystrixCommand#toObservable()} or {@link HystrixCollapser#toObservable()}
     */
    public Observable<R> observe();

}
```