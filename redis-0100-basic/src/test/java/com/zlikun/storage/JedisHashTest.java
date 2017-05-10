package com.zlikun.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author	zhanglikun
 * @date	2015年9月23日 下午4:41:06
 */
public class JedisHashTest {

	private Jedis jedis ;
	
	@Before
	public void init() {
		jedis = new Jedis("redis-1.i.v-log.cn");
	}
	
	@After
	public void destroy() {
		jedis.close();
	}
	
	@Test
	public void test() {

		System.out.println(jedis.hget("user:159225653:manager", "myuni"));
		
	}
	
}
