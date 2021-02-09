# Spring Cloud Hystrix设计原理
https://www.jianshu.com/p/684b04b6c454  

```java
public class QueryOrderCommand extends HystrixCommand<Order> {
    private String orderId;
    public QueryOrderCommand(String orderId){
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("hystrix-order-group"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("hystrix-thread-order"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("hystrix-pay-order"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.defaultSetter())
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.defaultSetter()
                        .withCoreSize(10)
                        .withQueueSizeRejectionThreshold(15)
                )
        );
        this.orderId = orderId;
    }
    @Override
    protected Order run() throws Exception {
        System.out.println("fetching order info via service call");
        return new Order();
    }
}

class Order{
    private String orderId;
    private String productId;
    private String status;
}
```