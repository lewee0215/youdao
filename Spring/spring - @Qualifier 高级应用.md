# @Qualifier 按类别批量依赖注入
https://www.cnblogs.com/yourbatman/p/11532717.html

注解@Autowired 通常用来注入一个Bean，其实注解@Autowired 还可以注入List和Map  
Spring会将service的名字作为key，对象作为value封装进入Map

```java
@LoadBalanced
@Autowired(required = false)
private List<RestTemplate> restTemplates = Collections.emptyList();	

// 注解@LoadBalanced和注解@Qualifier效果是一样的,标识作用
// 能把容器内所有RestTemplate类型并且标注有 @LoadBalanced 注解的 Bean 全注入进来
```

## QualifierAnnotationAutowireCandidateResolver 详解
@Qualifier 注解的功能实现，专门用于解析此注解
```java
public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryAware{

    // 同时支持 javax.inject.Qualifier 和 springframework - Qualifier
    public QualifierAnnotationAutowireCandidateResolver() {
		this.qualifierTypes.add(Qualifier.class);
		try {
			this.qualifierTypes.add((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Qualifier",
							QualifierAnnotationAutowireCandidateResolver.class.getClassLoader()));
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
	}
}
```
