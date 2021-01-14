# SpringBoot - 默认启动类 spring.factories
``` java
// PropertySource Loaders
org.springframework.boot.env.PropertySourceLoader=\
org.springframework.boot.env.PropertiesPropertySourceLoader,\
org.springframework.boot.env.YamlPropertySourceLoader

// Run Listeners
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
 
// Error Reporters
org.springframework.boot.SpringBootExceptionReporter=\
org.springframework.boot.diagnostics.FailureAnalyzers
 
// Application Context Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer,\
org.springframework.boot.context.ContextIdApplicationContextInitializer,\
org.springframework.boot.context.config.DelegatingApplicationContextInitializer,\
org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer

// Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.context.logging.LoggingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener

// Environment Post Processors
org.springframework.boot.env.EnvironmentPostProcessor=\
org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor,\
org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor,\
org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor

// Failure Analyzers
org.springframework.boot.diagnostics.FailureAnalyzer=\
org.springframework.boot.diagnostics.analyzer.BeanCurrentlyInCreationFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.BeanNotOfRequiredTypeFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.BindFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.BindValidationFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.UnboundConfigurationPropertyFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.ConnectorStartFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.NoUniqueBeanDefinitionFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.PortInUseFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.ValidationExceptionFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.InvalidConfigurationPropertyNameFailureAnalyzer,\
org.springframework.boot.diagnostics.analyzer.InvalidConfigurationPropertyValueFailureAnalyzer

// FailureAnalysisReporters
org.springframework.boot.diagnostics.FailureAnalysisReporter=\
org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter
```

# SpringBoot-启动流程分析
http://blog.csdn.net/doegoo/article/details/52471310
https://www.processon.com/special/template/5d9f1a26e4b06b7d6ec5cfd4

## @SpringBootApplication 启动注解
``` java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration // 标识作用： 同 @Configuration
@EnableAutoConfiguration // @AutoConfigurationPackage @Import(AutoConfigurationImportSelector.class)
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication{}
```

# 1.初始化SpringApplication
```java
SpringApplication.run(DealSupportApplication.class, args);

// primarySources = DealSupportApplication.class
new SpringApplication(primarySources).run(args);

@SuppressWarnings({ "unchecked", "rawtypes" })
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
	this.resourceLoader = resourceLoader;
	Assert.notNull(primarySources, "PrimarySources must not be null");
	this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));

    // WebApplicationType.REACTIVE || SERVLET || None
    // org.springframework.web.servlet.DispatcherServlet
    // org.springframework.web.context.ConfigurableWebApplicationContext
    // javax.servlet.Servlet
	this.webApplicationType = deduceWebApplicationType();

    // ApplicationContextInitializer.class
	setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));

    // ApplicationListener.class
	setListeners((Collection)getSpringFactoriesInstances(ApplicationListener.class));

	this.mainApplicationClass = deduceMainApplicationClass();
}
```
获取所有 ApplicationContextInitializer & ApplicationListener
1. SpringFactoriesLoader.loadFactoryNames(type, classLoader));
2. createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
// 从指定位置 META-INF/spring.factories 加载 FactoryNames

# 2.执行run()方法
```java
/**
    * Run the Spring application, creating and refreshing a new
    * {@link ApplicationContext}.
    * @param args the application arguments (usually passed from a Java main method)
    * @return a running {@link ApplicationContext}
    */
public ConfigurableApplicationContext run(String... args) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    ConfigurableApplicationContext context = null;
    Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
    configureHeadlessProperty();

    // SpringApplicationRunListeners.class 从 spring.factories 加载
    SpringApplicationRunListeners listeners = getRunListeners(args);

    //触发 ApplicationStartingEvent 事件
    listeners.starting();

    try {
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);

        // 触发事件 ApplicationEnvironmentPreparedEvent
        ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments);

        configureIgnoreBeanInfo(environment);
        Banner printedBanner = printBanner(environment);

        context = createApplicationContext();
        exceptionReporters = getSpringFactoriesInstances(
                SpringBootExceptionReporter.class,
                new Class[] { ConfigurableApplicationContext.class }, context);
        prepareContext(context, environment, listeners, applicationArguments,
                printedBanner);
        refreshContext(context);
        afterRefresh(context, applicationArguments);
        stopWatch.stop();
        if (this.logStartupInfo) {
            new StartupInfoLogger(this.mainApplicationClass)
                    .logStarted(getApplicationLog(), stopWatch);
        }
        listeners.started(context);
        callRunners(context, applicationArguments);
    }
    catch (Throwable ex) {
        handleRunFailure(context, listeners, exceptionReporters, ex);
        throw new IllegalStateException(ex);
    }
    listeners.running(context);
    return context;
}
```
## 1.SpringApplicationRunListeners listeners = getRunListeners(args);  
listeners.starting(); //触发 ApplicationStartingEvent 事件

