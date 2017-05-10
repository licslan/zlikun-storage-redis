package com.zlikun.storage.helper;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

/**
 * 仅作测试用
 * @author	zhanglikun
 * @date	2015年9月28日 下午5:24:13
 */
@Component("customKeyGenerator")
public class CustomKeyGenerator implements KeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		String key = params[0].toString();
		System.out.println("Key = " + key);
		return key ;
	}

}
