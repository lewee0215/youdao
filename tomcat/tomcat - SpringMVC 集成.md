# Tomcat 部署 SpringMVC 应用
https://cloud.tencent.com/developer/article/1476782

一般在web.xml里面会配置一个listener和一个dispatcher，其实这就配置了两个spring IOC容器，并且dispatcher容器的父容器就是listener的容器。一般在web.xml里面配置如下：
```xml
<!-- ContextLoaderListener会创建一个XMLWebApplicationContext上下文来管理contextConfigLocation配置的xml里面的普通bean -->
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>

<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>WEB-INF/applicationContext.xml</param-value>
</context-param>

<!--DispatcherServlet也会创建一个XMLWebApplicationContext默认管理web-info/springmvc-servlet.xml里面的Controller bean -->
<servlet>
    <servlet-name>springmvc</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
```