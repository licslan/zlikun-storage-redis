package org.springframework.data.redis.core.sharded;

import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.ShardedJedis;

/**
 * 基于spring-data-redis和jedis进行一致哈希封装，操作API与RedisTemplate相同，仅提供一致哈希实现
 * @author	zhanglikun
 * @date	2015年9月18日 下午5:54:32
 * @param <K>
 * @param <V>
 * @see RedisTemplate
 * @see ShardedJedis
 */
public class ShardedRedisTemplate<K, V> extends RedisTemplate<K, V> {

	// TODO
	
}
