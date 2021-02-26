# Sentinel - JDBC DataSource
https://blog.csdn.net/weixin_33940102/article/details/91665317  

# Sentinel - File DataSource
https://blog.csdn.net/qq_29064815/article/details/107163048

# Sentinel - 规则持久化
https://blog.csdn.net/weixin_34357962/article/details/87993984  
PS：需要注意的是，我们需要在系统启动的时候调用该数据源注册的方法，否则不会生效的。
具体的方式有多种实现方式
1. Spring 来初始化该方法
2. 自定义一个类来实现 Sentinel 中的 InitFunc 接口来完成初始化




