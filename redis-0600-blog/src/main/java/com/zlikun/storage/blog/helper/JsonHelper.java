package com.zlikun.storage.blog.helper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON转换辅助类
 * @author	zhanglikun
 * @date	2015年10月8日
 */
public class JsonHelper {

	private static final ObjectMapper om = new ObjectMapper() ;

	/**
	 * 将对象转换为JSON字符串
	 * @param obj
	 * @return
	 */
	public static final String obj2json(Object obj) {
		if(obj == null) return null ;
		try {
			return om.writeValueAsString(obj) ;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null ;
		}
	}
	
	/**
	 * json字符串转换为对象
	 * @param json
	 * @param type
	 * @return
	 */
	public static final <T> T json2obj(String json ,Class<T> type) {
		if(json == null || type == null) return null ;
		try {
			return om.readValue(json, type) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null ;
	}
	
}
