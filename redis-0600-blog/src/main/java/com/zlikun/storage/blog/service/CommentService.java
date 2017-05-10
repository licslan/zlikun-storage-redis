package com.zlikun.storage.blog.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.zlikun.storage.blog.helper.CacheHelper;
import com.zlikun.storage.blog.helper.JsonHelper;
import com.zlikun.storage.blog.helper.PageBean;
import com.zlikun.storage.blog.model.Comment;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service("commentService")
public class CommentService {

	@Resource
	private JedisPool jedisPool ;
	
	/**
	 * 创建文章评论
	 * @param comment
	 * @param articleId
	 */
	public void create(Comment comment ,long articleId) {
		Assert.notNull(comment ,"添加评论对象不能为空!");
		comment.setUuid(UUID.randomUUID().toString());
		comment.setCreateTime(new Date());
		Jedis jedis = jedisPool.getResource() ;
		try {
			String json = JsonHelper.obj2json(comment) ;
			if(json != null) 
				jedis.lpush(CacheHelper.cacheKey("article" ,String.valueOf(articleId) ,"comments"), json) ;
		} finally {
			jedis.close();
		}
	}
	
	/**
	 * 分页查询评论列表
	 * @param articleId
	 * @param page
	 * @return
	 */
	public List<Comment> list(long articleId ,PageBean<?> page) {
		Jedis jedis = jedisPool.getResource() ;
		try {
			List<String> list = null ;
			String key = "article:" + articleId + ":comments" ;
			if(page == null || page.getLimit() == 0) {
				list = jedis.lrange(key, 0, -1) ;	// 如果分页参数为空，取出全部评论
			} else {
				list = jedis.lrange(key, page.getStart(), page.getEnd()) ;
			}
			if(CollectionUtils.isEmpty(list)) return null ;
			List<Comment> target = new ArrayList<Comment>() ;
			for(String comment : list) {
				Comment c = JsonHelper.json2obj(comment, Comment.class) ;
				if(c != null) target.add(c) ;
			}
			return target ;
		} finally {
			jedis.close();
		}
	}
	
	/**
	 * 指定文章评论数
	 * @param articleId
	 * @return
	 */
	public long count(long articleId) {
		Jedis jedis = jedisPool.getResource() ;
		try {
			return jedis.llen("article:" + articleId + ":comments") ;
		} finally {
			jedis.close();
		}
	}
	
}
