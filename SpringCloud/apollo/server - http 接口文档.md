# Apollo - Server Http 接口文档
https://ctripcorp.github.io/apollo/#/zh/usage/other-language-client-user-guide

## ConfigFileController 带缓存的Http接口从Apollo读取配置
接口会从缓存中获取配置，适合频率较高的配置拉取请求，如简单的每30秒轮询一次配置

``` java
// 使用 Guava 对配置信息进行缓存
private Cache<String, String> localCache;

localCache = CacheBuilder.newBuilder()
    .expireAfterWrite(EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
    .weigher((Weigher<String, String>) (key, value) -> value == null ? 0 : value.length())
    .maximumWeight(MAX_CACHE_SIZE)
    .removalListener(notification -> {
        String cacheKey = notification.getKey();
        logger.debug("removing cache key: {}", cacheKey);
        if (!cacheKey2WatchedKeys.containsKey(cacheKey)) {
        return;
        }
        //create a new list to avoid ConcurrentModificationException
        List<String> watchedKeys = new ArrayList<>(cacheKey2WatchedKeys.get(cacheKey));
        for (String watchedKey : watchedKeys) {
        watchedKeys2CacheKey.remove(watchedKey, cacheKey);
        }
        cacheKey2WatchedKeys.removeAll(cacheKey);
        logger.debug("removed cache key: {}", cacheKey);
    })
    .build();

// outputFormat 默认 properties
String cacheKey = assembleCacheKey(outputFormat, appId, clusterName, namespace, dataCenter);
String result = localCache.getIfPresent(cacheKey);
```

* ip={clientIp} 参数是可选的，用来实现灰度发布

com.ctrip.framework.apollo.configservice.controller.ConfigFileController
``` json
URL: {config_server_url}/configfiles/json/{appId}/{clusterName}/{namespaceName}?ip={clientIp}   
 
Method: GET

// Http Response 返回结果
{
    "portal.elastic.document.type":"biz",
    "portal.elastic.cluster.name":"hermes-es-fws"
}
```

## ConfigController 不带缓存的Http接口从Apollo读取配置
接口会直接从数据库中获取配置，可以配合配置推送通知实现实时更新配置

* releaseKey={releaseKey} : 将上一次返回对象中的releaseKey传入即可，用来给服务端比较版本，如果版本比下来没有变化，则服务端直接返回304以节省流量和运算

* ip={clientIp} 参数是可选的，用来实现灰度发布

com.ctrip.framework.apollo.configservice.controller.ConfigController
``` json
URL: {config_server_url}/configs/{appId}/{clusterName}/{namespaceName}?releaseKey={releaseKey}&ip={clientIp}  

Method: GET

// Http Response 返回结果
{
  "appId": "100004458",
  "cluster": "default",
  "namespaceName": "application",
  "configurations": {
    "portal.elastic.document.type":"biz",
    "portal.elastic.cluster.name":"hermes-es-fws"
  },
  "releaseKey": "20170430092936-dee2d58e74515ff3"
}
```
## NotificationControllerV2 应用感知配置更新
Apollo提供了基于Http long polling的配置更新推送通知，第三方客户端可以看自己实际的需求决定是否需要使用这个功能

com.ctrip.framework.apollo.configservice.controller.NotificationControllerV2
``` json
URL: {config_server_url}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}  

Method: GET  

// Http Response 返回结果
[
  {
    "namespaceName": "application",
    "notificationId": 101
  }
]

// 如果返回的HttpStatus是304，说明配置没有变化
// 如果返回的HttpStauts是200，说明配置有变化，针对变化的namespace重新去服务端拉取配置
```