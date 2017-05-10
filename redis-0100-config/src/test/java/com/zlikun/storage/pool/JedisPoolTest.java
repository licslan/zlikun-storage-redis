package com.zlikun.storage.pool;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.pool2.PooledObjectFactory;
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
 * Jedis连接池配置，连接池基于commons-pool2实现
 * @author	zlikun
 * @date	2016年9月9日 下午5:22:58
 */
public class JedisPoolTest {

	private static final Logger log = LoggerFactory.getLogger(JedisPoolTest.class) ;
	
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
		config.setMaxIdle(10);
		// 最小空闲连接数，默认：0
		config.setMinIdle(3);
		
		// 最大连接等待时间，超时将抛出异常，单位：毫秒，默认-1，表示永不超时
		config.setMaxWaitMillis(1000);
		
//		config.setTimeBetweenEvictionRunsMillis(1000);
//		config.setMinEvictableIdleTimeMillis(0L);
//
//		config.setTestWhileIdle(true);
//		config.setTestOnBorrow(true);
//		config.setTestOnCreate(true);
//		config.setTestOnReturn(true);
//		
//		config.setBlockWhenExhausted(false);
//		config.setEvictionPolicyClassName("");
//		config.setFairness(false);
//		config.setLifo(false);
//		config.setNumTestsPerEvictionRun(0);
//		config.setSoftMinEvictableIdleTimeMillis(0L);
		
		initPool(config) ;
	}
	
	@Test
	public void test() {
		
		Jedis jedis = pool.getResource() ;
		List<String> list = jedis.blpop(1 ,"list") ;
		Assert.assertTrue(list.isEmpty());

		jedis.set("name", "zlikun") ;
		String name = jedis.get("name") ;
		Assert.assertNotNull(name);
		
		log.info("MaxBorrowWaitTimeMillis : {}" ,pool.getMaxBorrowWaitTimeMillis());
		log.info("MeanBorrowWaitTimeMillis : {}" ,pool.getMeanBorrowWaitTimeMillis());
		log.info("NumActive : {}" ,pool.getNumActive());
		log.info("NumIdle : {}" ,pool.getNumIdle());
		log.info("NumWaiters : {}" ,pool.getNumWaiters());
		
	}
	
	@After
	public void destroy() {
		if(pool != null) pool.close();
	}
	
	/**
	 * 使用构造方法配置连接池
	 * @param config 
	 */
	void initPool(GenericObjectPoolConfig config) {
		
		// 构造jedis连接池(#redis.clients.util.Pool、#GenericObjectPool)
		int connectionTimeout = 1000 ;	// 连接超时时间，单位：毫秒
		int soTimeout = 1000 ;			// 暂时未知
//		String password = "123456" ;	// redis密码，没有密码时使用null
		String password = null ;		// redis密码，没有密码时使用null
		int database = 1 ;				// 连接到指定库
		String clientName = "zlikun" ;	// 
		boolean ssl = false ;			// 
		SSLSocketFactory sslSocketFactory = null ;
	    SSLParameters sslParameters = null ;
	    HostnameVerifier hostnameVerifier = null ;
	    // 使用最复杂的一个构造方法来创建连接池
		pool = new JedisPool(config, HOST ,PORT ,connectionTimeout
				,soTimeout ,password ,database ,clientName
				,ssl ,sslSocketFactory ,sslParameters ,hostnameVerifier) ;
		
		Assert.assertNotNull(pool);
		
	}
	
	/**
	 * 使用PooledObjectFactory配置连接池
	 * @param config 
	 */
	void initPool2(GenericObjectPoolConfig config) {

		// 需要自行扩展实现
		PooledObjectFactory<Jedis> factory = null ; 
		
		pool = new JedisPool() ;
		pool.initPool(config, factory);
		
		Assert.assertNotNull(pool);
	}
	
	
}