// SpringApplicationRunListener接口规定了Boot的生命周期，在各个生命周期广播相应的事件，调用实际的ApplicationListener


## 2.ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments)
* 2.a> getOrCreateEnvironment();  

  //创建标准环境

* 2.b> configureEnvironment(environment, applicationArguments.getSourceArgs());  

  //绑定 CommandLinePropertySource ，获取命令行参数  
  //绑定Profile environment.setActiveProfiles(StringUtils.toStringArray(profiles)); 指定ActiveProfile

* 2.c> listeners.environmentPrepared(environment); 
  //触发 EnvironmentPrepared 事件

```java
// ConfigFileApplicationListener 是 Spring Boot 中处理配置文件的监听器
public class ConfigFileApplicationListener
		implements EnvironmentPostProcessor, SmartApplicationListener, Ordered {

    // 监听 onApplicationEnvironmentPreparedEvent 获取所有的 EnvironmentPostProcessor 执行 postProcessEnvironment 方法 
	private void onApplicationEnvironmentPreparedEvent(
			ApplicationEnvironmentPreparedEvent event) {
        
        // 从Spring.facotries 加载 EnvironmentPostProcessor.class
		List<EnvironmentPostProcessor> postProcessors = loadPostProcessors();
		postProcessors.add(this);
		AnnotationAwareOrderComparator.sort(postProcessors);
		for (EnvironmentPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessEnvironment(event.getEnvironment(),
					event.getSpringApplication());
		}
	}

    // 在自身的 postProcessEnvironment 方法中实现 区分 Profile 后加载 Spring 配置文件
    @Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		addPropertySources(environment, application.getResourceLoader());
	}

    // 添加 PropertySourceOrderingPostProcessor implements BeanFactoryPostProcessor
    private void onApplicationPreparedEvent(ApplicationEvent event) {
		this.logger.replayTo(ConfigFileApplicationListener.class);
		addPostProcessors(((ApplicationPreparedEvent) event).getApplicationContext());
	}
}
```

## 3.createApplicationContext();

// 实例化 AnnotationConfigServletWebServerApplicationContext || AnnotationConfigApplicationContext  
```java
public class AnnotationConfigServletWebServerApplicationContext
extends ServletWebServerApplicationContext implements AnnotationConfigRegistry{

	public AnnotationConfigServletWebServerApplicationContext() {
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);

        // 重要！！！
        // 同时添加重要的 ConfigurationClassPostProcessor 
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
	}
}

/**
    * Register all relevant annotation post processors in the given registry.
    * @param registry the registry to operate on
    * @param source the configuration source element (already extracted)
    * that this registration was triggered from. May be {@code null}.
    * @return a Set of BeanDefinitionHolders, containing all bean definitions
    * that have actually been registered by this call
    */
public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
        BeanDefinitionRegistry registry, @Nullable Object source) {

    DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
    if (beanFactory != null) {
        if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
            beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
        }
        if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
            beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
        }
    }

    Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(4);

    if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
        RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
        def.setSource(source);
        beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
    }

    if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
        RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
        def.setSource(source);
        beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
    }

    if (!registry.containsBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
        RootBeanDefinition def = new RootBeanDefinition(RequiredAnnotationBeanPostProcessor.class);
        def.setSource(source);
        beanDefs.add(registerPostProcessor(registry, def, REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
    }

    // Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
    if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
        RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
        def.setSource(source);
        beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
    }

    // Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
    if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
        RootBeanDefinition def = new RootBeanDefinition();
        try {
            def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
                    AnnotationConfigUtils.class.getClassLoader()));
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
        }
        def.setSource(source);
        beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
    }

    if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
        RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
        def.setSource(source);
        beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
    }
    if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
        RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
        def.setSource(source);
        beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
    }

    return beanDefs;
}

public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor

/** BeanDefinitionRegistryPostProcessor may register further bean definitions
 *  which in turn define BeanFactoryPostProcessor instances
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor

```
 
