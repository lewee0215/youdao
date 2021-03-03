# Server.xml 核心组件介绍
https://www.cnblogs.com/mharvay/p/13700291.html  
```xml
<!-- https://blog.csdn.net/xuheng8600/article/details/81661039 -->
<!-- 基础配置 -->
<Server>
    <Listener />
    <GlobaNamingResources><GlobaNamingResources/>
    <Service>
        <Connector />
        <Engine>
            <Logger />
            <Realm />
               <host>
                   <Logger />
                   <Context />
               </host>
        </Engine>
    </Service>
</Server>
```

## Server
Server：最顶层元素，而且唯一，代表整个tomcat容器。一个Server元素包含一个或者多个Service元素；
```xml
<Server port="8005" shutdown="SHUTDOWN">
<!--
port: 指定一个端口，这个端口负责监听关闭tomcat的请求
shutdown: 指定向端口发送的命令字符串
-->
```

## Service
Service：对外提供服务的。一个Service元素包含多个Connector元素，但是只能包含一个Engine元素；
```xml
<Service name="Catalina">
<!--
name: 指定service的名字
-->
```

## Connector
Connector：接收连接请求，创建Request和Response对象用于和请求端交换数据；然后分配线程让Engine来处理这个请求，并把产生的Request和Response对象传给Engine
```xml
<Connector port="8080" protocol="HTTP/1.1"
            connectionTimeout="20000"
            redirectPort="8443" />
<!--
port: 指定服务器端要创建的端口号，并在这个断口监听来自客户端的请求
minProcessors: 服务器启动时创建的处理请求的线程数
maxProcessors: 最大可以创建的处理请求的线程数
enableLookups: 如果为true，则可以通过调用request.getRemoteHost()进行DNS查询来得到远程客户端的实际主机名，若为false则不进行DNS查询，而是返回其ip地址
redirectPort: 指定服务器正在处理http请求时收到了一个SSL传输请求后重定向的端口号
acceptCount: 指定当所有可以使用的处理请求的线程数都被使用时，可以放到处理队列中的请求数，超过这个数的请求将不予处理
connectionTimeout: 指定超时的时间数(以毫秒为单位)
-->
```

## Engine
Engine：Engine组件在Service组件中有且只有一个；Engine是Service组件中的请求处理组件。Engine组件从一个或多个Connector中接收请求并处理，并将完成的响应返回给Connector，最终传递给客户端。
```xml
<Engine name="Catalina" defaultHost="localhost">
<!--
defaultHost: 指定缺省的处理请求的主机名，它至少与其中的一个host元素的name属性值是一样的
-->
```

Host：代表特定的虚拟主机
```xml
<Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true">
<!--
name：虚拟主机的主机名。比如 localhost 表示本机名称，实际应用时应该填写具体域名，比如 www.dog.com ，当然如果该虚拟主机是给内部人员访问的，也可以直接填写服务器的 ip 地址，比如 192.168.1.101；
appBase：设置 Web 应用程序组的路径。appBase 属性的值可以是相对于 Tomcat 安装目录的相对路径，也可以是绝对路径，需要注意的是该路径必须是 Tomcat 有权限访问的；
unpackWARs：是否自动展开war压缩包再运行Web应用程序，默认值为true；
autoDeplay：是否允许自动部署，默认值是 true，表示 Tomcat 会自动检测 appBase 目录下面的文件变化从而自动应用到正在运行的 Web 应用程序；
deployOnStartup：为true时，表示Tomcat在启动时检查Web应用，且检测到的所有Web应用视作新应用；
-->
```

Context：该元素代表在特定虚拟主机Host上运行的一个Web应用，它是Host的子容器，每个Host容器可以定义多个Context元素。静态部署Web应用时使
```xml
<Context path="/" docBase="E:\Resource\test.war" reloadable="true"/>
<!--
path：浏览器访问时的路径名，只有当自动部署完全关闭(deployOnStartup和autoDeploy都为false)或docBase不在appBase中时，才可以设置path属性。
docBase：静态部署时，docBase可以在appBase目录下，也可以不在；本例中，不在appBase目录下。
reloadable：设定项目有改动时，重新加载该项目
-->
```

# Tomcat Server 处理 Http 请求流程
https://blog.csdn.net/xuheng8600/article/details/81661039  
假设来自客户的请求为：http://localhost:8080/wsota/wsota_index.jsp
1) 请求被发送到本机端口8080，被在那里侦听的Coyote HTTP/1.1 Connector获得
2) Connector把该请求交给它所在的Service的Engine来处理，并等待来自Engine的回应
3) Engine获得请求localhost/wsota/wsota_index.jsp，匹配它所拥有的所有虚拟主机Host
4) Engine匹配到名为localhost的Host（即使匹配不到也把请求交给该Host处理，因为该Host被定义为该Engine的默认主机）
5) localhost Host获得请求/wsota/wsota_index.jsp，匹配它所拥有的所有Context
6) Host匹配到路径为/wsota的Context（如果匹配不到就把该请求交给路径名为""的Context去处理）
7) path="/wsota"的Context获得请求/wsota_index.jsp，在它的mapping table中寻找对应的servlet
8) Context匹配到URL PATTERN为*.jsp的servlet，对应于JspServlet类
9) 构造HttpServletRequest对象和HttpServletResponse对象，作为参数调用JspServlet的doGet或doPost方法
10) Context把执行完了之后的HttpServletResponse对象返回给Host
11) Host把HttpServletResponse对象返回给Engine
12) Engine把HttpServletResponse对象返回给Connector
13) Connector把HttpServletResponse对象返回给客户browser
