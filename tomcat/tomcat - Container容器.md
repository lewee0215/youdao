# Tomcat - Container
https://blog.csdn.net/shiziaishuijiao/article/details/25929147
处理某类型请求的容器，处理的方式一般为把处理请求的处理器包装为Valve对象，并按一定顺序放入类型为Pipeline的管道里。

Container有多种子类型：Engine、Host、Context和Wrapper，这几种子类型Container依次包含，处理不同粒度的请求。另外Container里包含一些基础服务，如Loader、Manager和Realm

Engine：Engine包含Host和Context，接到请求后仍给相应的Host在相应的Context里处理。

Host：就是我们所理解的虚拟主机。

Context：就是我们所部属的具体Web应用的上下文，每个请求都在是相应的上下文里处理的。

Wrapper：Wrapper是针对每个Servlet的Container，每个Servlet都有相应的Wrapper来管理

Engine包含多个Host，Host包含多个Context，Context包含多个Wrapper，每个Wrapper对应一个Servlet

## Host 虚拟主机
https://www.jianshu.com/p/1a2d2f868143  
http协议从1.1开始，支持在请求头里面添加Host字段用来表示请求的域名。DNS域名解析的时候，可以将不同的域名解析到同一个ip或者主机

![](https://upload-images.jianshu.io/upload_images/845143-51594604c13bf484.png?imageMogr2/auto-orient/strip|imageView2/2/w/640/format/webp)

tomcat里面支持多域名，我们需要在server.xml文件里面的Engine标签下面添加多个Host标签
```xml
<Host name="www.ramki.com" appbase="ramki_webapps" />
<Host name="www.krishnan.com" appbase="krishnan_webapps" /> 
<Host name="www.blog.ramki.com" appbase="blog_webapps" /> 
```

## Context
同一个Host里面不同的Context，其contextPath必须不同，默认Context的contextPath为空格("")或斜杠(/)

## Wrapper
Wrapper是一个Servlet的包装, 默认为 