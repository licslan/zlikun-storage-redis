package com.zlikun.storage.blog.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.zlikun.storage.blog.helper.CacheHelper;
import com.zlikun.storage.blog.helper.PageBean;
import com.zlikun.storage.blog.model.Article;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service("articleService")
public class ArticleService {

	@Resource
	private JedisPool jedisPool ;
	
	private static final Logger log = LoggerFactory.getLogger(ArticleService.class) ;
	
	/** 日期格式 */
	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss" ;
	
	/** 缩略名To文章ID */
	private static final String KEY_SLUG_TO_ARTICLEID = "SLUG_TO_ARTICLEID" ;
	/** 文章ID列表，用于分页查询文章 */
	private static final String KEY_ARTICLE_ID_LIST = "ARTICLE_ID_LIST" ;
	/** 最新文章ID列表，用于显示最新文章 */
	private static final String KEY_NEWEST_ARTICLE_ID_LIST = "NEWEST_ARTICLE_ID_LIST" ;
	
	/**
	 * 创建文章，并返回生成的文章ID
	 * @param article
	 * @return
	 */
	public long create(Article article) {
		// 基本校验(必填字段验证等)
		Jedis jedis = jedisPool.getResource() ;
		try {
			// 生成文章主键
			long articleId = jedis.incr(articleKey("count")) ;
			article.setArticleId(articleId);
			article.setCreateTime(new Date());
			// 判断文章缩略名是否存在，不存在记录之
			if(article.getSlug() != null) {
				long flag = jedis.hsetnx(KEY_SLUG_TO_ARTICLEID, article.getSlug(), String.valueOf(articleId)) ;
				if(flag == 0L) throw new IllegalArgumentException("文章缩略名重复!") ;
			}
			// 写入文章其它信息
			String reply = jedis.hmset(articleKey(articleId), toMap(article)) ;
			if(StringUtils.equals(reply, "OK")) {
				// 文件创建成功，将ID写入队列，便于分页查询(倒序排列)
				jedis.lpush(KEY_ARTICLE_ID_LIST, String.valueOf(articleId)) ;
				// 维护最新文章(5篇)
				jedis.lpush(KEY_NEWEST_ARTICLE_ID_LIST, String.valueOf(articleId)) ;
				jedis.ltrim(KEY_NEWEST_ARTICLE_ID_LIST, 0, 4) ;	// 清除索引范围外的元素
				return articleId ;
			} else throw new RuntimeException("创建文章出错!") ;
		} finally {
			jedis.close();
		}
	}
	
	private Map<String ,String> toMap(Article article) {
		Assert.notNull(article ,"文章对象不能为空!");
		Map<String ,String> map = new HashMap<String, String>() ;
		if(article.getArticleId() != null) map.put("articleId", article.getArticleId().toString()) ;
		if(article.getContent() != null) map.put("content", article.getContent()) ;
		if(article.getCreateTime() != null) map.put("createTime", DateFormatUtils.format(article.getCreateTime(), DATE_PATTERN)) ;
		if(article.getSummary() != null) map.put("summary", article.getSummary()) ;
		if(article.getTitle() != null) map.put("title", article.getTitle()) ;
		if(article.getSlug() != null) map.put("slug", article.getSlug()) ;
		return map ;
	}
	
	/**
	 * 查询单个文章
	 * @param articleId
	 * @return
	 */
	public Article get(long articleId) {
		Jedis jedis = jedisPool.getResource() ;
		try {
			return toArticle(jedis.hgetAll(articleKey(articleId))) ;
		} finally {
			jedis.close();
		}
	}
	
	private Article toArticle(Map<String, String> map) {
		if(map == null) return null ;
		Article article = new Article() ;
		if(map.containsKey("articleId")) article.setArticleId(NumberUtils.toLong(map.get("articleId")));
		if(map.containsKey("title")) article.setTitle(map.get("title"));
		if(map.containsKey("summary")) article.setSummary(map.get("summary"));
		if(map.containsKey("content")) article.setContent(map.get("content"));
		if(map.containsKey("slug")) article.setSlug(map.get("slug"));
		if(map.containsKey("createTime")) {
			try {
				article.setCreateTime(DateUtils.parseDate(map.get("createTime") ,DATE_PATTERN));
			} catch (ParseException e) {
				log.warn("文章创建时间格式转换出错!");
			}
		}
		if(map.containsKey("views")) article.setViews(NumberUtils.toLong(map.get("views")));
		return article;
	}

	/**
	 * 获取文章总数
	 * @return
	 */
	public long countArticle() {
		Jedis jedis = jedisPool.getResource() ;
		try {
			return jedis.llen(KEY_ARTICLE_ID_LIST) ;
		} finally {
			jedis.close();
		}
	}
	
	/**
	 * 增加并返回最新文件浏览量(增加长度为1)
	 * @param articleId
	 * @return
	 */
	public long doViews(long articleId) {
		return doViews(articleId ,1L) ;
	}
	
	/**
	 * 增加并返回最新文件浏览量(增加长度为incrBy值)
	 * @param articleId
	 * @param incrBy
	 * @return
	 */
	public long doViews(long articleId ,long incrBy) {
		Jedis jedis = jedisPool.getResource() ;
		try {
			return jedis.hincrBy(articleKey(articleId), "views", incrBy) ;
		} finally {
			jedis.close();
		}
	}
	
