##　@AutoConfigureBefore & @AutoConfigureAfter 实现原理
https://mingyang.blog.csdn.net/article/details/110448731  
EnableAutoConfiguration 加载实现类 AutoConfigurationImportSelector 

```java
// 自动化配置类加载顺序实现: AutoConfigurationImportSelector.AutoConfigurationGroup#sortAutoConfigurations
public class AutoConfigurationImportSelector
		implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware,
		BeanFactoryAware, EnvironmentAware, Ordered {
	...

	//autoConfigurationMetadata：自动化配置注解元数据对象，是PropertiesAutoConfigurationMetadata的实例对象
	private List<String> sortAutoConfigurations(Set<String> configurations,AutoConfigurationMetadata autoConfigurationMetadata) {
		//此处调用实际排序的方法 getInPriorityOrder
		//MetadataReaderFactory是MetadataReader元数据读取器工厂类
		return new AutoConfigurationSorter(getMetadataReaderFactory(), autoConfigurationMetadata).getInPriorityOrder(configurations);
	}

	public List<String> getInPriorityOrder(Collection<String> classNames) {
		// 读取并Check AutoConfigurationClass 可用性
		final AutoConfigurationClasses classes = new AutoConfigurationClasses(this.metadataReaderFactory, this.autoConfigurationMetadata, classNames);
		List<String> orderedClassNames = new ArrayList<String>(classNames);
		// Initially sort alphabetically
		Collections.sort(orderedClassNames);
		// Then sort by order
		Collections.sort(orderedClassNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int i1 = classes.get(o1).getOrder();
				int i2 = classes.get(o2).getOrder();
				return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
			}

		});
		// Then respect @AutoConfigureBefore @AutoConfigureAfter
		orderedClassNames = sortByAnnotation(classes, orderedClassNames);
		return orderedClassNames;
	}

	// 判定 AutoConfiguration 可用状态
	//获取自动化配置类实例对象
	AutoConfigurationClass autoConfigurationClass = new AutoConfigurationClass(className,metadataReaderFactory, autoConfigurationMetadata);
    //判定配置类是否可用
	boolean available = autoConfigurationClass.isAvailable();

}

// 判定可用的AutoConfiguration
private static class AutoConfigurationClasses{

	// 内存存储可用类信息
	private final Map<String, AutoConfigurationClass> classes = new HashMap<>();

	private void addToClasses(MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata,
				Collection<String> classNames, boolean required) {
			for (String className : classNames) {
				if (!this.classes.containsKey(className)) {
					AutoConfigurationClass autoConfigurationClass = new AutoConfigurationClass(
							className, metadataReaderFactory, autoConfigurationMetadata);
					boolean available = autoConfigurationClass.isAvailable();
					if (required || available) {
						this.classes.put(className, autoConfigurationClass);
					}
					if (available) {
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getBefore(), false);
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getAfter(), false);
					}
				}
			}
	}
}
```