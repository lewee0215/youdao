# Sharding-JDBC 4种 分片策略
https://www.cnblogs.com/chengxy-nds/p/13919981.html  

## 标准分片策略 StandardShardingStrategy
使用场景：SQL 语句中有>，>=, <=，<，=，IN 和 BETWEEN AND 操作符，都可以应用此分片策略

标准分片策略（StandardShardingStrategy），只支持对单个分片健（字段）为依据的分库分表，并提供了两种分片算法 PreciseShardingAlgorithm（精准分片）和 RangeShardingAlgorithm（范围分片）


## 复合分片策略
使用场景：SQL 语句中有>，>=, <=，<，=，IN 和 BETWEEN AND 等操作符，不同的是复合分片策略支持对多个分片健操作
```java
public class MyDBComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Integer> {
    @Override
    public Collection<String> doSharding(Collection<String> databaseNames, ComplexKeysShardingValue<Integer> complexKeysShardingValue) {

        // 得到每个分片健对应的值
        Collection<Integer> orderIdValues = this.getShardingValue(complexKeysShardingValue, "order_id");
        Collection<Integer> userIdValues = this.getShardingValue(complexKeysShardingValue, "user_id");

        List<String> shardingSuffix = new ArrayList<>();
        // 对两个分片健同时取模的方式分库
        for (Integer userId : userIdValues) {
            for (Integer orderId : orderIdValues) {
                String suffix = userId % 2 + "_" + orderId % 2;
                for (String databaseName : databaseNames) {
                    if (databaseName.endsWith(suffix)) {
                        shardingSuffix.add(databaseName);
                    }
                }
            }
        }
        return shardingSuffix;
    }

    private Collection<Integer> getShardingValue(ComplexKeysShardingValue<Integer> shardingValues, final String key) {
        Collection<Integer> valueSet = new ArrayList<>();
        Map<String, Collection<Integer>> columnNameAndShardingValuesMap = shardingValues.getColumnNameAndShardingValuesMap();
        if (columnNameAndShardingValuesMap.containsKey(key)) {
            valueSet.addAll(columnNameAndShardingValuesMap.get(key));
        }
        return valueSet;
    }
}
```
## 行表达式分片策略
行表达式分片策略（InlineShardingStrategy），在配置中使用 Groovy 表达式，提供对 SQL语句中的 = 和 IN 的分片操作支持，它只支持单分片健

```java
// 比如：ds-$->{order_id % 2} 表示对 order_id 做取模计算，$ 是个通配符用来承接取模结果，最终计算出分库ds-0 ··· ds-n

// 行表达式分片键
sharding.jdbc.config.sharding.tables.t_order.database-strategy.inline.sharding-column=order_id
// 表达式算法
sharding.jdbc.config.sharding.tables.t_order.database-strategy.inline.algorithm-expression=ds-$->{order_id % 2}
```

## Hint分片策略
Hint分片策略（HintShardingStrategy）,让 SQL在指定的分库、分表中执行。ShardingSphere 通过 Hint API实现指定操作，实际上就是把分片规则tablerule 、databaserule由集中配置变成了个性化配置

```java
/** 使用 Hint分片策略同样需要自定义，实现 HintShardingAlgorithm 接口并重写 doSharding()方法 */
public class MyTableHintShardingAlgorithm implements HintShardingAlgorithm<String> {

    @Override
    public Collection<String> doSharding(Collection<String> tableNames, HintShardingValue<String> hintShardingValue) {

        Collection<String> result = new ArrayList<>();
        for (String tableName : tableNames) {
            for (String shardingValue : hintShardingValue.getValues()) {
                if (tableName.endsWith(String.valueOf(Long.valueOf(shardingValue) % tableNames.size()))) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
}
```

在调用 SQL 前通过 HintManager 指定分库、分表信息。由于每次添加的规则都放在 ThreadLocal 内，所以要先执行 clear() 清除掉上一次的规则，否则会报错；
addDatabaseShardingValue 设置分库分片健键值，addTableShardingValue设置分表分片健键值。setMasterRouteOnly 读写分离强制读主库，避免造成主从复制导致的延迟

```java
// 清除掉上一次的规则，否则会报错
HintManager.clear();
// HintManager API 工具类实例
HintManager hintManager = HintManager.getInstance();
// 直接指定对应具体的数据库
hintManager.addDatabaseShardingValue("ds",0);
// 设置表的分片健
hintManager.addTableShardingValue("t_order" , 0);
hintManager.addTableShardingValue("t_order" , 1);
hintManager.addTableShardingValue("t_order" , 2);

// 在读写分离数据库中，Hint 可以强制读主库
hintManager.setMasterRouteOnly();
```