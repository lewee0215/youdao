## nacos-discovery-spring-boot-starter  pom.xml依赖
```java
<!-- Nacos -->
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-spring-context</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-discovery-spring-boot-autoconfigure</artifactId>
</dependency>
/** SPI文件: spring.facoties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.alibaba.boot.nacos.discovery.autoconfigure.NacosDiscoveryAutoConfiguration
*/

<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-spring-boot-base</artifactId>
</dependency>
```

