package com.zlikun.storage.dao;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.zlikun.storage.model.UserInfo;

public interface UserInfoDao {

	// 生成Key格式：my.sample.redis.model.UserInfo.1，条件是userId不能为空
	@Cacheable(value = "cache_30_sec" ,key = "'user:' + #userId" ,condition = "#userId != null")
//	@Cacheable(value = "cache_30_sec")
	UserInfo get(Long userId) ;
	
	@CacheEvict(value = "cache_30_sec" ,key = "'user:' + #userId" ,condition = "#userId != null")
//	@Cacheable(value = "cache_30_sec")
	void delete(Long userId) ;

	/**
	 * 列表查询，返回用户列表
	 * @param userIds
	 * @return
	 */
	List<UserInfo> list(Long ... userIds) ;
	
}
