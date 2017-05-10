package com.zlikun.storage;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.zlikun.storage.dao.UserInfoDao;
import com.zlikun.storage.model.Gender;
import com.zlikun.storage.model.UserInfo;


/**
 * 测试SpringCache集成
 * @author	zhanglikun
 * @date	2015年9月16日 上午11:29:56
 */
public class UserInfoDaoTest extends TestBase {

	@Resource
	private UserInfoDao userInfoDao ;
	
	/**
	 * 测试发现序列化上只能用JDK序列化，使用已提供的其它序列化需要指定类型，无法以配置的方式工作，可能需要扩展自定义序列化机制
	 * @throws InterruptedException
	 */
	@Test
	public void test() throws InterruptedException {
		
		System.out.println(String.format("查询第%d次!", 1));
		// 第一次查询，走数据库查询，写入缓存
		UserInfo uInfo = userInfoDao.get(1L) ;
		Assert.assertNotNull(uInfo);
		Assert.assertEquals(Gender.MALE, uInfo.getGender());

		System.out.println(String.format("查询第%d次!", 2));
		// 第二次查询，走缓存，缓存设置为1秒有效
		uInfo = userInfoDao.get(1L) ;
		Assert.assertNotNull(uInfo);

		// 删除用户后，会清除缓存
		userInfoDao.delete(1L);

		System.out.println(String.format("查询第%d次!", 3));
		// 清除缓存后，再次执行查询，又会走数据库，并将数据放入缓存
		uInfo = userInfoDao.get(1L) ;
		Assert.assertNotNull(uInfo);
		
		Thread.sleep(1000);	// 程序休眠1秒，使用缓存过期
		
		System.out.println(String.format("查询第%d次!", 4));
		uInfo = userInfoDao.get(1L) ;
		Assert.assertNotNull(uInfo);
		
	}
	
	@Test
	public void testGet() {
		// 第一次查询，走数据库查询，写入缓存
		UserInfo uInfo = userInfoDao.get(1L) ;
		Assert.assertNotNull(uInfo);
		Assert.assertEquals(Gender.MALE, uInfo.getGender());
		// 第二次查询，走缓存
		uInfo = userInfoDao.get(1L) ;
		Assert.assertNotNull(uInfo);
		Assert.assertEquals(Gender.MALE, uInfo.getGender());
	}
	
	@Test
	public void testList() {
		// 测试发现，内联调用未能使用缓存，原因是相同实例内部方法调用this指针会指向实际方法，而非缓存代理增强的方法导致
		List<UserInfo> list = userInfoDao.list(1L ,2L ,1L) ;
		Assert.assertNotNull(list);
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(Long.valueOf(2L), list.get(1).getUserId());
	}
	

}
