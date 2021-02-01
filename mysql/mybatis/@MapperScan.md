# @MapperScan 作用流程详解
https://blog.csdn.net/qq_34789577/article/details/109056959  
org.mybatis.spring.annotation.@MapperScan

```java
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    AnnotationAttributes mapperScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
    if (mapperScanAttrs != null) {
      registerBeanDefinitions(mapperScanAttrs, registry, generateBaseBeanName(importingClassMetadata, 0));
    }
  }

  void registerBeanDefinitions(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry, String beanName) {

    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
    builder.addPropertyValue("processPropertyPlaceHolders", true);

    // @MapperScan 注解参数 
    // annotationClass, markerInterface, nameGenerator,factoryBean,sqlSessionTemplateRef,sqlSessionFactoryRef
    String sqlSessionFactoryRef = annoAttrs.getString("sqlSessionFactoryRef");
    if (StringUtils.hasText(sqlSessionFactoryRef)) {
      builder.addPropertyValue("sqlSessionFactoryBeanName", annoAttrs.getString("sqlSessionFactoryRef"));
    }
    ...

    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
  }

  private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
    return importingClassMetadata.getClassName() + "#" + MapperScannerRegistrar.class.getSimpleName() + "#" + index;
  }

  // @Repeatable(MapperScans.class) 实现原理
  static class RepeatingRegistrar extends MapperScannerRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
      AnnotationAttributes mapperScansAttrs = AnnotationAttributes
          .fromMap(importingClassMetadata.getAnnotationAttributes(MapperScans.class.getName()));
      if (mapperScansAttrs != null) {
        AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
        for (int i = 0; i < annotations.length; i++) {
          registerBeanDefinitions(annotations[i], registry, generateBaseBeanName(importingClassMetadata, i));
        }
      }
    }
  }

}
```

## MapperScannerConfigurer
1.扫描指定路径，并将路径下的信息记录为BeanDefinition；
2.将获取的BeanDefinition,设置为MapperFactoryBean，注入IOC