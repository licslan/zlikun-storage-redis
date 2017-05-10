package com.zlikun.storage.blog.helper;

/**
 * 缓存辅助工具类
 * @author	zhanglikun
 * @date	2015年10月8日
 */
public class CacheHelper {

	private static final String CACHE_KEY_SEPARATOR = ":" ;
	
	/**
	 * 组合缓存Key
	 * @param parts
	 * @return
	 */
	public static final String cacheKey(String ... parts) {
		if(parts == null || parts.length == 0) return null ;
		StringBuffer sb = new StringBuffer() ;
		for(String part : parts) {
			if(part == null) throw new IllegalArgumentException("参数不能为空!") ;
			sb.append(part + CACHE_KEY_SEPARATOR) ;
		}
		return sb.substring(0, sb.length() - 1) ;
	}
	
	public static void main(String[] args) {
		System.out.println(cacheKey("AA" ,"BB" ,"CC"));
		System.out.println(cacheKey("null"));
//		System.out.println(cacheKey(null));
//		System.out.println(cacheKey(null ,null));
	}
	
}
