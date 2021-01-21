# Server - ReleaseMessage实现方式
https://www.cnblogs.com/lewis09/p/10822020.html  

1. Admin Service在配置发布后会往ReleaseMessage表插入一条消息记录，消息内容就是配置发布的AppId+Cluster+Namespace，参见DatabaseMessageSender

2. Apollo.ReleaseMessageScanner线程会每秒扫描一次ReleaseMessage表，看看是否有新的消息记录，参见ReleaseMessageScanner

3. Config Service如果发现有新的消息记录，那么就会通知到所有的消息监听器（ReleaseMessageListener），如NotificationControllerV2，消息监听器的注册过程参见ConfigServiceAutoConfiguration

4. NotificationControllerV2得到配置发布的AppId+Cluster+Namespace后，会通知对应的客户端

# Client - NotificationControllerV2
https://blog.csdn.net/qq_26418435/article/details/102601560  
在得知有配置发布后通知客户端，实现如下
1. 客户端会发起一个Http请求到Config Service的notifications/v2接口，也就是NotificationControllerV2，参见RemoteConfigLongPollService
2. NotificationControllerV2不会立即返回结果，而是通过Spring DeferredResult把请求挂起
3. 如果在60秒内没有该客户端关心的配置发布，那么会返回Http状态码304给客户端
4. 如果有该客户端关心的配置发布，NotificationControllerV2会调用DeferredResult的setResult方法，传入有配置变化的namespace信息，同时该请求会立即返回。客户端从返回的结果中获取到配置变化的namespace后，会立即请求Config Service获取该namespace的最新配置

> Spring DeferredResult 不会直接返回配置结果,只是返回变更状态码,然后由Config Service 获取最新配置