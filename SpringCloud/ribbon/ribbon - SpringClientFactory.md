# Ribbon - SpringClientFactory
https://blog.csdn.net/sinat_36553913/article/details/108298183  
该类是 Spring 创建 Ribbon 客户端、负载均衡器、客户端配置实例的工厂，并且为每个 client name 创建对应的 Spring ApplicationContext

```java
/**
 * A factory that creates client, load balancer and client configuration instances. It
 * creates a Spring ApplicationContext per client name, and extracts the beans that it
 * needs from there.
 *
 * @author Spencer Gibb
 * @author Dave Syer
 */
public class SpringClientFactory extends NamedContextFactory<RibbonClientSpecification> {}
```
该类将 RibbonClientSpecification 集合作为参数传递到 NamedContextFactory ，作为创建 Ribbon Client 上下文的依据
