# SkyWalking Server 启动流程
执行startup.bat之后会启动如下两个服务：
1. Skywalking-Collector：追踪信息收集器，通过 gRPC/Http 收集客户端的采集信息 ，Http默认端口 12800，gRPC默认端口 11800
2. Skywalking-Webapp：管理平台页面 默认端口 8080，登录信息 admin/admin

# SkyWalking Agent 启动流程
https://blog.csdn.net/lewee0215/article/details/109636349
SkyWalkingAgent 类的 premain() 方法，其中完成了 Agent 启动的流程：
1. 初始化配置信息。该步骤中会加载 agent.config 配置文件，其中会检测 Java Agent 参数以及环境变量是否覆盖了相应配置项。
2. 查找并解析 skywalking-plugin.def 插件文件。
3. AgentClassLoader 加载插件。
4. PluginFinder 对插件进行分类管理。
5. 使用 Byte Buddy 库创建 AgentBuilder。这里会根据已加载的插件动态增强目标类，插入埋点逻辑。
6. 使用 JDK SPI 加载并启动 BootService 服务。BootService 接口的实现会在后面的课时中展开详细介绍。
7. 添加一个 JVM 钩子，在 JVM 退出时关闭所有 BootService 服务