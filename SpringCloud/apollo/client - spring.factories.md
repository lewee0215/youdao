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
    PropertySourcesPlaceholderConfigurer 
    PropertySourcesProcessor
    ApolloAnnotationProcessor
    SpringValueProcessor
    SpringValueDefinitionProcessor
    ApolloJsonValueProcessor
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
    public static final int DEFAULT_ORDER = 0;

    private static final Logger logger = LoggerFactory.getLogger(ApolloApplicationContextInitializer.class);
    private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final String[] APOLLO_SYSTEM_PROPERTIES = {"app.id", ConfigConsts.APOLLO_CLUSTER_KEY,
        "apollo.cacheDir", ConfigConsts.APOLLO_META_KEY};

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
        .getInstance(ConfigPropertySourceFactory.class);

    private int order = DEFAULT_ORDER;

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
   *
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
   * To fill system properties from environment config
   */
  void initializeSystemProperty(ConfigurableEnvironment environment) {
    for (String propertyName : APOLLO_SYSTEM_PROPERTIES) {
      fillSystemPropertyFromEnvironment(environment, propertyName);
    }
  }

  private void fillSystemPropertyFromEnvironment(ConfigurableEnvironment environment, String propertyName) {
    if (System.getProperty(propertyName) != null) {
      return;
    }

    String propertyValue = environment.getProperty(propertyName);

    if (Strings.isNullOrEmpty(propertyValue)) {
      return;
    }

    System.setProperty(propertyName, propertyValue);
  }

  /**
   *
   * In order to load Apollo configurations as early as even before Spring loading logging system phase,
   * this EnvironmentPostProcessor can be called Just After ConfigFileApplicationListener has succeeded.
   *
   * <br />
   * The processing sequence would be like this: <br />
   * Load Bootstrap properties and application properties -----> load Apollo configuration properties ----> Initialize Logging systems
   *
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