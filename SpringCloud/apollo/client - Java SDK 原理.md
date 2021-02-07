# Apollo Client 使用教程
https://ctripcorp.github.io/apollo/#/zh/design/apollo-introduction

## Maven 依赖
```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>1.4.0</version>
</dependency>
```

# Apollo - SpringBoot 集成
```yml
app:
  id: tutorabc-deal-support

apollo:
  meta: http://dev.config.tsp.weitutorstage.com
  cacheDir: ./config
  bootstrap:
    enabled: true
    eagerLoad:
      enabled: true
```

#  客户端获取配置（Java API）
Apollo 本身是可以基于事件监听实时推送（Http长连接）变更（AutoUpdateConfigChangeListener），也会定时拉取（fallback）最新配置  
```java
Config config = ConfigService.getAppConfig();
Integer defaultRequestTimeout = 200;
Integer requestTimeout = config.getIntProperty("requestTimeout", defaultRequestTimeout);
```

# 客户端监听配置变化（Java API）
```java
Config config = ConfigService.getAppConfig();
config.addChangeListener(new ConfigChangeListener() {
  @Override
  public void onChange(ConfigChangeEvent changeEvent) {
    for (String key : changeEvent.changedKeys()) {
      ConfigChange change = changeEvent.getChange(key);
      System.out.println(String.format(
        "Found change - key: %s, oldValue: %s, newValue: %s, changeType: %s",
        change.getPropertyName(), change.getOldValue(),
        change.getNewValue(), change.getChangeType()));
     }
  }
});
```

## 客户端本地开发模式
https://ctripcorp.github.io/apollo/#/zh/usage/java-sdk-user-guide  
Apollo客户端还支持本地开发模式，这个主要用于当开发环境无法连接Apollo服务器的时候，比如在邮轮、飞机上做相关功能开发。

在本地开发模式下，Apollo只会从本地文件读取配置信息，不会从Apollo服务器读取配置

1. 修改/opt/settings/server.properties（Mac/Linux）或C:\opt\settings\server.properties（Windows）文件，设置env为Local  
<code>
env=Local
</code>

2. 本地配置文件目录
<code>  
Mac/Linux: /opt/data/{appId}/config-cache  
Windows: C:\opt\data\{appId}\config-cache  
</code>
appId就是应用的appId，如100004458。  
请确保该目录存在，且应用程序对该目录有读权限  

3. 配置文件名称及类容  
<code>
{appId}+{cluster}+{namespace}.properties
</code>  
appId就是应用自己的appId，如100004458  
cluster就是应用使用的集群，一般在本地模式下没有做过配置的话，就是default  
namespace就是应用使用的配置namespace，一般是application client-local-cache  

## 客户端本地缓存
Apollo客户端会把从服务端获取到的配置在本地文件系统缓存一份，用于在遇到服务不可用，或网络不通的时候，依然能从本地恢复配置，不影响应用正常运行

### com.ctrip.framework.apollo.ConfigService
https://blog.csdn.net/crystonesc/article/details/106630412  

```java
public class ConfigService {
  private static final ConfigService s_instance = new ConfigService();

  // ConfigManager是config的管理器
  // 根据 namespace 获取 Config 实例; 默认 namespace = application
  private volatile ConfigManager m_configManager;   // 单例

  // ConfigRegistry是ConfigFactory的注册器
  private volatile ConfigRegistry m_configRegistry; // 单例

}
```

### ConfigManager
ConfigManager (com.ctrip.framework.apollo.internals.ConfigManager)实际上是一个接口类  
其默认包含了一个实现类DefaultConfigManager(com.ctrip.framework.apollo.internals.DefaultConfigManager)  

实际上Apollo-Client默认也是使用的DefaultConfigManager(通过ApolloInjector注入)，ConfigManager包含两个接口方法getConfig和getConfigFile，getConfig用于获取property类型的配置文件（即key-value形式），getConfigFile用于支持其它类型的配置文件，例如xml,yaml,yml等

