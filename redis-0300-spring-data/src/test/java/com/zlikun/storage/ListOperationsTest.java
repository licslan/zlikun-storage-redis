package com.zlikun.storage;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.zlikun.storage.data.Gender;
import com.zlikun.storage.data.UserInfo;

public class ListOperationsTest extends TestBase {

	/**
	 * 可以直接使用redisTemplate注入
	 */
	@Resource(name = "redisTemplate")
	private ListOperations<String ,UserInfo> lo ;
	
	@Resource
	private RedisTemplate<String ,UserInfo> redisTemplate ;
	
	@Before
	public void init() {
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<UserInfo>(UserInfo.class));
	}
	
	@Test
	public void test() {
		
		redisTemplate.delete("users");
		
		// 从队首加入队列
		Long count = lo.leftPushAll("users" 
				,new UserInfo().setName("Link").setBirthday(new Date()).setGender(Gender.MALE)
				,new UserInfo().setName("Tom").setBirthday(new Date()).setGender(Gender.MALE)
				,new UserInfo().setName("Suse").setBirthday(new Date()).setGender(Gender.FEMALE)
		) ;
		
		Assert.assertEquals(Long.valueOf(3), count);
		
		// 获取整个队列
		List<UserInfo> list = lo.range("users", 0, -1) ;
		
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(Gender.FEMALE, list.get(0).getGender());
		
		// 另一种使用方式
		BoundListOperations<String ,UserInfo> blo = redisTemplate.boundListOps("users") ;
		Assert.assertNotNull(blo.range(0, 1).get(0));	// 此时从队首取出一个元素，应为非空
		blo.expireAt(new Date()) ;						// 设置此刻缓存过期
		UserInfo user = blo.rightPop() ;				// 如果没有过期，从队尾弹出一个，即：Link
		Assert.assertNull(user);
//		Assert.assertEquals("Link", user.getName());	// 如果未过期，元素是name为Link的元素
		
	}
	
}