## 4.prepareContext(context, environment, listeners, applicationArguments,printedBanner)

* 4.a> context.setEnvironment(environment);

* 4.b> postProcessApplicationContext(context);

  // 注册BeanNameGenerator AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR
 
* 4.c> applyInitializers(context);

  // 调用 ApplicationContextInitializer..initialize(context) 初始化方法

* 4.d> listeners.contextPrepared(context); // 空方法

* 4.e> load(context, sources.toArray(new Object[0]));

  // 把source也就是主类当做bean，加载到spring的容器中  
  [class com.liuwei.springboot.MybatisApplication]

 
* 4.f> listeners.contextLoaded(context); // 触发 ApplicationPreparedEvent 事件

## 5.1 refreshContext(context);
https://www.jianshu.com/p/a0a5088a651d?utm_campaign=hugo
```java
@Override
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        // Prepare this context for refreshing.
        prepareRefresh();

        // Tell the subclass to refresh the internal bean factory.
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

        // Prepare the bean factory for use in this context.
        prepareBeanFactory(beanFactory);

        try {
            // Allows post-processing of the bean factory in context subclasses.
            postProcessBeanFactory(beanFactory);

            // Invoke factory processors registered as beans in the context.
            invokeBeanFactoryPostProcessors(beanFactory);

            // Register bean processors that intercept bean creation.
            registerBeanPostProcessors(beanFactory);

            // Initialize message source for this context.
            initMessageSource();

            // Initialize event multicaster for this context.
            initApplicationEventMulticaster();

            // Initialize other special beans in specific context subclasses.
            onRefresh();

            // Check for listener beans and register them.
            registerListeners();

            // Instantiate all remaining (non-lazy-init) singletons.
            finishBeanFactoryInitialization(beanFactory);

            // Last step: publish corresponding event.
            finishRefresh();
        }

        catch (BeansException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - " +
                        "cancelling refresh attempt: " + ex);
            }

            // Destroy already created singletons to avoid dangling resources.
            destroyBeans();

            // Reset 'active' flag.
            cancelRefresh(ex);

            // Propagate exception to caller.
            throw ex;
        }

        finally {
            // Reset common introspection caches in Spring's core, since we
            // might not ever need metadata for singleton beans anymore...
            resetCommonCaches();
        }
    }
}
```
https://www.jianshu.com/p/a0a5088a651d?utm_campaign=hugo
https://www.cnblogs.com/disandafeier/p/12081301.html

### (01) prepareRefresh();  
// Prepare this context for refreshing. <br/>
初始化 context environment 中占位符
对系统的环境变量或者系统属性进行准备和校验

### (02) ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory(); 
// Tell the subclass to refresh the internal bean factory.<br/>
//创建并初始化beanFactory
this.beanFactory = new DefaultListableBeanFactory(); 
<br/>
 
### (03) prepareBeanFactory(beanFactory);
// Prepare the bean factory for use in this context.<br/>
```java
/**
    * Configure the factory's standard context characteristics,
    * such as the context's ClassLoader and post-processors.
    * @param beanFactory the BeanFactory to configure
    */
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    // Tell the internal bean factory to use the context's class loader etc.
    beanFactory.setBeanClassLoader(getClassLoader());
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

    // Configure the bean factory with context callbacks.
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

    // BeanFactory interface not registered as resolvable type in a plain factory.
    // MessageSource registered (and found for autowiring) as a bean.
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);

    // Register early post-processor for detecting inner beans as ApplicationListeners.
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

    // Detect a LoadTimeWeaver and prepare for weaving, if found.
    if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
        beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
        // Set a temporary ClassLoader for type matching.
        beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }

    // Register default environment beans.
    if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
    }
}
```
* //对BeanFactory的各种功能进行填充，如常用的注解@Autowired @Qualifier等
<br/>
 
