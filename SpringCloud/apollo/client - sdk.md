# Apollo 使用教程
https://ctripcorp.github.io/apollo/#/zh/design/apollo-introduction

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

#  客户端获取配置（Java API）
```java
Config config = ConfigService.getAppConfig();
Integer defaultRequestTimeout = 200;
Integer requestTimeout = config.getIntProperty("requestTimeout", defaultRequestTimeout);
```

# 客户端监听配置变化 （Java API）
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