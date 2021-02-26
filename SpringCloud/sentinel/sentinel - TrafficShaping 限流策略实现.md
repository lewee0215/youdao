# Sentinel限流
https://www.daqianduan.com/17408.html
Sentinel的令牌桶算法和漏桶算法都参考了Guava RateLimiter的设计  
```java
// 限流策略入口 FlowRuleUtil#generateRater
private static TrafficShapingController generateRater(FlowRule rule) {
    if (rule.getGrade() == RuleConstant.FLOW_GRADE_QPS) {
        switch (rule.getControlBehavior()) {
            case RuleConstant.CONTROL_BEHAVIOR_WARM_UP:
                // WarmUp-令牌桶算法
                return new WarmUpController(rule.getCount(), rule.getWarmUpPeriodSec(),ColdFactorProperty.coldFactor);
            case RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER:
                // 排队等待-漏桶算法
                return new RateLimiterController(rule.getMaxQueueingTimeMs(), rule.getCount());
            case RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER:
                // 预热和匀速排队结合
                return new WarmUpRateLimiterController(rule.getCount(), rule.getWarmUpPeriodSec(),rule.getMaxQueueingTimeMs(), ColdFactorProperty.coldFactor);
            case RuleConstant.CONTROL_BEHAVIOR_DEFAULT:
            default:
                // Default mode or unknown mode: default traffic shaping controller (fast-reject).
        }
    }
    // 快速失败
    return new DefaultController(rule.getCount(), rule.getGrade());
}
```

## 快速失败 - DefaultController
https://www.daqianduan.com/17408.html  
当到达限流条件，直接抛出异常（Blocked by Sentinel (flow limiting)）
默认流控算法代码如下：
```java
@Override
public boolean canPass(Node node, int acquireCount, boolean prioritized) {
    int curCount = avgUsedTokens(node);
    // 当前阈值 + acquireCount 是否大于规则设定的count，小于等于则表示符合阈值设定直接返回true
    if (curCount + acquireCount > count) {
        // 在大于的情况下，针对QPS的情况会对先进来的请求进行特殊处理
        if (prioritized && grade == RuleConstant.FLOW_GRADE_QPS) {
            long currentTime;
            long waitInMs;
            currentTime = TimeUtil.currentTimeMillis();
            // 如果策略是QPS，那么对于优先请求尝试去占用下一个时间窗口中的令牌
            waitInMs = node.tryOccupyNext(currentTime, acquireCount, count);
            if (waitInMs < OccupyTimeoutProperty.getOccupyTimeout()) {
                node.addWaitingRequest(currentTime + waitInMs, acquireCount);
                node.addOccupiedPass(acquireCount);
                sleep(waitInMs);

                // PriorityWaitException indicates that the request will pass after waiting for {@link @waitInMs}.
                throw new PriorityWaitException(waitInMs);
            }
        }
        return false;
    }
    return true;
}
```

## WarmUp - WarmUpController

## 排队等待 - RateLimiterController