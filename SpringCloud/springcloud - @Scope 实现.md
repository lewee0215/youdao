# @Scope 实现原理
https://www.cnblogs.com/javastack/p/12049139.html  
Scope(org.springframework.beans.factory.config.Scope)是Spring 2.0开始就有的核心的概念

## Scope与ApplicationContext生命周期
AbstractBeanFactory#doGetBean创建Bean实例
```java
protected <T> T doGetBean(...){
    final RootBeanDefinition mbd = ...
    if (mbd.isSingleton()) {
        ...
    } else if (mbd.isPrototype())
       ...
    } else {
          String scopeName = mbd.getScope();
          final Scope scope = this.scopes.get(scopeName);
          Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {...});
          ...
    }
    ...
}
```
Singleton和Prototype是硬编码的，并不是Scope子类。

Scope实际上是自定义扩展的接口，Scope Bean实例交由Scope自己创建，例如SessionScope是从Session中获取实例的，ThreadScope是从ThreadLocal中获取的，而RefreshScope是在内建缓存中获取的

## @Scope Bean 注册
https://blog.csdn.net/geng2568/article/details/112874664  
```java
public void registerBean(Class<?> annotatedClass, String name, Class<? extends Annotation>... qualifiers) {
    // 将配置类装载到AnnotatedGenericBeanDefinition,通过内置的StandardAnnotationMetadata来获取注解元数据
    AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);
    if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
        return;
    }

    // 从scope元数据信息中获得scope的值，默认是singleton
    ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
    abd.setScope(scopeMetadata.getScopeName());

    // 判断是否含有beanName，如果没有则调用自动生成器进行生成
    String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

    // 解析部分注解: @DependsOn、@Lazy、@Primary、@Role
    AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

    // qualifier,多个实现类时，通过这个注解来区分加载的bean
    if (qualifiers != null) {
        for (Class<? extends Annotation> qualifier : qualifiers) {
            if (Primary.class == qualifier) {
                abd.setPrimary(true);
            }
            else if (Lazy.class == qualifier) {
                abd.setLazyInit(true);
            }
            else {
                abd.addQualifier(new AutowireCandidateQualifier(qualifier));
            }
        }
    }

    // 是否需要根据scope生成动态代理对象
    BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
    definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);

    // 注册BeanDefinition
    BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
}

// AnnotationConfigUtils.applyScopedProxyMode
static BeanDefinitionHolder applyScopedProxyMode(
ScopeMetadata metadata, BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
 
    ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
    if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
        //如果当前的definition中的scope属性是默认值则直接返回，否则则为当前的definition生成一个代理definition，很有意思。
        return definition;
    }
    boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
    return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
}

public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition,BeanDefinitionRegistry registry, boolean proxyTargetClass) {
 
   String originalBeanName = definition.getBeanName();
   BeanDefinition targetDefinition = definition.getBeanDefinition();
   String targetBeanName = getTargetBeanName(originalBeanName);
 
   // Create a scoped proxy definition for the original bean name,
   // "hiding" the target bean in an internal target definition.
   //将ScopedProxyFactoryBean类封装成BD，为后续实例准备
   RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);

   //这个代理BD中装饰一个被代理的BD
   proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, targetBeanName));
   proxyDefinition.setOriginatingBeanDefinition(targetDefinition);
   proxyDefinition.setSource(definition.getSource());
   proxyDefinition.setRole(targetDefinition.getRole());
 
   proxyDefinition.getPropertyValues().add("targetBeanName", targetBeanName);
   if (proxyTargetClass) {
      targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
      // ScopedProxyFactoryBean's "proxyTargetClass" default is TRUE, so we don't need to set it explicitly here.
   }
   else {
      proxyDefinition.getPropertyValues().add("proxyTargetClass", Boolean.FALSE);
   }
 
   // Copy autowire settings from original bean definition.
   proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
   proxyDefinition.setPrimary(targetDefinition.isPrimary());
   if (targetDefinition instanceof AbstractBeanDefinition) {
      proxyDefinition.copyQualifiersFrom((AbstractBeanDefinition) targetDefinition);
   }
 
   // The target bean should be ignored in favor of the scoped proxy.
   //setAutowireCandidate这个属性为false的意思是容器在自动装配对象时，不考虑该Bean，但是该bean是可以装配其他的Bean
   targetDefinition.setAutowireCandidate(false);
   targetDefinition.setPrimary(false);
 
   // Register the target bean as separate bean in the factory.
   //注册BD
   registry.registerBeanDefinition(targetBeanName, targetDefinition);
 
   // Return the scoped proxy definition as primary bean definition
   // 返回代理BDHolder
   // (potentially an inner bean).
   return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
}

// AbstractBeanFactory#doGetBean 创建Bean实例
{
    String scopeName = mbd.getScope();
    final Scope scope = this.scopes.get(scopeName);
    if (scope == null) {
        throw new IllegalStateException("No Scope registered for scope '" + scopeName + "'");
    }
    try {
        Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
            public Object getObject() throws BeansException {
                beforePrototypeCreation(beanName);
                try {
                    return createBean(beanName, mbd, args);
                }
                finally {
                    afterPrototypeCreation(beanName);
                }
            }
        });
        bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
    }
    catch (IllegalStateException ex) {
        throw new BeanCreationException(beanName,
                "Scope '" + scopeName + "' is not active for the current thread; " +
                "consider defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                ex);
    }
}

```

