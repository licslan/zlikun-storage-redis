package com.zlikun.storage;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

public class HashRedisTest extends TestBase {

	@Resource
	private RedisTemplate<String ,String> redisTemplate ;
	
	@Before
	public void init() {
//		redisTemplate.setKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
//		redisTemplate.setHashKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
	}
	
	@Test
	public void testHash() {
		
		// 哈希缓存
		BoundHashOperations<String ,String, String> bhoHandler = redisTemplate.boundHashOps("user:" + 10001 + ":manager") ;
		bhoHandler.expireAt(new Date()) ;
		// 加入缓存
		bhoHandler.put("myuni", "1");
		Assert.assertEquals("1", bhoHandler.get("myuni"));
		
	}
	
}
