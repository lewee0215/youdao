# Cache Java SDK 使用方法
https://www.jb51.net/article/172941.htm

## Maven Dependency
```xml
<dependency>
    <groupId>com.sohu.tv</groupId>
    <artifactId>cachecloud-open-client-redis</artifactId>
    <version>1.0</version>
</dependency>
<repositories>
    <repository>
        <id>sohu.nexus</id>
        <url>http://your_maven_house</url>
    </repository>
</repositories>
```

## \src\main\resources\cacheCloudClient.properties
https://www.jb51.net/article/172941.htm  
配置文件  cacheCloudClient.properties，启动项目时  VM参数追加 -Dcachecloud.config= 配置文件路径  

```properties
http_conn_timeout = 3000
http_socket_timeout = 5000
client_version = 1.0-SNAPSHOT
domain_url = http://192.168.33.221:8585  #cachecloud实际路径
redis_cluster_suffix = /cache/client/redis/cluster/%s.json?clientVersion=
redis_sentinel_suffix = /cache/client/redis/sentinel/%s.json?clientVersion=
redis_standalone_suffix = /cache/client/redis/standalone/%s.json?clientVersion=
cachecloud_report_url = /cachecloud/client/reportData.json
```

## Java Demo Code
```java
long appId = 10150;
/**
* 自定义配置
*/
GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
JedisSentinelPool sentinelPool = ClientBuilder.redisSentinel(appId)
    .setConnectionTimeout(1000)
    .setSoTimeout(1000)
    .setPoolConfig(poolConfig)
    .build();
Jedis jedis = null;
try {
    jedis = sentinelPool.getResource();
    System.out.println(jedis.get("key1"));
    //1.字符串value
    jedis.set("key1", "1");
    
} catch (Exception e) {
    e.printStackTrace();
} finally {
    if(jedis!=null)
    jedis.close();
}
```

## Rest API
https://blog.csdn.net/weixin_44220158/article/details/102619341  
```yml
运行模式 appType=cluster|sentinel|standalone

Rest API 格式
http://your.domain.com/cache/client/redis/{sentinel}/{10150}.json?clientVersion={1.0}

// cluster 接口返回值
{
  "shardNum": 10,
  "shardInfo": "10.10.xx.xx:6390,10.10.xx.xx:6382 10.10.xx.xx:6387,10.10.xx.xx:6379 10.10.xx.xx:6387,10.10.xx.xx:7382 10.10.xx.xx:6380,10.10.xx.xx:6392",

  "message": "client is up to date, Cheers!",
  "status": 1,
  "appId": 10192
}

// sentinel 接口返回值
{
    "sentinels": "ip:port ip:port ip:port",
    "message": "appId:10086 client is up to date, Cheers!",
    "status": 1,
    "masterName": "sentinel-ip-port",
    "appId": 10086
}

// standlone 接口返回值
{
    "standlone": "ip:port",
    "message": "appId:10086 client is up to date, Cheers!",
    "status": 1,
    "password": "password",
    "appId": 10086
}
```
