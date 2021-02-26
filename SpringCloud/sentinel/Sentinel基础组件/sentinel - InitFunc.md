# Sentinel - InitFunc
/META-INF/services/com.alibaba.csp.sentinel.init.InitFunc
```java
com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc
com.alibaba.csp.sentinel.transport.init.HeartbeatSenderInitFunc
```

# InitFunc 启动流程
```java
public class Env {
    public static final Sph sph = new CtSph();
    static {
        // If init fails, the process will exit.
        InitExecutor.doInit();
    }
}

public final class InitExecutor {
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    /**
     * If one {@link InitFunc} throws an exception, the init process
     * will immediately be interrupted and the application will exit.
     *
     * The initialization will be executed only once.
     */
    public static void doInit() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        try {
            ServiceLoader<InitFunc> loader = ServiceLoader.load(InitFunc.class);
            List<OrderWrapper> initList = new ArrayList<OrderWrapper>();
            for (InitFunc initFunc : loader) {
                RecordLog.info("[InitExecutor] Found init func: " + initFunc.getClass().getCanonicalName());
                insertSorted(initList, initFunc);
            }
            for (OrderWrapper w : initList) {
                w.func.init();
                RecordLog.info(String.format("[InitExecutor] Executing %s with order %d",
                    w.func.getClass().getCanonicalName(), w.order));
            }
        } catch (Exception ex) {
            RecordLog.warn("[InitExecutor] WARN: Initialization failed", ex);
            ex.printStackTrace();
        } catch (Error error) {
            RecordLog.warn("[InitExecutor] ERROR: Initialization failed with fatal error", error);
            error.printStackTrace();
        }
    }
}
```