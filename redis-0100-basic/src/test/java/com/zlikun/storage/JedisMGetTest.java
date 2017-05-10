package com.zlikun.storage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 一次取多个值
 * @author	zhanglikun
 * @date	2015年9月21日 下午7:54:54
 */
public class JedisMGetTest {

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

		// 清除用户1 ~ 9缓存
		jedis.del("user:1" ,"user:2" ,"user:3" ,"user:4" ,"user:5" ,"user:6" ,"user:7" ,"user:8" ,"user:9") ;
		// 添加1、3、9三个元素到缓存
		jedis.mset("user:1" ,"Jackson" ,"user:3" ,"Tom" ,"user:9" ,"Suse") ;
		
		// 测试MGET命令结果
		List<String> list = jedis.mget("user:1" ,"user:2" ,"user:3" ,"user:4" ,"user:5" ,"user:6" ,"user:7" ,"user:8" ,"user:9") ;
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 9);	// 测试发现，查询的Key在缓存中不存在，返回NULL，所以元素个数仍为9个，未命中的以NULL值填充
	
	}
	
	/**
	 * Codis HMGET/HGETALL 性能测试(单位：毫秒)
	 * 		2858 / 2660 / 3247		-- 2000
	 * 		2950 / 2174	/ 3206		-- 2000
	 * 		7266 / 6407	/ 7721		-- 5000
	 * 
	 * Redis HMGET/HGETALL 性能测试(单位：毫秒)
	 * 		4549 / 5195 / 5138		-- 5000
	 * 		4828 / 4908 / 5138 		-- 5000
	 * 		4339 / 5713 / 5707		-- 5000
	 */
	@Test
	public void testHmgetPerformance() {
//		Jedis jedis = new Jedis("192.168.9.170", 19000) ;	// Codis
		Jedis jedis = new Jedis("192.168.9.205", 6379) ;	// Redis

		int loop = 5000 ;
		String key = "myuni:school:100" ;
		
		long time = System.currentTimeMillis() ;
		for(int i = 0 ; i < loop ; i ++) {
			jedis.hmget(key, "schoolId" ,"code" ,"name" ,"sld" ,"img") ;
		}
		System.out.println(String.format("程序耗时:%d 毫秒!", System.currentTimeMillis() - time));

		time = System.currentTimeMillis() ;
		for(int i = 0 ; i < loop ; i ++) {
			Pipeline pipe = jedis.pipelined() ;
			pipe.hget(key, "schoolId") ;
			pipe.hget(key, "code") ;
			pipe.hget(key, "name") ;
			pipe.hget(key, "sld") ;
			pipe.hget(key, "img") ;
			pipe.sync();
		}
		System.out.println(String.format("程序耗时:%d 毫秒!", System.currentTimeMillis() - time));
		
		time = System.currentTimeMillis() ;
		for(int i = 0 ; i < loop ; i ++) {
			jedis.hgetAll(key) ;
		}
		System.out.println(String.format("程序耗时:%d 毫秒!", System.currentTimeMillis() - time));
		
		if(jedis != null) jedis.close();
	}
	
}
