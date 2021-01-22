# Apollo Client 启动流程
1. RemoteConfigRepository ，定时轮询 Config Service 的配置读取  
2. RemoteConfigLongPollService ，长轮询 Config Service 的配置变更通知 /notifications/v2 接口  

## pom.xml
```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>1.4.0</version>
</dependency>
```

## spring.factories
```java
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.ctrip.framework.apollo.spring.boot.ApolloAutoConfiguration

org.springframework.context.ApplicationContextInitializer=\
com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer

org.springframework.boot.env.EnvironmentPostProcessor=\
com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer
```

### @EnableApolloConfig
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ApolloConfigRegistrar.class)
public @interface EnableApolloConfig {}

// apollo-client-1.4.0.jar
public class ApolloConfigRegistrar implements ImportBeanDefinitionRegistrar {
  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata
        .getAnnotationAttributes(EnableApolloConfig.class.getName()));
    String[] namespaces = attributes.getStringArray("value");
    int order = attributes.getNumber("order");
    PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaces), order);

    Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();

    // to make sure the default PropertySourcesPlaceholderConfigurer's priority is higher than PropertyPlaceholderConfigurer
    propertySourcesPlaceholderPropertyValues.put("order", 0);

    // BeanRegistrationUtil.registerBeanDefinitionIfNotExists 注册指定Bean
    PropertySourcesPlaceholderConfigurer  //->BeanFactoryPostProcessor
    PropertySourcesProcessor              //->BeanFactoryPostProcessor
    ApolloAnnotationProcessor             //->BeanPostProcessor
    SpringValueProcessor                  //->BeanFactoryPostProcessor和BeanPostProcessor
    SpringValueDefinitionProcessor        //->BeanDefinitionRegistryPostProcessor（即BeanFactoryPostProcessor）
    ApolloJsonValueProcessor              //->BeanPostProcessor
  }
}
```

### PropertySourcesProcessor 作用详解
https://blog.csdn.net/fedorafrog/article/details/103919805

1. 根据命名空间从配置中心获取配置信息，创建RemoteConfigRepository和LocalFileConfigRepository对象。RemoteConfigRepository表示远程配置中心资源，LocalFileConfigRepository表示本地缓存配置资源。
LocalFileConfigRepository对象缓存配置信息到C:\opt\data 或者/opt/data目录。
2. RemoteConfigRepository开启HTTP长轮询请求定时任务，默认2s请求一次。
3. 將本地缓存配置信息转换为PropertySource对象（Apollo自定义了Spring的PropertySource），加载到Spring的Environment对象中。
4. 將自定义的ConfigPropertySource注册为观察者。一旦RemoteConfigRepository发现远程配置中心信息发生变化，ConfigPropertySource对象会得到通知

```java
public class PropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    initializePropertySources();
    initializeAutoUpdatePropertiesFeature(beanFactory);
  }
}
```

### com.ctrip.framework.apollo.spring.boot.ApolloAutoConfiguration
```java
@Configuration
//apollo.bootstrap.enabled = true
@ConditionalOnProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED)
@ConditionalOnMissingBean(PropertySourcesProcessor.class)
public class ApolloAutoConfiguration {

  @Bean
  public ConfigPropertySourcesProcessor configPropertySourcesProcessor() {
    return new ConfigPropertySourcesProcessor();
  }
}

public class ConfigPropertySourcesProcessor extends PropertySourcesProcessor
    implements BeanDefinitionRegistryPostProcessor {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();
    // to make sure the default PropertySourcesPlaceholderConfigurer's priority is higher than PropertyPlaceholderConfigurer
    propertySourcesPlaceholderPropertyValues.put("order", 0);

    // BeanRegistrationUtil.registerBeanDefinitionIfNotExists 注册指定Bean
    PropertySourcesPlaceholderConfigurer 
    ApolloAnnotationProcessor
    SpringValueProcessor
    ApolloJsonValueProcessor

    processSpringValueDefinition(registry);
  }
}

// 判断 isAutoUpdateInjectedSpringPropertiesEnabled() 处理配置的自动更新
// 获取所有的 BeanDefinition 的 placeholder
// springValueDefinitions.put(beanName, new SpringValueDefinition(key, placeholder, propertyValue.getName()));
private void processSpringValueDefinition(BeanDefinitionRegistry registry) {
  SpringValueDefinitionProcessor springValueDefinitionProcessor = new SpringValueDefinitionProcessor();
  springValueDefinitionProcessor.postProcessBeanDefinitionRegistry(registry);
}
```

### com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer
```java
public class ApolloApplicationContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> , EnvironmentPostProcessor, Ordered {

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
      .getInstance(ConfigPropertySourceFactory.class);

    @Override
    public void initialize(ConfigurableApplicationContext context) {
      ConfigurableEnvironment environment = context.getEnvironment();
      String enabled = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, "false");
      if (!Boolean.valueOf(enabled)) {
        logger.debug("Apollo bootstrap config is not enabled for context {}, see property: ${{}}", context, PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
        return;
      }
      logger.debug("Apollo bootstrap config is enabled for context {}", context);

      initialize(environment);
    }


  /**
   * Initialize Apollo Configurations Just after environment is ready.
   * @param environment
   */
  protected void initialize(ConfigurableEnvironment environment) {
    if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
      //already initialized
      return;
    }

    String namespaces = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
    logger.debug("Apollo bootstrap namespaces: {}", namespaces);
    List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);

    CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
    for (String namespace : namespaceList) {
      Config config = ConfigService.getConfig(namespace);
      composite.addPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
    }

    environment.getPropertySources().addFirst(composite);
  }

  /**
   * In order to load Apollo configurations as early as even before Spring loading logging system phase,
   * this EnvironmentPostProcessor can be called Just After ConfigFileApplicationListener has succeeded.
   *
   * <br />
   * The processing sequence would be like this: <br />
   * Load Bootstrap properties and application properties -----> load Apollo configuration properties ----> Initialize Logging systems
   * @param configurableEnvironment
   * @param springApplication
   */
  @Override
  public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {

    // should always initialize system properties like app.id in the first place
    initializeSystemProperty(configurableEnvironment);
    Boolean eagerLoadEnabled = configurableEnvironment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_EAGER_LOAD_ENABLED, Boolean.class, false);

    //EnvironmentPostProcessor should not be triggered if you don't want Apollo Loading before Logging System Initialization
    if (!eagerLoadEnabled) {
      return;
    }
    Boolean bootstrapEnabled = configurableEnvironment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, Boolean.class, false);
    if (bootstrapEnabled) {
      initialize(configurableEnvironment);
    }

  }
}
```

## SpringInjector
```java
public class SpringInjector {
  private static volatile Injector s_injector;
  private static final Object lock = new Object();

  private static Injector getInjector() {
    if (s_injector == null) {
      synchronized (lock) {
        if (s_injector == null) {
          try {
            s_injector = Guice.createInjector(new SpringModule());
          } catch (Throwable ex) {
            ApolloConfigException exception = new ApolloConfigException("Unable to initialize Apollo Spring Injector!", ex);
            Tracer.logError(exception);
            throw exception;
          }
        }
      }
    }
    return s_injector;
  }

  private static class SpringModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(PlaceholderHelper.class).in(Singleton.class);
      bind(ConfigPropertySourceFactory.class).in(Singleton.class);
      bind(SpringValueRegistry.class).in(Singleton.class);
    }
  }
}
```