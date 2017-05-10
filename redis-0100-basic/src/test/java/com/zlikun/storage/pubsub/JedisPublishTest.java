package com.zlikun.storage.pubsub;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

/**
 * Jedis发布/订阅示例
 * @author	zhanglikun
 * @date	2015年9月16日 下午9:12:38
 */
public class JedisPublishTest {

	private Jedis jedis ;
	
	@Before
	public void init() {
		jedis = new Jedis("redis-1.i.v-log.cn");
		jedis.auth("ablejava") ;
		jedis.select(1) ;	// 防止与其它数据混合(redis-,cli查询时方便一些)
	}
	
	@After
	public void destroy() {
		jedis.close();
	}
	
	/**
	 * 同时发布两个消息(不同通道)，观察订阅者处理方式
	 */
	@Test
	public void publish() {
		
		// 发布消息
		Long subscribeNumber = jedis.publish("channel_goods", "新到货一批手机!") ;
		System.out.println("消息订阅数：" + subscribeNumber);
		
		// 发布消息
		subscribeNumber = jedis.publish("channel_specials", "XX发布了第一张专辑!") ;
		System.out.println("消息订阅数：" + subscribeNumber);
		
		// 相同通道再发布一条消息会怎样？
		subscribeNumber = jedis.publish("channel_specials", "XX发布了第二张专辑!") ;
		
		System.out.println(String.format("pubsubNumPat = %d", jedis.pubsubNumPat()));
		
		// 模式查询活跃频道列表(即有订阅的客户端监听的)
		System.out.println(jedis.pubsubChannels("channel_*"));
		
		// 查询通道订阅数
		System.out.println(jedis.pubsubNumSub("channel_goods" ,"channel_specials"));
	}
	
}
