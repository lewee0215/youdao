package com.liuwei.springboot.algorithm.ratelimit;

import java.util.concurrent.ConcurrentHashMap;

public class RedisLimitter {
	
	public static void main(String[] args) {
		RedisLimitter limitter = new RedisLimitter();
		RateDataStore dataStore  = new RateLimitterLocalStore();
		boolean checkResult = limitter.rateLimit("uuid",1d,10d,50d,dataStore);
		System.out.println(checkResult);
	}
	
	/**
	 * 
	 * @param requestkey 请求唯一标识
	 * @param requested	 请求量
	 * @param rate	令牌桶填充平均速率，单位：秒
	 * @param capacity	 令牌桶上限
	 * @return
	 */
	public boolean rateLimit(String requestkey, double requested,double rate,double capacity,RateDataStore dataStore) {
		// https://blog.csdn.net/weixin_42073629/article/details/106934827
		String tokens_key = String.format("request_rate_limiter.%s.tokens", requestkey);  		// 令牌桶剩余令牌数
		String timestamp_key = String.format("request_rate_limiter.%s.timestamp", requestkey);	// 令牌桶最后填充令牌时间，单位：秒
		
		// double rate = 10;  		// 令牌桶填充平均速率，单位：秒
		// double capacity = 50;  	// 令牌桶上限
		double now = System.currentTimeMillis();	// 当前时间戳
		// double requested = 1;	// 请求量
		
		// 计算令牌桶填充满令牌需要多久时间，单位：秒
		// 如果是Redis * 2 保证时间充足,  如果设置永不过期也不影响功能
		double fill_time = capacity/rate;			
		double ttl = Math.floor(fill_time*2);  		
		
		// 获得令牌桶剩余令牌数( last_tokens ) 
		Double last_tokens = dataStore.getRateData(tokens_key);	
		if(last_tokens == null) last_tokens = capacity;
		
		// 令牌桶最后填充令牌时间(last_refreshed) 
		Double last_refreshed = dataStore.getRateData(timestamp_key);
		if(last_refreshed == null) last_refreshed = 0d;
		
		// 填充令牌，计算新的令牌桶剩余令牌数( filled_tokens )。填充不超过令牌桶令牌上限
		double delta = Math.max(0, now-last_refreshed);
		double filled_tokens = Math.min(capacity, last_tokens+(delta*rate));
		
		boolean allowed = filled_tokens >= requested;
		double new_tokens = filled_tokens;
		double allowed_num = 0;
		if(allowed) {
			new_tokens = filled_tokens - requested;
			allowed_num = requested;
		}
		// redis.call("setex", tokens_key, ttl, new_tokens)
		// redis.call("setex", timestamp_key, ttl, now)
		// return { allowed_num, new_tokens }
		
		dataStore.setRateData(tokens_key, new_tokens);
		dataStore.setRateData(timestamp_key, now);
		System.out.println(String.format("allowed_num:%s, new_tokens:%s ",allowed_num, new_tokens));
		return allowed;
	}
	
	/**
	 *  限流工具全局存储, 可基于数据库或Redis实现
	 * @author LIUWEI122
	 *
	 */
	public static interface RateDataStore{
		public Double getRateData(String key);
		public void setRateData(String key,Double value);
		public void setRateData(String key,double ttl,Double value);
	}
	
	public static class RateLimitterLocalStore implements RateDataStore{
		private static ConcurrentHashMap<String,Double> store = new ConcurrentHashMap<>();
		@Override
		public Double getRateData(String key) {
			return store.get(key);
		}

		@Override
		public void setRateData(String key, Double value) {
			store.put(key,value);
		}

		@Override
		public void setRateData(String key, double ttl, Double value) {
			store.put(key,value);
		}
	}

}
