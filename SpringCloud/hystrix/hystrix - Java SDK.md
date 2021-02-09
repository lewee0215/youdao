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

@Aspect
public class HystrixCommandAspect{
	// 指定 HystrixCommand 解析工厂类
	private static final Map<HystrixPointcutType, MetaHolderFactory> META_HOLDER_FACTORY_MAP;
    static {
        META_HOLDER_FACTORY_MAP = ImmutableMap.<HystrixPointcutType, MetaHolderFactory>builder()
                .put(HystrixPointcutType.COMMAND, new CommandMetaHolderFactory())
                .put(HystrixPointcutType.COLLAPSER, new CollapserMetaHolderFactory())
                .build();
    }

    @Pointcut("@annotation(com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand)")
    public void hystrixCommandAnnotationPointcut() {
    }

    @Pointcut("@annotation(com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser)")
    public void hystrixCollapserAnnotationPointcut() {
    }

    @Around("hystrixCommandAnnotationPointcut() || hystrixCollapserAnnotationPointcut()")
    public Object methodsAnnotatedWithHystrixCommand(final ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethodFromTarget(joinPoint);
        Validate.notNull(method, "failed to get method from joinPoint: %s", joinPoint);
        if (method.isAnnotationPresent(HystrixCommand.class) && method.isAnnotationPresent(HystrixCollapser.class)) {
            throw new IllegalStateException("method cannot be annotated with HystrixCommand and HystrixCollapser " +
                    "annotations at the same time");
        }

		// 判断注解类型 HystrixCommand || HystrixCollapser
        MetaHolderFactory metaHolderFactory = META_HOLDER_FACTORY_MAP.get(HystrixPointcutType.of(method));

		// 指定 Fallback 方法和 从注解上获取默认的配置信息 DefaultProperties
        MetaHolder metaHolder = metaHolderFactory.create(joinPoint);

		// 包装为 HystrixInvokable
        HystrixInvokable invokable = HystrixCommandFactory.getInstance().create(metaHolder);
        ExecutionType executionType = metaHolder.isCollapserAnnotationPresent() ?
                metaHolder.getCollapserExecutionType() : metaHolder.getExecutionType();

		// 隔离执行 HystrixCommand 方法调用
        Object result;
        try {
            if (!metaHolder.isObservable()) {
                result = CommandExecutor.execute(invokable, executionType, metaHolder);
            } else {
                result = executeObservable(invokable, executionType, metaHolder);
            }
        } catch (HystrixBadRequestException e) {
            throw e.getCause() != null ? e.getCause() : e;
        } catch (HystrixRuntimeException e) {
            throw hystrixRuntimeExceptionToThrowable(metaHolder, e);
        }
        return result;
    }
}
```

## com.netflix.hystrix.contrib.javanica.command.CommandExecutor
```java
public class CommandExecutor {
	// Calls a method of {@link HystrixExecutable} in accordance with specified execution type
	public static Object execute(HystrixInvokable invokable, ExecutionType executionType, MetaHolder metaHolder) throws RuntimeException{
		Validate.notNull(invokable);
        Validate.notNull(metaHolder);

        switch (executionType) {
            case SYNCHRONOUS: {
                return castToExecutable(invokable, executionType).execute();
            }
            case ASYNCHRONOUS: {
                HystrixExecutable executable = castToExecutable(invokable, executionType);
                if (metaHolder.hasFallbackMethodCommand()
                        && ExecutionType.ASYNCHRONOUS == metaHolder.getFallbackExecutionType()) {
                    return new FutureDecorator(executable.queue());
                }
                return executable.queue();
            }
            case OBSERVABLE: {
                HystrixObservable observable = castToObservable(invokable);
                return ObservableExecutionMode.EAGER == metaHolder.getObservableExecutionMode() ? observable.observe() : observable.toObservable();
            }
            default:
                throw new RuntimeException("unsupported execution type: " + executionType);
        }
	}

	public R execute() {
        try {
            return queue().get();
        } catch (Exception e) {
            throw Exceptions.sneakyThrow(decomposeException(e));
        }
    }

	public Future<R> queue() {
        /*
         * The Future returned by Observable.toBlocking().toFuture() does not implement the
         * interruption of the execution thread when the "mayInterrupt" flag of Future.cancel(boolean) is set to true;
         * thus, to comply with the contract of Future, we must wrap around it.
         */
        final Future<R> delegate = toObservable().toBlocking().toFuture();
    	
        final Future<R> f = new Future<R>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (delegate.isCancelled()) {
                    return false;
                }

                if (HystrixCommand.this.getProperties().executionIsolationThreadInterruptOnFutureCancel().get()) {
                    /*
                     * The only valid transition here is false -> true. If there are two futures, say f1 and f2, created by this command
                     * (which is super-weird, but has never been prohibited), and calls to f1.cancel(true) and to f2.cancel(false) are
                     * issued by different threads, it's unclear about what value would be used by the time mayInterruptOnCancel is checked.
                     * The most consistent way to deal with this scenario is to say that if *any* cancellation is invoked with interruption,
                     * than that interruption request cannot be taken back.
                     */
                    interruptOnFutureCancel.compareAndSet(false, mayInterruptIfRunning);
        		}

                final boolean res = delegate.cancel(interruptOnFutureCancel.get());

                if (!isExecutionComplete() && interruptOnFutureCancel.get()) {
                    final Thread t = executionThread.get();
                    if (t != null && !t.equals(Thread.currentThread())) {
                        t.interrupt();
                    }
                }

                return res;
			}

            @Override
            public boolean isCancelled() {
                return delegate.isCancelled();
			}

            @Override
            public boolean isDone() {
                return delegate.isDone();
			}

            @Override
            public R get() throws InterruptedException, ExecutionException {
                return delegate.get();
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return delegate.get(timeout, unit);
            }
        	
        };

        /* special handling of error states that throw immediately */
        if (f.isDone()) {
            try {
                f.get();
                return f;
            } catch (Exception e) {
                Throwable t = decomposeException(e);
                if (t instanceof HystrixBadRequestException) {
                    return f;
                } else if (t instanceof HystrixRuntimeException) {
                    HystrixRuntimeException hre = (HystrixRuntimeException) t;
                    switch (hre.getFailureType()) {
					case COMMAND_EXCEPTION:
					case TIMEOUT:
						// we don't throw these types from queue() only from queue().get() as they are execution errors
						return f;
					default:
						// these are errors we throw from queue() as they as rejection type errors
						throw hre;
					}
                } else {
                    throw Exceptions.sneakyThrow(t);
                }
            }
        }

        return f;
    }
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