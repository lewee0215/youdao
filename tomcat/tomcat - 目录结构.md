# Tomcat 目录结构详解
https://www.cnblogs.com/mharvay/p/13700291.html 
![](https://img2020.cnblogs.com/blog/1846981/202009/1846981-20200920143542294-1365413144.png)

## bin 目录
存放一些可执行的二进制文件，.sh结尾的为linux下执行命令，.bat结尾的为windows下执行命
* catalina.sh：真正启动tomcat文件，可以在里面设置jvm参数。
* startup.sh：启动tomcat（需事先配置好JAVA_HOME环境变量才可启动，该命令源码实际执行的为catalina.sh start）。
* shutdown.sh：关闭tomcat。
* version.sh：查看tomcat版本相关信息

## conf 目录
![](https://img2020.cnblogs.com/blog/1846981/202009/1846981-20200920154154200-981309146.png)

* catalina.policy 项目安全文件  
用来防止欺骗代码或JSP执行带有像System.exit(0)这样的命令，可能影响容器的破坏。 只有当Tomcat用-security命令行参数启动时这个文件才会被使用，即启动tomcat时， startup.sh -security 

* catalina.properties  
配置tomcat启动相关信息文件

* context.xml
监视并加载资源文件，当监视文件发生变化时，自动加载，通常不会去配置

* jsspic-providers.xml & jaspic-providers.xsd  
不常用文件

* logging.properties  
tomcat日志文件配置，包括输出格式、日志级别

* server.xml  
核心配置文件：修改端口号，添加编码格式等

* tomcat-users.xml & tomcat-users.xsd  
tomcat用户配置文件，配置用户名，密码，用户具备权限

* web.xml  
配置servlet  
添加过滤器，比如过滤敏感词汇  
设置session过期时间，tomcat默认30分钟  
配置系统欢迎页  


## webapps 目录
用来存放应用程序，可以以文件夹、war包、jar包的形式发布应用

## work 目录
用于存放tomcat在运行时的编译后文件（清空该目录下所有内容，重启tomcat，可达到清除缓冲的作用）