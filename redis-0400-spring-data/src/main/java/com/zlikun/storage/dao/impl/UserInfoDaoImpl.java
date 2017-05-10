package com.zlikun.storage.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.zlikun.storage.dao.UserInfoDao;
import com.zlikun.storage.model.Gender;
import com.zlikun.storage.model.UserInfo;

/**
 * 模拟实现数据查询接口
 * @author	zhanglikun
 * @date	2015年9月16日 下午2:18:16
 */
@Repository("userInfoDao")
public class UserInfoDaoImpl implements UserInfoDao {

	@Override
	public UserInfo get(Long userId) {
		System.out.println("数据库查询：" + userId);
		return new UserInfo().setUserId(userId)
				.setName("jackson")
				.setGender(Gender.MALE)
				.setEmail("jackson@sample.cn");
	}

	@Override
	public void delete(Long userId) {
		System.out.println("删除用户：" + userId);
	}

	@Override
	public List<UserInfo> list(Long ... userIds) {
		if(userIds == null || userIds.length == 0) return null ;
		List<UserInfo> list = new ArrayList<UserInfo>() ;
		for(Long userId : userIds) {
			list.add(get(userId)) ;		// 这里的get方法并不会读取缓存
		}
		return list ;
	}

}