## RefreshAutoConfiguration
```java
@Configuration
@ConditionalOnClass(RefreshScope.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class RefreshAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public static RefreshScope refreshScope() {
		return new RefreshScope();
	}

	@Bean
	@ConditionalOnMissingBean
	public static LoggingRebinder loggingRebinder() {
		return new LoggingRebinder();
	}

	@Bean
	@ConditionalOnMissingBean
	public EnvironmentManager environmentManager(ConfigurableEnvironment environment) {
		return new EnvironmentManager(environment);
	}

	@Bean
	@ConditionalOnMissingBean
	public ContextRefresher contextRefresher(ConfigurableApplicationContext context,
			RefreshScope scope) {
		return new ContextRefresher(context, scope);
	}
}
```

## @RefreshScope & class RefreshScope extends GenericScope
```java
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Scope("refresh")  //{@link #scopeName = refresh} @RefreshScope 是scopeName="refresh"的 @Scope
@Documented
public @interface RefreshScope {
	/**
	 * @see Scope#proxyMode()
	 */
	ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;
}

// 向 Spring 容器注册 RefreshScope
public class GenericScope implements Scope, BeanFactoryPostProcessor...{
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
      beanFactory.registerScope(this.name/*refresh*/, this/*RefreshScope*/);
      ...
  }
}
```

## RefreshScope 刷新过程
入口在ContextRefresher#refresh
```java
refresh() {
  Map<String, Object> before = ①extract(this.context.getEnvironment().getPropertySources());

  ②addConfigFilesToEnvironment();

  Set<String> keys = ④changes(before,③extract(this.context.getEnvironment().getPropertySources())).keySet();

  this.context.⑤publishEvent(new EnvironmentChangeEvent(keys));
  this.scope.⑥refreshAll();
 }

/**
①提取标准参数(SYSTEM,JNDI,SERVLET)之外所有参数变量

②把原来的Environment里的参数放到一个新建的Spring Context容器下重新加载，完事之后关闭新容器

③提起更新过的参数(排除标准参数)

④比较出变更项

⑤发布环境变更事件,接收：EnvironmentChangeListener／LoggingRebinder

⑥RefreshScope用新的环境参数重新生成Bean，重新生成的过程很简单，清除refreshscope缓存幷销毁Bean，下次就会重新从BeanFactory获取一个新的实例（该实例使用新的配置）

*/
```
