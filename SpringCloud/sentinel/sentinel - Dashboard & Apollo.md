# Sentinel-开源版本Dashboard集成Apollo配置中心
https://www.cnblogs.com/kiwifly/p/11569190.html

## 限流规则
![](https://s3cdn.pahx.com/mgm/36fe8694a0809456712dac7f7be626fe.jpg)

## 降级规则
![](https://s3cdn.pahx.com/mgm/47754a7c817f2552d3a723b617bfc82d.jpg)

## 系统规则
![](https://s3cdn.pahx.com/mgm/4d355761e46aa0c2b8a74428517927c7.jpg)

## 授权规则
![](https://s3cdn.pahx.com/mgm/c4d3f37bac5684475906bb313e4a530d.jpg)

官方版本Sentinel Dashboard 所有的限流规则配置都存储在内存中，重启会丢失  
针对Dashboard做修改，关键在于两点
1. Dashboard中规则查询接口，要改为从配置中心拉取(规则拉取)
2. Dashboard中规则保存接口，要改为推送配置到配置中心(规则推送)