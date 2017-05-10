package com.zlikun.storage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 分片连接池测试
 * @author	zhanglikun
 * @date	2015年9月16日 下午9:04:54
 */
public class SharededPoolRedisTest {

	private ShardedJedisPool shardedJedisPool ;
	
	@SuppressWarnings("resource")
	@Before
	public void init() {
		shardedJedisPool = new ClassPathXmlApplicationContext("beans-redis-pool-shareded.xml").getBean("shardedJedisPool" ,ShardedJedisPool.class) ;
	}
	
	@Test
	public void test() {
		ShardedJedis jedis = shardedJedisPool.getResource() ;
		jedis.set("user:name", "jackson") ;
//		jedis.expire("user:name", 30) ;	// 为指定Key设置缓存时间
		
		String name = jedis.get("user:name") ;
		Assert.assertEquals("jackson", name);
		
		jedis.close();
		
	}
	
}
