package com.zlikun.storage.pool;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 测试JedisPool关于带有test前缀配置
 * @author	zlikun
 * @date	2016年9月12日 下午5:12:39
 */
public class JedisPoolTest02 {

	private static final Logger log = LoggerFactory.getLogger(JedisPoolTest02.class) ;
	private static final Random random = new Random() ;
	
	// Redis服务器IP
	private static final String HOST = "redis.zlikun.net";
	// Redis的端口号
	private static int PORT = 6379;
	
	private JedisPool pool ;
	
	@Before
	public void init() {

		// 连接池配置(commons-pool2)
		GenericObjectPoolConfig config = new JedisPoolConfig();
		
		// 最大连接数，默认：8
		config.setMaxTotal(10);
		// 最大空闲连接数，默认：8
		config.setMaxIdle(8);
		// 最小空闲连接数，默认：0
		config.setMinIdle(0);
		
		// 最大连接等待时间，超时将抛出异常，单位：毫秒，默认-1，表示永不超时
		config.setMaxWaitMillis(1000);
		
		// 失效检查运行时间间隔，默认：-1，表示不开启
		config.setTimeBetweenEvictionRunsMillis(100);
		// 连接闲置时间最小值设置，如果闲置时间大于此值，将被销毁，否则将被激活
		config.setMinEvictableIdleTimeMillis(100L);
		// 申请连接时，检查连接是否可用
		config.setTestWhileIdle(true);
		// 出于性能考虑，下面三项生产环境是不开启的，对于失效连接，通常由应用程序的异常处理来控制
		config.setTestOnBorrow(true);
		config.setTestOnCreate(true);
		config.setTestOnReturn(true);
		
		// 密码为空
		pool = new JedisPool(config, HOST ,PORT ,1000 ,null) ;
	}
	
	@Test
	public void test() throws InterruptedException {
		
		Jedis jedis = pool.getResource() ;
		jedis.set("name", "zlikun") ;
		String name = jedis.get("name") ;
		Assert.assertNotNull(name);
		
		// 守护线程，用于监控连接池状态
		Thread monitor = new Thread("t-daemon-monitor") {
			public void run() {
				while(true) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	
					log.info("--------------------------------------------------<");
					log.info("MaxBorrowWaitTimeMillis : {} / MeanBorrowWaitTimeMillis : {}"
							,pool.getMaxBorrowWaitTimeMillis()
							,pool.getMeanBorrowWaitTimeMillis());
					log.info("NumActive : {} / NumIdle : {} / NumWaiters : {} / IsClosed : {}"
							,pool.getNumActive()
							,pool.getNumIdle()
							,pool.getNumWaiters()
							,pool.isClosed());
					log.info(">--------------------------------------------------");
				}
			};
		} ;
		monitor.setDaemon(true);
		monitor.start();
		
		ExecutorService exec = Executors.newFixedThreadPool(10) ;
		for(int i = 0 ;i < 100 ;i ++) {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					Jedis jedis = null;
					try {
						jedis = pool.getResource() ;
						handle(jedis) ;
					} finally {
						if(jedis != null) jedis.close();
					}
				}
			}) ;
		}
		
		exec.shutdown();
		while(!exec.isTerminated()) ;
		
		if(pool != null && !pool.isClosed()) {
			log.info("连接池关闭 ..");
			pool.close();
		}
		
		Thread.sleep(500);
		
		log.info("测试完成 ..");
		
	}
	
	/**
	 * 使用Jedis操作Redis
	 * @param jedis
	 */
	private void handle(final Jedis jedis) {
		try {
			Thread.sleep(random.nextInt(1000) + 100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 使用循环100次递增，模拟较耗时操作
		for(int i = 0 ;i < 100 ;i ++) {
			jedis.incr("xxx") ;
		}
	}
	
	@After
	public void destroy() {
		if(pool != null && !pool.isClosed()) {
			log.info("关闭连接池 !!");
			pool.close();
		}
	}

}