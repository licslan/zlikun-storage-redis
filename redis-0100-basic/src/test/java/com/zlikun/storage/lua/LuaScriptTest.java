package com.zlikun.storage.lua;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.zlikun.storage.LuaScriptManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Jedis执行Lua脚本
 * @author	zhanglikun
 * @date	2015年11月11日 下午1:39:23
 */
public class LuaScriptTest {

	private Jedis jedis ;
	
	/**
	 * 实现访问频率控制，如果某个IP在短时间内频繁访问页面，检测并记录之
	 */
	@Test
	public void testRateLimiting() {
		// 如下代码体现实现思路，参考《Redis入门指南》
		// 针对访问IP设置一个缓存：rate:limiting:$ip
		String ip = "192.168.1.100" ;	// 假定访问IP
		// 先清空缓存，防止干扰测试
		jedis.del("rate:limiting:" + ip) ;
		// 设定1分钟内最多可访问10次
		final int seconds = 60 ;
		final int times = 10 ;
		// 快速循环访问12次，模拟12个并发情况
		for(int i = 0 ; i <  120 ; i ++) {
			if(rateLimiting(ip, seconds, times)) {
				// 模拟正常访问逻辑
				System.out.println(String.format("可以正常访问![%d次]", i + 1));
			} else {
				// 模拟受限访问逻辑
				System.out.println("访问频率过快(" + seconds + "秒访问超过" + times + "次)!");
			}
		}
	}
	
	/**
	 * 使用Lua脚本来实现上述机制
	 * @throws IOException 
	 */
	@Test
	public void testRateLimitingByLuaScript() throws IOException {
		// --eval参数是告知redis-cli后面将执行lua脚本
		// /usr/local/likun/redis-scripts/ratelimiting.lua 是脚本路径
		// rate:limiting:127.0.0.1	是缓存Key
		// 10 3	是脚本参数，此脚本中表示10秒、3次(注意前面的,前后各有一个空格)，即：10秒内小于3次返回1，否则返回0
		// Lua脚本内容参考：src/test/resources/scripts/ratelimiting.lua
		// redis-cli --eval /usr/local/likun/redis-scripts/ratelimiting.lua rate:limiting:127.0.0.1 , 10 3

		// 读取脚本内容
		InputStream in = LuaScriptTest.class.getClassLoader().getResourceAsStream("scripts/ratelimiting.lua") ;
		BufferedReader br = new BufferedReader(new InputStreamReader(in)) ;
		StringBuffer sb = new StringBuffer() ;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			sb.append(line + "\r\n");	// 拼接脚本时，行尾插入\r\n，否则脚本缩成一行时，容易出错
		}
		br.close();
		
		// 执行脚本
		String luaScript = sb.toString() ;
		System.out.println(luaScript);
		/*
		 *  luaScript 									脚本内容
		 *  Arrays.asList("rate:limiting:127.0.0.1")	Key列表
		 *  Arrays.asList("60" ,"10")					参数列表
		 */
		Object result = jedis.eval(luaScript, Arrays.asList("rate:limiting:127.0.0.1"), Arrays.asList("60" ,"10")) ;
		Assert.assertEquals(Long.valueOf(1), result);
	}
	
	@Test
	public void testRateLimitingByLuaScriptEvalsha() throws IOException {
		Object result = jedis.evalsha(LuaScriptManager.getInstance().getEvalsha("ratelimiting.lua")
				, Arrays.asList("rate:limiting:127.0.0.1"), Arrays.asList("60" ,"10")) ;
		Assert.assertEquals(Long.valueOf(1), result);
	}
	
	/**
	 * 判断是否可以访问方法
	 * @param ip		访问IP
	 * @param seconds	计数时长(单位：秒)
	 * @param times		计数时长内最大访问次数
	 * @return
	 */
	private boolean rateLimiting(final String ip ,final int seconds ,final int times) {
		// 每访问一次，就自增一次
		String key = "rate:limiting:" + ip ;
		if(jedis.exists(key)) {
			// 如果Key存在，则自增
			long _times = jedis.incr(key) ;
			// 判断最新访问次数是否大于受限次数
			if(_times > times) {
				return false ;
			}
		} else {
			// 保证操作是原子的，使用Redis的事务机制
			Transaction t = jedis.multi() ;
			// 如果不存在，创建之(也是自增)
			t.incr(key) ;
			// 设置缓存时间60秒，即一分钟(一分钟后将重新计数)
			t.expire(key, seconds) ;
			// 执行，可以理解为提交事务(Redis没有回滚事务机制)
			t.exec() ;
		}
		return true ;
	}
	
	
	@Before
	public void init() {
		// 连接Redis服务器
		jedis = new Jedis("redis-1.i.v-log.cn");
		// 进行认证(密码)
		jedis.auth("ablejava") ;
		jedis.select(1) ;
	}
	
	@After
	public void destroy() {
		jedis.close();
	}
	
}
