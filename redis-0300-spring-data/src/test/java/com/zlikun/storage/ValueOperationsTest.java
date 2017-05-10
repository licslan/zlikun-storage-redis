package com.zlikun.storage;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.zlikun.storage.data.Gender;
import com.zlikun.storage.data.UserInfo;

/**
 * 普通KV缓存
 * @author	zhanglikun
 * @date	2015年9月16日 上午11:29:56
 */
public class ValueOperationsTest extends TestBase {

	/**
	 * 可以直接使用redisTemplate注入
	 */
	@Resource(name = "redisTemplate")
	private ValueOperations<String ,UserInfo> vo ;
	
	@Resource
	private RedisTemplate<String ,UserInfo> redisTemplate ;
	
	@Before
	public void init() {
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<UserInfo>(UserInfo.class));
	}
	
	@Test
	public void test() throws InterruptedException {
		
		UserInfo user = new UserInfo() ;
		user.setName("jackson");
		user.setGender(Gender.MALE);
		
		redisTemplate.delete("user");
		vo.set("user", user);
		UserInfo info = vo.get("user") ;
		
		Assert.assertNotNull(info);
		Assert.assertEquals(Gender.MALE, info.getGender());
		
		// 另外一种加入缓存方式(可以理解为命名缓存)，可以设置缓存时间
		redisTemplate.delete("user");
		BoundValueOperations<String ,UserInfo> bvo = redisTemplate.boundValueOps("user") ;
		bvo.set(user, 200, TimeUnit.MILLISECONDS) ;
		Assert.assertNotNull(bvo.get());

		Thread.sleep(200);
		
		// 200毫秒后，缓存失效
		Assert.assertNull(bvo.get());
		
	}
	
}
