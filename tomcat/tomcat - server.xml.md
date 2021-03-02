# Server.xml 核心组件介绍
https://www.cnblogs.com/mharvay/p/13700291.html  

Server：最顶层元素，而且唯一，代表整个tomcat容器。一个Server元素包含一个或者多个Service元素；

Service：对外提供服务的。一个Service元素包含多个Connector元素，但是只能包含一个Engine元素；

Connector：接收连接请求，创建Request和Response对象用于和请求端交换数据；然后分配线程让Engine来处理这个请求，并把产生的Request和Response对象传给Engine

Engine：Engine组件在Service组件中有且只有一个；Engine是Service组件中的请求处理组件。Engine组件从一个或多个Connector中接收请求并处理，并将完成的响应返回给Connector，最终传递给客户端。

Host：代表特定的虚拟主机
```xml
<Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true">
```
name：虚拟主机的主机名。比如 localhost 表示本机名称，实际应用时应该填写具体域名，比如 www.dog.com ，当然如果该虚拟主机是给内部人员访问的，也可以直接填写服务器的 ip 地址，比如 192.168.1.101；

appBase：设置 Web 应用程序组的路径。appBase 属性的值可以是相对于 Tomcat 安装目录的相对路径，也可以是绝对路径，需要注意的是该路径必须是 Tomcat 有权限访问的；

unpackWARs：是否自动展开war压缩包再运行Web应用程序，默认值为true；

autoDeplay：是否允许自动部署，默认值是 true，表示 Tomcat 会自动检测 appBase 目录下面的文件变化从而自动应用到正在运行的 Web 应用程序；

deployOnStartup：为true时，表示Tomcat在启动时检查Web应用，且检测到的所有Web应用视作新应用；

Context：该元素代表在特定虚拟主机Host上运行的一个Web应用，它是Host的子容器，每个Host容器可以定义多个Context元素。静态部署Web应用时使
```xml
<Context path="/" docBase="E:\Resource\test.war" reloadable="true"/>
```
path：浏览器访问时的路径名，只有当自动部署完全关闭(deployOnStartup和autoDeploy都为false)或docBase不在appBase中时，才可以设置path属性。

docBase：静态部署时，docBase可以在appBase目录下，也可以不在；本例中，不在appBase目录下。

reloadable：设定项目有改动时，重新加载该项目