* //设置SPEL表达式#{key}的解析器  
beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));

* //设置资源编辑注册器，如PerpertyEditorSupper的支持  
beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

* //添加ApplicationContextAwareProcessor处理器  
beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
 
* //在依赖注入忽略实现*Aware的接口，beanFactory.ignoreDependencyInterface 如EnvironmentAware、ApplicationEventPublisherAware，ApplicationContextAware ，ResourceLoaderAware，EmbeddedValueResolverAware，MessageSourceAware等

* // 忽略的自动装配（也就是实现了这些接口的Bean，不要Autowired自动装配了）

* //注册依赖，如一个bean的属性中含有ApplicationEventPublisher(beanFactory)，则会将beanFactory的实例注入进去  
beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

* // 注入一些其它信息的bean，比如environment、systemProperties、SystemEnvironment等

### (04) postProcessBeanFactory(beanFactory);
// Allows post-processing of the bean factory in context subclasses.<br/>
https://fangjian0423.github.io/2017/05/10/springboot-context-refresh/
```java
@Override
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    super.postProcessBeanFactory(beanFactory);
    if (this.basePackages != null && this.basePackages.length > 0) {
        this.scanner.scan(this.basePackages);
    }
    if (!this.annotatedClasses.isEmpty()) {
        this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));
    }
}

// super.postProcessBeanFactory(beanFactory);

/**
* Register ServletContextAwareProcessor.
* @see ServletContextAwareProcessor
*/
@Override
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    beanFactory.addBeanPostProcessor(
            new WebApplicationContextServletContextAwareProcessor(this));
    beanFactory.ignoreDependencyInterface(ServletContextAware.class);
}
```

### (05) invokeBeanFactoryPostProcessors(beanFactory);
// Invoke factory processors registered as beans in the context.<br/>
找出beanFactory中所有的实现了BeanDefinitionRegistryPostProcessor接口和BeanFactoryPostProcessor接口的bean  
将程序中的所有bean放入到beanDefinitionMap中  
// 注意这一步并没有实例化bean，而是获得bean的beanDefinition

<font color='red'>## ConfigurationClassPostProcessor</font>

```java
/**
    * Apply processing and build a complete {@link ConfigurationClass} by reading the
    * annotations, members and methods from the source class. This method can be called
    * multiple times as relevant sources are discovered.
    * @param configClass the configuration class being build
    * @param sourceClass a source class
    * @return the superclass, or {@code null} if none found or previously processed
    */
@Nullable
protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
        throws IOException {

    // Recursively process any member (nested) classes first
    processMemberClasses(configClass, sourceClass);

    // Process any @PropertySource annotations
    for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
            sourceClass.getMetadata(), PropertySources.class,
            org.springframework.context.annotation.PropertySource.class)) {
        if (this.environment instanceof ConfigurableEnvironment) {
            processPropertySource(propertySource);
        }
        else {
            logger.warn("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
                    "]. Reason: Environment must implement ConfigurableEnvironment");
        }
    }

    // Process any @ComponentScan annotations
    Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
            sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
    if (!componentScans.isEmpty() &&
            !this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
        for (AnnotationAttributes componentScan : componentScans) {
            // The config class is annotated with @ComponentScan -> perform the scan immediately
            Set<BeanDefinitionHolder> scannedBeanDefinitions =
                    this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
            // Check the set of scanned definitions for any further config classes and parse recursively if needed
            for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
                BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
                if (bdCand == null) {
                    bdCand = holder.getBeanDefinition();
                }
                if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
                    parse(bdCand.getBeanClassName(), holder.getBeanName());
                }
            }
        }
    }

    // Process any @Import annotations
    processImports(configClass, sourceClass, getImports(sourceClass), true);

    // Process any @ImportResource annotations
    AnnotationAttributes importResource =
            AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
    if (importResource != null) {
        String[] resources = importResource.getStringArray("locations");
        Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
        for (String resource : resources) {
            String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
            configClass.addImportedResource(resolvedResource, readerClass);
        }
    }

    // Process individual @Bean methods
    Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
    for (MethodMetadata methodMetadata : beanMethods) {
        configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
    }

    // Process default methods on interfaces
    processInterfaces(configClass, sourceClass);

    // Process superclass, if any
    if (sourceClass.getMetadata().hasSuperClass()) {
        String superclass = sourceClass.getMetadata().getSuperClassName();
        if (superclass != null && !superclass.startsWith("java") &&
                !this.knownSuperclasses.containsKey(superclass)) {
            this.knownSuperclasses.put(superclass, configClass);
            // Superclass found, return its annotation metadata and recurse
            return sourceClass.getSuperClass();
        }
    }

    // No superclass -> processing is complete
    return null;
}
```

