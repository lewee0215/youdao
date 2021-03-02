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

