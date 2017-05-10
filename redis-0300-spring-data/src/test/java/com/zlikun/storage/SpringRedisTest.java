package com.zlikun.storage;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

public class SpringRedisTest extends TestBase {

	@Resource
	private RedisTemplate<String ,String> redisTemplate ;
	
	@Before
	public void init() {
		// Jackson序列化需要传入类型参数，所以并不太适合放在配置文件里面
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<String>(String.class));
	}
	
	@Test
	public void test() {
		
		// 普通KV缓存
		redisTemplate.delete("user:name");	// 先清缓存
		ValueOperations<String ,String> vo = redisTemplate.opsForValue() ;
		vo.set("user:name", "jackson");
		Assert.assertEquals("jackson", vo.get("user:name"));
		
		// 列表缓存
		redisTemplate.delete("list");		// 先清缓存
		ListOperations<String ,String> lo = redisTemplate.opsForList() ;
		lo.leftPush("list", "AAA") ;
		lo.rightPush("list", "BBB") ;
		lo.leftPushAll("list", "CCC" ,"DDD") ;
		// DDD CCC AAA BBB
		List<String> list = lo.range("list", 0, -1) ;
		Assert.assertEquals("CCC", list.get(1));
		
		// 集合缓存
		redisTemplate.delete("countries");	// 先清缓存
		SetOperations<String, String> so = redisTemplate.opsForSet() ;
		Long count = so.add("countries", "中国" ,"美国" ,"德国") ;
		Assert.assertEquals(Long.valueOf(3L), count);
		Set<String> countries = so.members("countries") ;
		Assert.assertEquals(Long.valueOf(countries.size()), so.size("countries"));
		
		// 有序集合缓存
		redisTemplate.delete("members");	// 先清缓存
		ZSetOperations<String, String> zso = redisTemplate.opsForZSet() ;
		zso.add("members", "jack", 2D) ;
		zso.add("members", "link", 1D) ;
		zso.add("members", "suse", 2.8D) ;
		Set<String> members = zso.range("members", 0, -1) ;
		for(String member : members) System.out.print(String.format("%s - ", member));
		
		// 哈希缓存
		redisTemplate.delete("user");		// 先清缓存
		HashOperations<String, String, String> ho = redisTemplate.opsForHash() ;
		ho.put("user", "name", "jackson");
		ho.put("user", "gender", "male");
		ho.put("user", "age", "18");
		Assert.assertEquals("male", ho.get("user", "gender"));
		
	}
	
}
