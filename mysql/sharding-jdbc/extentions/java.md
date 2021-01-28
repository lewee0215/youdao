# SpringBoot 2.0 整合sharding-jdbc中间件
https://www.cnblogs.com/cicada-smile/p/10970626.html  

```java
/**
 * 数据库映射计算
 */
public class DataSourceAlg implements PreciseShardingAlgorithm<String> {

    private static Logger LOG = LoggerFactory.getLogger(DataSourceAlg.class);
    @Override
    public String doSharding(Collection<String> names, PreciseShardingValue<String> value) {
        LOG.debug("分库算法参数 {},{}",names,value);
        int hash = HashUtil.rsHash(String.valueOf(value.getValue()));
        return "ds_" + ((hash % 2) + 2) ;
    }
}

/**
 * 分表算法
 */
public class TableOneAlg implements PreciseShardingAlgorithm<String> {
    private static Logger LOG = LoggerFactory.getLogger(TableOneAlg.class);
    /**
     * 该表每个库分5张表
     */
    @Override
    public String doSharding(Collection<String> names, PreciseShardingValue<String> value) {
        LOG.debug("分表算法参数 {},{}",names,value);
        int hash = HashUtil.rsHash(String.valueOf(value.getValue()));
        return "table_one_" + (hash % 5+1);
    }
}

/**
 * 分表算法
 */
public class TableTwoAlg implements PreciseShardingAlgorithm<String> {
    private static Logger LOG = LoggerFactory.getLogger(TableTwoAlg.class);
    /**
     * 该表每个库分5张表
     */
    @Override
    public String doSharding(Collection<String> names, PreciseShardingValue<String> value) {
        LOG.debug("分表算法参数 {},{}",names,value);
        int hash = HashUtil.rsHash(String.valueOf(value.getValue()));
        return "table_two_" + (hash % 5+1);
    }
}

/**
 * 数据库分库分表配置
 */
@Configuration
public class ShardJdbcConfig {
    // 省略了 druid 配置，源码中有
    /**
     * Shard-JDBC 分库配置
     */
    @Bean
    public DataSource dataSource (@Autowired DruidDataSource dataOneSource,
                                  @Autowired DruidDataSource dataTwoSource,
                                  @Autowired DruidDataSource dataThreeSource) throws Exception {
        ShardingRuleConfiguration shardJdbcConfig = new ShardingRuleConfiguration();
        shardJdbcConfig.getTableRuleConfigs().add(getTableRule01());
        shardJdbcConfig.getTableRuleConfigs().add(getTableRule02());
        shardJdbcConfig.setDefaultDataSourceName("ds_0");
        Map<String,DataSource> dataMap = new LinkedHashMap<>() ;
        dataMap.put("ds_0",dataOneSource) ;
        dataMap.put("ds_2",dataTwoSource) ;
        dataMap.put("ds_3",dataThreeSource) ;
        Properties prop = new Properties();
        return ShardingDataSourceFactory.createDataSource(dataMap, shardJdbcConfig, new HashMap<>(), prop);
    }

    /**
     * Shard-JDBC 分表配置
     */
    private static TableRuleConfiguration getTableRule01() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("table_one");
        result.setActualDataNodes("ds_${2..3}.table_one_${1..5}");
        result.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("phone", new DataSourceAlg()));
        result.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("phone", new TableOneAlg()));
        return result;
    }
    private static TableRuleConfiguration getTableRule02() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("table_two");
        result.setActualDataNodes("ds_${2..3}.table_two_${1..5}");
        result.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("phone", new DataSourceAlg()));
        result.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("phone", new TableTwoAlg()));
        return result;
    }
}

```