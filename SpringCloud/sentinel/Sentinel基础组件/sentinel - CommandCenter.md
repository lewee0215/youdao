# Sentinel - CommandHandler
/META-INF/services/com.alibaba.csp.sentinel.command.CommandHandler
```java
com.alibaba.csp.sentinel.command.handler.BasicInfoCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchActiveRuleCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchClusterNodeByIdCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchClusterNodeHumanCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchJsonTreeCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchOriginCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchSimpleClusterNodeCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchSystemStatusCommandHandler
com.alibaba.csp.sentinel.command.handler.FetchTreeCommandHandler
com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler
com.alibaba.csp.sentinel.command.handler.OnOffGetCommandHandler
com.alibaba.csp.sentinel.command.handler.OnOffSetCommandHandler
com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler
com.alibaba.csp.sentinel.command.handler.VersionCommandHandler
```

## CommandCenterProvider
```java
public final class CommandCenterProvider {
    private static CommandCenter commandCenter = null;
    static {
        resolveInstance();
    }
    
    private static void resolveInstance() {
        CommandCenter resolveCommandCenter = SpiLoader.loadHighestPriorityInstance(CommandCenter.class);

        if (resolveCommandCenter == null) {
            RecordLog.warn("[CommandCenterProvider] WARN: No existing CommandCenter found");
        } else {
            commandCenter = resolveCommandCenter;
            RecordLog.info("[CommandCenterProvider] CommandCenter resolved: " + resolveCommandCenter.getClass()
                .getCanonicalName());
        }
    }
}
```

## CommandCenterInitFunc 启动流程
```java
@InitOrder(-1)
public class CommandCenterInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        CommandCenter commandCenter = CommandCenterProvider.getCommandCenter();

        if (commandCenter == null) {
            RecordLog.warn("[CommandCenterInitFunc] Cannot resolve CommandCenter");
            return;
        }

        // 注册 CommandCenterProvider 加载的所有CommandHandler
        commandCenter.beforeStart();

        // 创建线程池处理 网络Command 信息
        commandCenter.start();

        RecordLog.info("[CommandCenterInit] Starting command center: "
                + commandCenter.getClass().getCanonicalName());
    }
}

public class SimpleHttpCommandCenter implements CommandCenter {

    @Override
    @SuppressWarnings("rawtypes")
    public void beforeStart() throws Exception {
        // Register handlers
        Map<String, CommandHandler> handlers = CommandHandlerProvider.getInstance().namedHandlers();
        registerCommands(handlers);
    }
}
```