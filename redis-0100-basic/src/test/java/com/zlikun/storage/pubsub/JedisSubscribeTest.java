package com.zlikun.storage.pubsub;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Jedis发布/订阅示例
 * @author	zhanglikun
 * @date	2015年9月16日 下午9:12:38
 */
public class JedisSubscribeTest {

	private Jedis jedis ;
	
	// 需要自行实现相关代码以完成业务
	private JedisPubSub jps = new JedisPubSub() {
		/**
		 * 消息处理方法
		 */
		@Override
		public void onMessage(String channel, String message) {
			System.out.println(String.format("onMessage : channel = %s / message = %s", channel ,message));
		}
		/**
		 * 订阅端启动时执行，subscribedChannels参数是通道序列，从1开始计
		 */
		@Override
		public void onSubscribe(String channel, int subscribedChannels) {
			System.out.println(String.format("onSubscribe : channel = %s / subscribedChannels = %s", channel ,subscribedChannels));
		}
		/**
		 * 退订处理方法
		 */
		@Override
		public void onUnsubscribe(String channel, int subscribedChannels) {
			System.out.println(String.format("onUnsubscribe : channel = %s / subscribedChannels = %d", channel ,subscribedChannels));
		}
		/**
		 * 使用同onMessage，用于模式订阅
		 */
		@Override
		public void onPMessage(String pattern, String channel, String message) {
			System.out.println(String.format("onPMessage : pattern = %s / channel = %s / message = %s", pattern ,channel ,message));
		}
		/**
		 * 使用同onSubscribe，用于模式订阅，订阅端启动时执行
		 */
		@Override
		public void onPSubscribe(String pattern, int subscribedChannels) {
			System.out.println(String.format("onPSubscribe : pattern = %s / subscribedChannels = %d", pattern ,subscribedChannels));
		}
		/**
		 * 使用同onUnsubscribe，用于模式订阅
		 */
		@Override
		public void onPUnsubscribe(String pattern, int subscribedChannels) {
			System.out.println(String.format("onPUnsubscribe : pattern = %s / subscribedChannels = %d", pattern ,subscribedChannels));
		}
	};
	
	@Before
	public void init() {
		jedis = new Jedis("redis-1.i.v-log.cn");
		jedis.auth("ablejava") ;
		jedis.select(1) ;	// 防止与其它数据混合(redis-cli查询时方便一些)
	}
	
	@After
	public void destroy() {
		jedis.close();
	}
	
	/**
	 * 订阅消息时，订阅程序会阻塞住，直接到接收到消息并响应消息(程序不会退出)
	 */
	@Test
	public void subscribe() {
		
		// 订阅消息(可以同时订阅多个channel)
		// 同一个频道订阅两次会怎样？测试结论是同一个客户端同一频道只能订阅一次，多次订阅只算作一次
		jedis.subscribe(this.jps, "channel_goods" ,"channel_specials" ,"channel_cars" ,"channel_goods");
		
		System.out.println("如果此句未打印，说明上一句程序阻塞住了~");
		
	}
	
	@Test @Ignore
	public void psubscribe() {
		
		// 模式订阅
		jedis.psubscribe(this.jps, "channel_*");
		
	}
	
}
