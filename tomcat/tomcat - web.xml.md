# Tomcat - web.xml
https://blog.csdn.net/msl1348498595/article/details/79882930  
tomcat中加载顺序: listener -> filter -> servlet

## servlet 配置
当tomcat部署应用程序时（在激活过程中，或加载应用程序后），它都会读取通用的conf/web.xml，然后再读取web应用程序中的WEB-INF/web.xml

```xml
<!-- conf/web.xml 默认 servlet 配置-->
<servlet>
    <servlet-name>default</servlet-name>
    <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
    <init-param>
        <param-name>debug</param-name>
        <param-value>0</param-value>
    </init-param>
    <init-param>
        <param-name>listings</param-name>
        <param-value>false</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
```
conf/web.xml文件中的设定会应用于所有的web应用程序，而某些web应用程序的WEB-INF/web.xml中的设定只应用于该应用程序本身

## filter 配置
```xml
<filter>
    <filter-name>setCharacterEncodingFilter</filter-name>
    <filter-class>org.apache.catalina.filters.SetCharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
    <async-supported>true</async-supported>
</filter>
```

## listener 配置
```xml
<listener>    
      <listerner-class>listener.SessionListener</listener-class>    
</listener>  
```

## web.xml 其他常用配置
https://www.cnblogs.com/c0liu/p/7468787.html
```xml
<web-app> 

<display-name></display-name>
定义了WEB应用的名字 

<description></description> 
声明WEB应用的描述信息 
  
<context-param></context-param> 
context-param元素声明应用范围内的初始化参数。 

<filter></filter> 
过滤器元素将一个名字与一个实现javax.servlet.Filter接口的类相关联。 

<filter-mapping></filter-mapping> 
一旦命名了一个过滤器, 就要利用filter-mapping元素把它与一个或多个servlet或JSP页面相关联。 

<listener></listener>
servlet API的版本2.3增加了对事件监听程序的支持, 事件监听程序在建立、修改和删除会话或servlet环境时得到通知。 Listener元素指出事件监听程序类。 

<servlet></servlet> 
在向servlet或JSP页面制定初始化参数或定制URL时, 必须首先命名servlet或JSP页面。Servlet元素就是用来完成此项任务的。 

<servlet-mapping></servlet-mapping> 
服务器一般为servlet提供一个缺省的URL：http://host/webAppPrefix/servlet/ServletName。
但是, 常常会更改这个URL, 以便servlet可以访问初始化参数或更容易地处理相对URL。在更改缺省URL时, 使用servlet-mapping元素。 
  
<session-config></session-config> 
如果某个会话在一定时间内未被访问, 服务器可以抛弃它以节省内存。 可通过使用HttpSession的setMaxInactiveInterval方法明确设置单个会话对象的超时值, 或者可利用session-config元素制定缺省超时值。 
  
<mime-mapping></mime-mapping>
如果Web应用具有想到特殊的文件, 希望能保证给他们分配特定的MIME类型, 则mime-mapping元素提供这种保证。 

<welcome-file-list></welcome-file-list> 
指示服务器在收到引用一个目录名而不是文件名的URL时, 使用哪个文件。 

<error-page></error-page> 
在返回特定HTTP状态代码时, 或者特定类型的异常被抛出时, 能够制定将要显示的页面。 

<taglib></taglib> 
对标记库描述符文件（Tag Libraryu Descriptor file）指定别名。此功能使你能够更改TLD文件的位置,  而不用编辑使用这些文件的JSP页面。 

<resource-env-ref></resource-env-ref>
声明与资源相关的一个管理对象。 

<resource-ref></resource-ref> 
声明一个资源工厂使用的外部资源。 

<security-constraint></security-constraint> 
制定应该保护的URL。它与login-config元素联合使用 

<login-config></login-config> 
指定服务器应该怎样给试图访问受保护页面的用户授权。它与sercurity-constraint元素联合使用。 

<security-role></security-role>
给出安全角色的一个列表, 这些角色将出现在servlet元素内的security-role-ref元素 的role-name子元素中。分别地声明角色可使高级IDE处理安全信息更为容易。 

<env-entry></env-entry>
声明Web应用的环境项。 

<ejb-ref></ejb-ref>
声明一个EJB的主目录的引用。 

<ejb-local-ref></ejb-local-ref>
声明一个EJB的本地主目录的应用。 

</web-app> 
```

