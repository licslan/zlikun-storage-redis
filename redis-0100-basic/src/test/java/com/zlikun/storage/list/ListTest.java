package com.zlikun.storage.list;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class ListTest {

	private Jedis jedis ;
	
	@Before
	public void init() {
		// 连接Redis服务器
		jedis = new Jedis("redis-1.i.v-log.cn");
		// 进行认证(密码)
		jedis.auth("ablejava") ;
		// 选择库
		jedis.select(1) ;
	}
	
	@After
	public void destroy() {
		jedis.close();
	}
	
	/**
	 * 作为栈使用，先进后出
	 */
	@Test
	public void testStack() {
		jedis.del("tst:queue") ;
		
		// 左边追加元素
		Long count = jedis.lpush("tst:queue", "A" ,"B" ,"C" ,"D" ,"E") ;
		Assert.assertEquals(Long.valueOf(5), count);
		
		Assert.assertEquals(Long.valueOf(5), jedis.llen("tst:queue"));

		List<String> list = jedis.lrange("tst:queue", 0, -1) ;
		Assert.assertNotNull(list);
		Assert.assertEquals("D", list.get(1));
	}

	/**
	 * 作为队列使用，先进先出
	 */
	@Test
	public void testQueue() {
		jedis.del("tst:queue") ;
		
		// 右边追加元素
		Long count = jedis.rpush("tst:queue", "A" ,"B" ,"C" ,"D" ,"E") ;
		Assert.assertEquals(Long.valueOf(5), count);
		
		Assert.assertEquals(Long.valueOf(5), jedis.llen("tst:queue"));
		
		List<String> list = jedis.lrange("tst:queue", 0, -1) ;
		Assert.assertNotNull(list);
		Assert.assertEquals("B", list.get(1));
		
		// 从左边弹出元素
		Assert.assertEquals("A", jedis.lpop("tst:queue"));
		Assert.assertEquals(Long.valueOf(4), jedis.llen("tst:queue"));
		Assert.assertEquals("E", jedis.rpop("tst:queue"));
		Assert.assertEquals(Long.valueOf(3), jedis.llen("tst:queue"));
	}
	
	/**
	 * 阻塞式弹出元素
	 */
	@Test
	public void testBrpop() {
		jedis.del("tst:queue") ;
		jedis.lpush("tst:queue", "A" ,"B" ,"C" ,"D" ,"E") ;
		
		// 阻塞式弹出元素(一次可以弹出多个队列，弹出顺序从左到右，即：左边有元素从左边弹出，没有才从右边一个弹出，类似优先级的概念，如果所有队列都没有元素，则阻塞住)
		// 弹出数据格式为：[key ,value] => [tst:queue, A]
		List<String> list = jedis.brpop(0, "tst:queue" ,"tst:queue2") ;
		Assert.assertEquals("tst:queue", list.get(0));
		Assert.assertEquals("A", list.get(1));
	}
	
	/**
	 * 多个客户端使用Brpop时，会发生什么？
	 * 结论，多个客户端同时(并发)从队列中获取数据时不会有问题，Redis是单线程的，所以即使并发条件下也是一个一个执行
	 * 并发使用会提升性能么？测试结论多个客户端同时工作会提升整体处理性能
	 * 提升点在哪里？(并发也是一个一个排队执行，网络开销、附加处理逻辑)
	 */
	@Test @Ignore
	public void testMultiBrpop() {

		jedis.del("tst:queue") ;
		
		// 使用管道一次写入5000条记录，用于后面读取操作
		Pipeline pip = jedis.pipelined() ;
		for(int i = 0 ; i < 5000 ; i ++) {
			pip.lpush("tst:queue", String.valueOf(i)) ;
		}
		pip.sync();
		
		// 1447213932922(开5个线程)
		// 1447214011765(开1个线程)
		System.out.println("开始读取时间：" + System.currentTimeMillis());
		
		brpopThread(1) ;	// 启动一个线程接收队列数据
//		brpopThread(2) ;	// 启动一个线程接收队列数据
//		brpopThread(3) ;	// 启动一个线程接收队列数据
//		brpopThread(4) ;	// 启动一个线程接收队列数据
//		brpopThread(5) ;	// 启动一个线程接收队列数据
		
		// 主线程休眠30秒，避免子线程未完成主线程已运行结束
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 指定线程编号
	 * @param number
	 */
	private void brpopThread(final int number) {
		new Thread() {
			private Jedis jedis0 = new Jedis("redis-1.i.v-log.cn");
			{
				jedis0.auth("ablejava") ;
				jedis0.select(1) ;
			}
			@Override
			public void run() {
				while(true) {
					List<String> list = jedis0.brpop(0, "tst:queue") ;
					// 打印全部读取结束时间
					// 1447213933717(开5个线程|795ms读完)
					// 1447214016592(开1个线程|4827ms读完)
					if("4999".equals(list.get(1))) System.out.println("全部读取结束时间：" + System.currentTimeMillis());
					System.out.println("Thread-" + number + " \t " + list.get(1));	// 打印取得的数据
				}
			}
		}.start();
	}
	
}