### (06) registerBeanPostProcessors(beanFactory);
// Register bean processors that intercept bean creation.<br/>

找到BeanPostProcessor的实现，排序后注册到容器内  
<br/>

### (07) initMessageSource(); 
// Initialize message source for this context.<br/>
初始化国际化相关属性


### (08) initApplicationEventMulticaster();
// Initialize event multicaster for this context.<br/>
初始化事件广播器

### (09) onRefresh();
// Initialize other special beans in specific context subclasses.<br/>
createWebServer(); // 创建Web容器


### (10) registerListeners();
// Check for listener beans and register them.<br/>


### (11) finishBeanFactoryInitialization(beanFactory);  
// Instantiate all remaining (non-lazy-init) singletons.  <br/>
初始化工厂类 FactoryBean & 单例类 SingletonBean

将beanDefinitionMap中的非懒加载的bean都实例化  
// spring bean 的实例化过程 https://www.cnblogs.com/kevin-yuan/p/12157017.html
```java 
/**
* Finish the initialization of this context's bean factory,
* initializing all remaining singleton beans.
*/
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
    // Initialize conversion service for this context.
    if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
            beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
        beanFactory.setConversionService(
                beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
    }

    // Register a default embedded value resolver if no bean post-processor
    // (such as a PropertyPlaceholderConfigurer bean) registered any before:
    // at this point, primarily for resolution in annotation attribute values.
    if (!beanFactory.hasEmbeddedValueResolver()) {
        beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
    }

    // Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
    String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
    for (String weaverAwareName : weaverAwareNames) {
        getBean(weaverAwareName);
    }

    // Stop using the temporary ClassLoader for type matching.
    beanFactory.setTempClassLoader(null);

    // Allow for caching all bean definition metadata, not expecting further changes.
    beanFactory.freezeConfiguration();

    // Instantiate all remaining (non-lazy-init) singletons.
    beanFactory.preInstantiateSingletons();
}

// beanFactory.preInstantiateSingletons()
// 当Spring将所有单例 bean 都实例初始化完成以后，如果存在实现SmartInitializingSingleton接口的bean，那么Spring还会调用到该bean的afterSingletonsInstantiated()方法
```

### (12) finishRefresh();
// Last step: publish corresponding event.<br/>


### (13) resetCommonCaches();
// Reset common introspection caches in Spring's core, since we might not ever need metadata for singleton beans anymore... <br/>


## 5.2 registerShutdownHook()

##  6.afterRefresh(context, applicationArguments); 
// 空方法

## 7.listeners.started(context);   
// 触发 ApplicationStartedEvent 事件

## 8.callRunners(context, applicationArguments);
// 执行 ApplicationRunner & CommandLineRunner
```java
// CommandLineRunner接口可以用来接收字符串数组的命令行参数，ApplicationRunner 是使用ApplicationArguments 用来接收参数的
private void callRunners(ApplicationContext context, ApplicationArguments args) {
    List<Object> runners = new ArrayList<>();
    runners.addAll(context.getBeansOfType(ApplicationRunner.class).values());
    runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
    AnnotationAwareOrderComparator.sort(runners);
    for (Object runner : new LinkedHashSet<>(runners)) {
        if (runner instanceof ApplicationRunner) {
            callRunner((ApplicationRunner) runner, args);
        }
        if (runner instanceof CommandLineRunner) {
            callRunner((CommandLineRunner) runner, args);
        }
    }
}
```

##  9.listeners.running(context);   
// 触发 ApplicationReadyEvent 事件