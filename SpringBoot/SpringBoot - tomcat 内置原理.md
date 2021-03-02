# SpringBoot 内置 Tomcat
https://www.cnblogs.com/sword-successful/p/11383723.html  

tomcat的启动主要是实例化两个组件：Connector、Container  
一个tomcat实例就是一个Server，一个Server包含多个Service，也就是多个应用程序，每个Service包含多个Connector和一个Container，而一个Container下又包含多个子容器

```java
//ServletWebServerApplicationContext.java
@Override
protected void onRefresh() {
	super.onRefresh();
	try {
		this.createWebServer();
	} catch (Throwable var2) {
		
	}
}

//ServletWebServerApplicationContext.java
//这里是创建webServer，但是还没有启动tomcat，通过ServletWebServerFactory创建
private void createWebServer() {
	WebServer webServer = this.webServer;
	ServletContext servletContext = this.getServletContext();
	if (webServer == null && servletContext == null) {
		ServletWebServerFactory factory = this.getWebServerFactory();
		this.webServer = factory.getWebServer(new ServletContextInitializer[]{this.getSelfInitializer()});
	} else if (servletContext != null) {
		try {
			this.getSelfInitializer().onStartup(servletContext);
		} catch (ServletException var4) {
		
		}
	}

	this.initPropertySources();
}

/* 接口
AbstractServletWebServerFactory
JettyServletWebServerFactory
TomcatServletWebServerFactory
UndertowServletWebServerFactory */
public interface ServletWebServerFactory {
    WebServer getWebServer(ServletContextInitializer... initializers);
}
```

## TomcatServletWebServerFactory
```java
//TomcatServletWebServerFactory.java
//这里我们使用的tomcat，所以我们查看TomcatServletWebServerFactory。到这里总算是看到了tomcat的踪迹。
@Override
public WebServer getWebServer(ServletContextInitializer... initializers) {
	Tomcat tomcat = new Tomcat();
	File baseDir = (this.baseDirectory != null) ? this.baseDirectory : createTempDir("tomcat");
	tomcat.setBaseDir(baseDir.getAbsolutePath());
    //创建Connector对象
	Connector connector = new Connector(this.protocol);
	tomcat.getService().addConnector(connector);
	customizeConnector(connector);
	tomcat.setConnector(connector);
	tomcat.getHost().setAutoDeploy(false);
	configureEngine(tomcat.getEngine());
	for (Connector additionalConnector : this.additionalTomcatConnectors) {
		tomcat.getService().addConnector(additionalConnector);
	}
	prepareContext(tomcat.getHost(), initializers);
	return getTomcatWebServer(tomcat);
}

protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
	return new TomcatWebServer(tomcat, getPort() >= 0);
}

//Tomcat.java
public Engine getEngine() {
    Service service = getServer().findServices()[0];
    if (service.getContainer() != null) {
        return service.getContainer();
    }
    Engine engine = new StandardEngine();
    engine.setName( "Tomcat" );
    engine.setDefaultHost(hostname);
    engine.setRealm(createDefaultRealm());
    service.setContainer(engine);
    return engine;
}
// 容器等级
// Engine -> Host  -> Context  ->  Wrapper
```