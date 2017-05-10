package com.zlikun.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

/**
 * jedis的一些简单用法
 * @author	zhanglikun
 * @date	2015年8月4日 下午3:45:32
 * @link	http://www.cnblogs.com/liuling/p/2014-4-19-04.html
 */
public class JedisUsageTest {

	private Jedis jedis ;
	
	@Before
	public void init() {
		jedis = new Jedis("redis.zlikun.net" ,6379);
	}
	
	@After
	public void destroy() {
		jedis.close();
	}
	
	@Test
	public void testString() {
		String usernameKey = "user:name" ;
		
		jedis.set(usernameKey, "Alice");
		Assert.assertEquals("Alice", jedis.get(usernameKey));
		
		// 追加字符串值
		jedis.append(usernameKey, " is a girl .") ;
		Assert.assertEquals("Alice is a girl .", jedis.get(usernameKey));
		
		// 删除键
		jedis.del(usernameKey) ;
		Assert.assertNull(jedis.get(usernameKey));
		
		// 设置多个键值对
		jedis.mset("name" ,"Alice" ,"age" ,"20" ,"mobile" ,"15618950101") ;
		jedis.incr("age") ;	// 自增
		Assert.assertEquals("Alice", jedis.get("name"));
		Assert.assertEquals("21", jedis.get("age"));
		
		// 设置NULL值(Jedis不允许值为NULL)
		String reply = jedis.set("foo", "null") ;
		Assert.assertEquals("OK", reply);
	}
	
	@Test
	public void testMap() {
		Map<String ,String> user = new HashMap<String, String>() ;
		user.put("name", "Alice") ;
		user.put("age", "20") ;
		user.put("mobile", "15618950101") ;
		
		String userKey = "user:alice" ;
		
		// 将Map写入缓存
		jedis.hmset(userKey, user) ;
		
		// 从缓存中获取数据
		List<String> value = jedis.hmget(userKey, "name" ,"age") ;
		Assert.assertEquals("Alice", value.get(0));
		Assert.assertEquals("20", value.get(1));
		
		// 删除某个Key
		jedis.hdel(userKey, "mobile") ;
		Assert.assertNull(jedis.hmget(userKey, "mobile").get(0));
		
		// 一些其它信息的获取
		Assert.assertEquals(Long.valueOf(2L), jedis.hlen(userKey));	// 获取Map键数量
		Assert.assertTrue(jedis.hexists(userKey, "age"));			// 判断某个Key是否存在
		Assert.assertEquals(2, jedis.hkeys(userKey).size());		// 取出所有Key，返回Set集合
		Assert.assertEquals(2, jedis.hvals(userKey).size());		// 取出所有Value，返回Set集合
		
	}
	
	@Test
	public void testList() {
		String cacheKey = "languages" ;

		// lpush从左向队列中添加
		jedis.lpush(cacheKey, "c++") ;
		jedis.lpush(cacheKey, "java" ,"javascript" ,"nodejs") ;
		// rpush从右向队列中添加
		jedis.rpush(cacheKey, "php") ;
		
		// -1表示获取全部数据
		List<String> list = jedis.lrange(cacheKey, 0L, -1L) ;
		Assert.assertEquals("nodejs", list.get(0)) ;
		Assert.assertEquals("java", list.get(2)) ;
		Assert.assertEquals("php", list.get(list.size() - 1)) ;
		
	}
	
	@Test
	public void testSet() {
		// 添加
		jedis.sadd("user", "liuling");
		jedis.sadd("user", "xinxin");
		jedis.sadd("user", "ling");
		jedis.sadd("user", "zhangxinxin");
		jedis.sadd("user", "who");
		// 移除noname
		jedis.srem("user", "who");
		System.out.println(jedis.smembers("user"));			// 获取所有加入的value
		System.out.println(jedis.sismember("user", "who"));	// 判断who是否是user集合的元素
		System.out.println(jedis.srandmember("user"));		// 随机获取
		System.out.println(jedis.scard("user"));			// 返回集合的元素个数
	}

	@Test
	public void testSort() {
		// 清空缓存并重新填充数据
		jedis.del("numbers") ;
		jedis.lpush("numbers", "12" ,"28" ,"9" ,"47" ,"13") ;
		// 排序
		System.out.println(jedis.sort("numbers"));
	}
	
}
