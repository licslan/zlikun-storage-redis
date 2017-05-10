package com.zlikun.storage.blog.helper;

import java.util.Set;

/**
 * 
 * @author	zhanglikun
 * @date	2015年10月8日
 */
public class ArrayHelper {

	/**
	 * 集合转换为数组
	 * @param sets
	 * @return
	 */
	public static final String [] toArray(Set<String> sets) {
		if(sets == null || sets.isEmpty()) return null ;
		String [] ts = new String [sets.size()] ;
		int index = 0 ;
		for(String t : sets) {
			ts[index ++] = t ;
		}
		return ts ;
	}
	
}
