# RibbonClientConfiguration
spring-cloud-netflix-core-1.2.5.RELEASE.jar

```java
@SuppressWarnings("deprecation")
@Configuration
@EnableConfigurationProperties
public class RibbonClientConfiguration {

	@Value("${ribbon.client.name}")
	private String name = "client";

	// TODO: maybe re-instate autowired load balancers: identified by name they could be
	// associated with ribbon clients

	@Autowired
	private PropertiesFactory propertiesFactory;

	@Bean
	@ConditionalOnMissingBean
	public IClientConfig ribbonClientConfig() {
		DefaultClientConfigImpl config = new DefaultClientConfigImpl();
		config.loadProperties(this.name);
		return config;
	}

	@Bean
	@ConditionalOnMissingBean
	public IRule ribbonRule(IClientConfig config) {
		if (this.propertiesFactory.isSet(IRule.class, name)) {
			return this.propertiesFactory.get(IRule.class, config, name);
		}
		ZoneAvoidanceRule rule = new ZoneAvoidanceRule();
		rule.initWithNiwsConfig(config);
		return rule;
	}

	@Bean
	@ConditionalOnMissingBean
	public IPing ribbonPing(IClientConfig config) {
		if (this.propertiesFactory.isSet(IPing.class, name)) {
			return this.propertiesFactory.get(IPing.class, config, name);
		}
		return new NoOpPing();
	}

	@Bean
	@ConditionalOnMissingBean
	@SuppressWarnings("unchecked")
	public ServerList<Server> ribbonServerList(IClientConfig config) {
		if (this.propertiesFactory.isSet(ServerList.class, name)) {
			return this.propertiesFactory.get(ServerList.class, config, name);
		}
		ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
		serverList.initWithNiwsConfig(config);
		return serverList;
	}

	@Configuration
	@ConditionalOnProperty(name = "ribbon.httpclient.enabled", matchIfMissing = true)
	protected static class HttpClientRibbonConfiguration {
		@Value("${ribbon.client.name}")
		private String name = "client";

		@Bean
		@ConditionalOnMissingBean(AbstractLoadBalancerAwareClient.class)
		public RibbonLoadBalancingHttpClient ribbonLoadBalancingHttpClient(
				IClientConfig config, ServerIntrospector serverIntrospector,
				ILoadBalancer loadBalancer, RetryHandler retryHandler) {
			RibbonLoadBalancingHttpClient client = new RibbonLoadBalancingHttpClient(
					config, serverIntrospector);
			client.setLoadBalancer(loadBalancer);
			client.setRetryHandler(retryHandler);
			Monitors.registerObject("Client_" + this.name, client);
			return client;
		}
	}

	@Configuration
	@ConditionalOnProperty("ribbon.okhttp.enabled")
	@ConditionalOnClass(name = "okhttp3.OkHttpClient")
	protected static class OkHttpRibbonConfiguration {
		@Value("${ribbon.client.name}")
		private String name = "client";

		@Bean
		@ConditionalOnMissingBean(AbstractLoadBalancerAwareClient.class)
		public OkHttpLoadBalancingClient okHttpLoadBalancingClient(IClientConfig config,
				ServerIntrospector serverIntrospector, ILoadBalancer loadBalancer,
				RetryHandler retryHandler) {
			OkHttpLoadBalancingClient client = new OkHttpLoadBalancingClient(config,
					serverIntrospector);
			client.setLoadBalancer(loadBalancer);
			client.setRetryHandler(retryHandler);
			Monitors.registerObject("Client_" + this.name, client);
			return client;
		}
	}

	@Configuration
	@RibbonAutoConfiguration.ConditionalOnRibbonRestClient
	protected static class RestClientRibbonConfiguration {
		@Value("${ribbon.client.name}")
		private String name = "client";

		/**
		 * Create a Netflix {@link RestClient} integrated with Ribbon if none already exists
		 * in the application context. It is not required for Ribbon to work properly and is
		 * therefore created lazily if ever another component requires it.
		 *
		 * @param config             the configuration to use by the underlying Ribbon instance
		 * @param loadBalancer       the load balancer to use by the underlying Ribbon instance
		 * @param serverIntrospector server introspector to use by the underlying Ribbon instance
		 * @param retryHandler       retry handler to use by the underlying Ribbon instance
		 * @return a {@link RestClient} instances backed by Ribbon
		 */
		@Bean
		@Lazy
		@ConditionalOnMissingBean(AbstractLoadBalancerAwareClient.class)
		public RestClient ribbonRestClient(IClientConfig config, ILoadBalancer loadBalancer,
										   ServerIntrospector serverIntrospector, RetryHandler retryHandler) {
			RestClient client = new OverrideRestClient(config, serverIntrospector);
			client.setLoadBalancer(loadBalancer);
			client.setRetryHandler(retryHandler);
			Monitors.registerObject("Client_" + this.name, client);
			return client;
		}
	}

	@Bean
	@ConditionalOnMissingBean
	public ILoadBalancer ribbonLoadBalancer(IClientConfig config,
			ServerList<Server> serverList, ServerListFilter<Server> serverListFilter,
			IRule rule, IPing ping) {
		if (this.propertiesFactory.isSet(ILoadBalancer.class, name)) {
			return this.propertiesFactory.get(ILoadBalancer.class, config, name);
		}
		ZoneAwareLoadBalancer<Server> balancer = LoadBalancerBuilder.newBuilder()
				.withClientConfig(config).withRule(rule).withPing(ping)
				.withServerListFilter(serverListFilter).withDynamicServerList(serverList)
				.buildDynamicServerListLoadBalancer();
		return balancer;
	}

	@Bean
	@ConditionalOnMissingBean
	@SuppressWarnings("unchecked")
	public ServerListFilter<Server> ribbonServerListFilter(IClientConfig config) {
		if (this.propertiesFactory.isSet(ServerListFilter.class, name)) {
			return this.propertiesFactory.get(ServerListFilter.class, config, name);
		}
		ZonePreferenceServerListFilter filter = new ZonePreferenceServerListFilter();
		filter.initWithNiwsConfig(config);
		return filter;
	}

	@Bean
	@ConditionalOnMissingBean
	public RibbonLoadBalancerContext ribbonLoadBalancerContext(
			ILoadBalancer loadBalancer, IClientConfig config, RetryHandler retryHandler) {
		return new RibbonLoadBalancerContext(loadBalancer, config, retryHandler);
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryHandler retryHandler(IClientConfig config) {
		return new DefaultLoadBalancerRetryHandler(config);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public ServerIntrospector serverIntrospector() {
		return new DefaultServerIntrospector();
	}

	@PostConstruct
	public void preprocess() {
		setRibbonProperty(name, DeploymentContextBasedVipAddresses.key(), name);
	}

	static class OverrideRestClient extends RestClient {

		private IClientConfig config;
		private ServerIntrospector serverIntrospector;

		protected OverrideRestClient(IClientConfig config,
				ServerIntrospector serverIntrospector) {
			super();
			this.config = config;
			this.serverIntrospector = serverIntrospector;
			initWithNiwsConfig(this.config);
		}

		@Override
		public URI reconstructURIWithServer(Server server, URI original) {
			URI uri = updateToHttpsIfNeeded(original, this.config, this.serverIntrospector, server);
			return super.reconstructURIWithServer(server, uri);
		}

		@Override
		protected Client apacheHttpClientSpecificInitialization() {
			ApacheHttpClient4 apache = (ApacheHttpClient4) super
					.apacheHttpClientSpecificInitialization();
			apache.getClientHandler()
					.getHttpClient()
					.getParams()
					.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
			return apache;
		}

	}

}

```