```java
public class DefaultConfigManager implements ConfigManager {

  //(1) 持有ConfigFactoryManager,ConfigFactoryManager是ConfigFactory的管理器
  private ConfigFactoryManager m_factoryManager;
 
  //(2) 生成的Config存储在这里的Map数据结构,key为namespace
  private Map<String, Config> m_configs = Maps.newConcurrentMap();
  //(3) 生成的ConfigFile存储在这里的Map数据结构,key为文件名
  private Map<String, ConfigFile> m_configFiles = Maps.newConcurrentMap();
 
  //(4) 构造函数中通过ApolloInjector注入了ConfigFactoryManager
  public DefaultConfigManager() {
    m_factoryManager = ApolloInjector.getInstance(ConfigFactoryManager.class);
  }

  @Override
  public Config getConfig(String namespace) {
    // 通过namespace获取Config,首先从m_configs缓存中获取
    Config config = m_configs.get(namespace);
 
    if (config == null) {
      synchronized (this) {
        config = m_configs.get(namespace);

        // 如果没有获取则通过ConfigFacotryManager获取ConfigFactory并创建Config
        if (config == null) {
          ConfigFactory factory = m_factoryManager.getFactory(namespace);
 
          config = factory.create(namespace);
          m_configs.put(namespace, config);
        }
      }
    }
 
    return config;
  }
  ...
}
```

### ConfigFactory
ConfigFactory也是一个接口类，其默认包含一个DefaultConfigFactory
Apollo也是通过ApolloInjector来讲DefaultConfigFactory注入到ConfigFactoryManager当中

```java
public class DefaultConfigFactory implements ConfigFactory {
  private static final Logger logger = LoggerFactory.getLogger(DefaultConfigFactory.class);
  private ConfigUtil m_configUtil;

  public DefaultConfigFactory() {
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  }

  @Override
  public Config create(String namespace) {
    ConfigFileFormat format = determineFileFormat(namespace);
    if (ConfigFileFormat.isPropertiesCompatible(format)) {
      return new DefaultConfig(namespace, createPropertiesCompatibleFileConfigRepository(namespace, format));
    }
    return new DefaultConfig(namespace, createLocalConfigRepository(namespace));
  }

  @Override
  public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
    ConfigRepository configRepository = createLocalConfigRepository(namespace);
    switch (configFileFormat) {
      case Properties:
        return new PropertiesConfigFile(namespace, configRepository);
      case XML:
        return new XmlConfigFile(namespace, configRepository);
      case JSON:
        return new JsonConfigFile(namespace, configRepository);
      case YAML:
        return new YamlConfigFile(namespace, configRepository);
      case YML:
        return new YmlConfigFile(namespace, configRepository);
      case TXT:
        return new TxtConfigFile(namespace, configRepository);
    }

    return null;
  }

  // 创建LocalFileConfigRepository,如果Apollo是以本地模式运行，则创建没有upstream的LocalFileConfigRepository,否则
  // 创建一个带有远程仓库RemoteConfigRepository的创建LocalFileConfigRepository
  LocalFileConfigRepository createLocalConfigRepository(String namespace) {
    if (m_configUtil.isInLocalMode()) {
      logger.warn(
          "==== Apollo is in local mode! Won't pull configs from remote server for namespace {} ! ====",
          namespace);
      return new LocalFileConfigRepository(namespace);
    }
    return new LocalFileConfigRepository(namespace, createRemoteConfigRepository(namespace));
  }

  // 创建远程配置仓库
  RemoteConfigRepository createRemoteConfigRepository(String namespace) {
    return new RemoteConfigRepository(namespace);
  }

  ......
}
```

### ApolloInjector
Apollo SPI 默认加载路径 /META-INF/services/%s

```java
com.ctrip.framework.apollo.internals.DefaultMetaServerProvider
com.ctrip.framework.apollo.internals.DefaultInjector
com.ctrip.framework.apollo.spring.spi.DefaultApolloConfigRegistrarHelper
com.ctrip.framework.apollo.spring.spi.DefaultConfigPropertySourcesProcessorHelper

public class DefaultInjector implements Injector{

  public DefaultInjector() {
    try {
      m_injector = Guice.createInjector(new ApolloModule());
    } catch (Throwable ex) {
      ApolloConfigException exception = new ApolloConfigException("Unable to initialize Guice Injector!", ex);
      Tracer.logError(exception);
      throw exception;
    }
  }

  private static class ApolloModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ConfigManager.class).to(DefaultConfigManager.class).in(Singleton.class);
      bind(ConfigFactoryManager.class).to(DefaultConfigFactoryManager.class).in(Singleton.class);
      bind(ConfigRegistry.class).to(DefaultConfigRegistry.class).in(Singleton.class);
      bind(ConfigFactory.class).to(DefaultConfigFactory.class).in(Singleton.class);
      bind(ConfigUtil.class).in(Singleton.class);
      bind(HttpUtil.class).in(Singleton.class);
      bind(ConfigServiceLocator.class).in(Singleton.class);
      bind(RemoteConfigLongPollService.class).in(Singleton.class);
      bind(YamlParser.class).in(Singleton.class);
    }
  }
}
```
