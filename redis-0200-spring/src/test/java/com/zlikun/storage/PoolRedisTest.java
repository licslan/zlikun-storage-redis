package com.zlikun.storage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 普通连接池测试
 * @author	zhanglikun
 * @date	2015年9月16日 下午9:05:10
 */
public class PoolRedisTest {

	private JedisPool jedisPool ;
	
	@SuppressWarnings("resource")
	@Before
	public void init() {
		jedisPool = new ClassPathXmlApplicationContext("beans-redis-pool.xml").getBean("jedisPool" ,JedisPool.class) ;
	}
	
	@Test
	public void test() {
		Jedis jedis = jedisPool.getResource() ;
		jedis.set("user:name", "jackson") ;
//		jedis.expire("user:name", 30) ;	// 为指定Key设置缓存时间
		
		String name = jedis.get("user:name") ;
		Assert.assertEquals("jackson", name);
		
		jedis.close();
		
	}
	
}
