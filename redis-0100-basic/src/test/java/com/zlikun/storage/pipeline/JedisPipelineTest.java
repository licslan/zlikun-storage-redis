package com.zlikun.storage.pipeline;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * Redis管道命令测试
 * @author	zhanglikun
 * @date	2015年11月9日 上午11:27:54
 */
public class JedisPipelineTest {

	private Jedis jedis ;
	private long time ;
	
	@Before
	public void init() {
		// 连接Redis服务器
		jedis = new Jedis("redis.i.zlikun.com");
		jedis.select(1) ;
		time = System.currentTimeMillis() ;
	}
	
	@After
	public void destroy() {
		System.out.println(String.format("程序执行[%d]毫秒!"
				, System.currentTimeMillis() - time));
		jedis.close();
	}

	/**
	 * 基本管道测试用例
	 */
	@Test
	public void testPipelined() {

		jedis.del("counter") ;
		
		// 使用管道命令可以一次执行多条Redis命令而只发生一次网络传输
		// ，从而提升执行效率
		Pipeline pip = jedis.pipelined() ;
		
		// 如下执行四次请求，如不使用管道，将产生四次网络开销
		// ，如果使用管道，则只有一次
		pip.incr("counter") ;
		pip.incr("counter") ;
		pip.incr("counter") ;
		pip.incr("counter") ;
		
		// 注意在使用管道过程中，不能进行非管道操作(直到管道关闭)
		// redis.clients.jedis.exceptions.JedisDataException
		// Assert.assertEquals("4", jedis.get("counter"));
	
		// Redis处理完所有命令后，将处理结果一次返回
		List<Object> list = pip.syncAndReturnAll() ;
		
		Assert.assertEquals(Long.valueOf(1), list.get(0));
		Assert.assertEquals(Long.valueOf(4), list.get(3));
	}
	
	/**
	 * 管道性能测试(这里不考虑管道命令上限问题)
	 * 测试耗时：47ms/39ms/47ms(采样其中三次输出结果)
	 */
	@Test
	public void testPipelinedPerformance() {
		Pipeline pip = jedis.pipelined() ;
		// 循环写入10000条数据
		for(int i = 0 ; i < 10000 ;i ++) {
			pip.set("pip:" + (i + 1)
					, String.valueOf(i + 1)) ;
		}
	}

	/**
	 * 非管道性能测试
	 * 测试耗时：737ms/745ms/733ms(采样其中三次输出结果)
	 */
	@Test
	public void testJedisPerformance() {
		// 循环写入10000条数据
		for(int i = 0 ; i < 10000 ;i ++) {
			jedis.set("pip:" + (i + 1)
					, String.valueOf(i + 1)) ;
		}
	}

}
