# Sentinel-core-1.5.2.jar
# Default slot chain builder
com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder

## DefaultSlotChainBuilder#build
https://blog.csdn.net/prestigeding/article/details/103842382
```java
public class DefaultSlotChainBuilder implements SlotChainBuilder {
    public ProcessorSlotChain build() {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();
        chain.addLast(new NodeSelectorSlot());   //主要用于构建调用链
        chain.addLast(new ClusterBuilderSlot()); //用于集群限流、熔断
        chain.addLast(new LogSlot());            //用于记录日志
        chain.addLast(new StatisticSlot());      //用于实时收集实时消息
        chain.addLast(new AuthoritySlot());      //用于权限校验的
        chain.addLast(new SystemSlot());         //用于验证系统级别的规则
        chain.addLast(new FlowSlot());           //实现限流机制
        chain.addLast(new DegradeSlot());        //实现熔断机制
        return chain;
    }
}
```

## SlotChain 调用流程 SphU.entry(resource); 
```java
Env.sph.entry(name, type, 1, OBJECTS0); // 方法入口

public class CtSph implements Sph {
    private Entry entryWithPriority(ResourceWrapper resourceWrapper, int count, boolean prioritized, Object... args)
        throws BlockException {
        Context context = ContextUtil.getContext();
        if (context instanceof NullContext) {
            // The {@link NullContext} indicates that the amount of context has exceeded the threshold,
            // so here init the entry only. No rule checking will be done.
            return new CtEntry(resourceWrapper, null, context);
        }

        if (context == null) {
            // Using default context.
            context = MyContextUtil.myEnter(Constants.CONTEXT_DEFAULT_NAME, "", resourceWrapper.getType());
        }

        // Global switch is close, no rule checking will do.
        if (!Constants.ON) {
            return new CtEntry(resourceWrapper, null, context);
        }

        ProcessorSlot<Object> chain = lookProcessChain(resourceWrapper);

        /*
            * Means amount of resources (slot chain) exceeds {@link Constants.MAX_SLOT_CHAIN_SIZE},
            * so no rule checking will be done.
            */
        if (chain == null) {
            return new CtEntry(resourceWrapper, null, context);
        }

        Entry e = new CtEntry(resourceWrapper, chain, context);
        try {
            chain.entry(context, resourceWrapper, null, count, prioritized, args);
        } catch (BlockException e1) {
            e.exit(count, args);
            throw e1;
        } catch (Throwable e1) {
            // This should not happen, unless there are errors existing in Sentinel internal.
            RecordLog.info("Sentinel unexpected exception", e1);
        }
        return e;
    }

    ProcessorSlotChain chain = chainMap.get(resourceWrapper);
        if (chain == null) {
            synchronized (LOCK) {
                chain = chainMap.get(resourceWrapper);
                if (chain == null) {
                    // Entry size limit.
                    if (chainMap.size() >= Constants.MAX_SLOT_CHAIN_SIZE) {
                        return null;
                    }

                    chain = SlotChainProvider.newSlotChain();
                    Map<ResourceWrapper, ProcessorSlotChain> newMap = new HashMap<ResourceWrapper, ProcessorSlotChain>(
                        chainMap.size() + 1);
                    newMap.putAll(chainMap);
                    newMap.put(resourceWrapper, chain);
                    chainMap = newMap;
                }
            }
        }
        return chain;
    }
}

// SlotChainProvider.newSlotChain
public final class SlotChainProvider {
    private static final ServiceLoader<SlotChainBuilder> LOADER = ServiceLoader.load(SlotChainBuilder.class);
    /**
     * The load and pick process is not thread-safe, but it's okay since the method should be only invoked
     * via {@code lookProcessChain} in {@link com.alibaba.csp.sentinel.CtSph} under lock.
     *
     * @return new created slot chain
     */
    public static ProcessorSlotChain newSlotChain() {
        if (builder != null) {
            return builder.build();
        }
        resolveSlotChainBuilder();
        if (builder == null) {
            RecordLog.warn("[SlotChainProvider] Wrong state when resolving slot chain builder, using default");
            builder = new DefaultSlotChainBuilder();
        }
        return builder.build();
    }

    // 默认 DefaultSlotChainBuilder
    private static void resolveSlotChainBuilder() {
        List<SlotChainBuilder> list = new ArrayList<SlotChainBuilder>();
        boolean hasOther = false;
        for (SlotChainBuilder builder : LOADER) {
            if (builder.getClass() != DefaultSlotChainBuilder.class) {
                hasOther = true;
                list.add(builder);
            }
        }
        if (hasOther) {
            builder = list.get(0);
        } else {
            // No custom builder, using default.
            builder = new DefaultSlotChainBuilder();
        }

        RecordLog.info("[SlotChainProvider] Global slot chain builder resolved: "
            + builder.getClass().getCanonicalName());
    }
}
```

```java
public class SystemSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        // 规则校验
        SystemRuleManager.checkSystem(resourceWrapper);
        // 链式处理进入下一个Slot
        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }
}
```