# Sentinel-开源版本Dashboard集成Apollo配置中心
https://www.cnblogs.com/kiwifly/p/11569190.html

官方版本Sentinel Dashboard 所有的限流规则配置都存储在内存中，重启会丢失  
针对Dashboard做修改，关键在于两点
1. Dashboard中规则查询接口，要改为从配置中心拉取(规则拉取)
2. Dashboard中规则保存接口，要改为推送配置到配置中心(规则推送)