# Apollo - Server Http 接口文档
https://ctripcorp.github.io/apollo/#/zh/usage/other-language-client-user-guide

## 带缓存的Http接口从Apollo读取配置
接口会从缓存中获取配置，适合频率较高的配置拉取请求，如简单的每30秒轮询一次配置
``` json
URL: {config_server_url}/configfiles/json/{appId}/{clusterName}/{namespaceName}?ip={clientIp}    
Method: GET

// Http Response 返回结果
{
    "portal.elastic.document.type":"biz",
    "portal.elastic.cluster.name":"hermes-es-fws"
}
```

## 不带缓存的Http接口从Apollo读取配置
接口会直接从数据库中获取配置，可以配合配置推送通知实现实时更新配置
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