	/**
	 * 删除一篇文章，返回删除文章数(0表示要删除的文章不存在)
	 * @param articleId
	 */
	public long delete(long articleId) {
		Jedis jedis = jedisPool.getResource() ;
		try {
			// 从ID列表中删除文章ID
			jedis.lrem(KEY_ARTICLE_ID_LIST, 1, String.valueOf(articleId)) ;
			// 删除文章本身
			return jedis.del(articleKey(articleId)) ;
		} finally {
			jedis.close();
		}
	}
	
	/**
	 * 根据文章缩略名获取文章
	 * @param slug
	 * @return
	 */
	public Article getArticleBySlug(String slug) {
		String articleId = null ;
		Jedis jedis = jedisPool.getResource() ;
		try {
			articleId = jedis.hget(KEY_SLUG_TO_ARTICLEID, slug) ;
		} finally {
			jedis.close();
		}
		if(articleId == null) return null ;
		return get(NumberUtils.toLong(articleId)) ;
	}
	
	/**
	 * 更新文章(空值不更新)
	 * @param article
	 */
	public void update(Article article) {
		Assert.notNull(article);
		Assert.notNull(article.getArticleId());
		Map<String ,String> map = toMap(article) ;
		// 更新时，加入更新时间字段
		map.put("updateTime", DateFormatUtils.format(new Date(), DATE_PATTERN)) ;
		Jedis jedis = jedisPool.getResource() ;
		try {
			String key = articleKey(article.getArticleId()) ;
			// slug如果有更新，需要判断是否有重复
			if(article.getSlug() != null) {
				long flag = jedis.hsetnx(KEY_SLUG_TO_ARTICLEID, article.getSlug(), String.valueOf(article.getArticleId())) ;
				if(flag == 0L) throw new IllegalArgumentException("文章缩略名重复!") ;
				// 旧的slug记录应被删除
				String oldSlug = jedis.hget(key, "slug") ;
				jedis.hdel(KEY_SLUG_TO_ARTICLEID, oldSlug) ;
			}
			jedis.hmset(key, map) ;
		} finally {
			jedis.close();
		}
	}
	
	/**
	 * 分页查询文章列表
	 * @param page
	 * @return
	 */
	public List<Article> list(PageBean<Article> page) {
		Assert.notNull(page ,"分页参数不允许为空!");
		List<Article> list = new ArrayList<Article>() ;
		Jedis jedis = jedisPool.getResource() ;
		try {
			// 从列表中截取指定个元素
			List<String> articleIds = jedis.lrange(KEY_ARTICLE_ID_LIST, page.getStart(), page.getEnd()) ;
			if(CollectionUtils.isEmpty(articleIds)) return null ;
			// 根据ID列表，查询文章列表
			for(String articleId : articleIds) {
				// 仅取出需要的字段(列表一般不会展示正文，所以不查询此字段信息)
				List<String> result = jedis.hmget(articleKey(articleId), "title" ,"summary" ,"slug" ,"views" ,"createTime") ;
				// 将结果集转换为Map结构，以便于转换为对象
				Map<String ,String> map = new HashMap<String, String>() ;
				map.put("title", result.get(0)) ;
				map.put("summary", result.get(1)) ;
				map.put("slug", result.get(2)) ;
				map.put("views", result.get(3)) ;
				map.put("createTime", result.get(4)) ;
				map.put("articleId", articleId) ;
				list.add(toArticle(map)) ;
			}
		} finally {
			jedis.close();
		}
		return list ;
	}
	
	/**
	 * 构建文章Key
	 * @param part
	 * @return
	 */
	private String articleKey(String part) {
		return CacheHelper.cacheKey("article:" ,part) ;
	}
	
	private String articleKey(long part) {
		return CacheHelper.cacheKey("article:" ,String.valueOf(part)) ;
	}
	
	/**
	 * 文章添加标签
	 * @param articleId
	 * @param tags	多个标签参数
	 */
	public void addTags(long articleId ,String ... tags) {
		Jedis jedis = jedisPool.getResource() ;
		// 保存文章标签文章(便于搜索)
		for(int i = 0 ,len = tags.length ; i < len ; i ++) {
			jedis.sadd(CacheHelper.cacheKey("tag" ,tags[i] ,"article"), String.valueOf(articleId)) ;
		}
		// 保存文章标签
		jedis.sadd(CacheHelper.cacheKey("article" ,String.valueOf(articleId) ,"tags"), tags) ;
		jedis.close();
	}
	
	/**
	 * 根据标签查询文章列表
	 * @param tag
	 * @return
	 */
	public List<Article> listByTag(String tag) {
		Jedis jedis = jedisPool.getResource() ;
		try {
			// 保存文章标签文章(便于搜索)
			Set<String> articleIds = jedis.smembers(CacheHelper.cacheKey("tag" ,tag ,"article")) ;
			// 查询文章列表
			List<Article> list = new ArrayList<Article>() ;		
			String [] fields = new String [] {"articleId" ,"title" ,"createTime" ,"slug" ,"views"} ;
			for(String articleId : articleIds) {
				// XXX 结果顺序与字段顺序是否一致？
				List<String> results = jedis.hmget(articleKey(articleId), fields) ;
				if(results == null || results.size() != fields.length) throw new RuntimeException("查询列表数据与字段列表不一致!") ;
				Map<String ,String> map = new HashMap<String, String>() ;
				for(int i = 0 ,len = fields.length ; i < len ; i ++) {
					map.put(fields[i], results.get(i)) ;
				}
				list.add(toArticle(map)) ;
			}
			return list ;
		} finally {
			jedis.close();
		}
	}
	
}
