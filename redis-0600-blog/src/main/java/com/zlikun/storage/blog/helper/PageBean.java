package com.zlikun.storage.blog.helper;

import java.io.Serializable;
import java.util.Collection;

/**
 * 分页辅助类
 * @author	zhanglikun
 * @date	2015年9月27日
 */
public class PageBean<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private int index ;	// 分页索引，从0开始计
	private int limit ;	// 分页长度
	private int count ;	// 总记录数
	private int pages ;	// 总页数
	
	private Collection<T> records ;		// 记录列表

	public PageBean() {}
	
	/**
	 * 构造方法
	 * @param limit	分页长度
	 */
	public PageBean(int limit) {
		this.limit = limit ;
	}
	
	/**
	 * 构造方法
	 * @param index	分页索引
	 * @param limit	分页长度
	 */
	public PageBean(int index ,int limit) {
		this.index = index ;
		this.limit = limit ;
	}
	
	/**
	 * 计算分页起始记录位置
	 * @return
	 */
	public int getStart() {
		return index * limit ;
	}
	
	/**
	 * 计算分页结束记录位置(Redis队列截取时，包含结束位置上的元素，所以减一)
	 * @return
	 */
	public int getEnd() {
		return getStart() + limit - 1 ;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPages() {
		if(count > 0 && limit > 0) {
			pages = count % limit == 0
					? count / limit
					: count / limit + 1 ;
		}
		return pages;
	}

	@Override
	public String toString() {
		return "[" + this.index + " ," + this.limit + " ," + this.count + " ," + this.pages
				+ "] => [" + this.getStart() + " ," + this.getEnd() + "]" ;
	}

	public Collection<T> getRecords() {
		return records;
	}

	public void setRecords(Collection<T> records) {
		this.records = records;
	}	
	
}

