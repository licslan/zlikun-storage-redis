package com.zlikun.storage.blog.service;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.zlikun.storage.blog.TestBase;
import com.zlikun.storage.blog.helper.PageBean;
import com.zlikun.storage.blog.model.Article;

public class ArticleServiceTest extends TestBase {

	@Resource
	private ArticleService articleService ;
	
	@Test @Ignore
	public void testCreate() {
		Article article = new Article() ;
		article.setTitle("测试文章-标题");
		article.setSummary("测试文章-摘要");
		article.setContent("测试文章-正文");
		article.setSlug("test-article-by-redis");
		long articleId = articleService.create(article) ;
		Assert.assertTrue(articleId > 0L);
	}
	
	@Test
	public void testGet() {
		Article article = articleService.get(1L) ;
		Assert.assertNotNull(article);
		Assert.assertEquals("测试文章-标题", article.getTitle());
	}
	
	@Test
	public void testCountArticle() {
		long count = articleService.countArticle() ;
		Assert.assertTrue(count > 0L);
	}
	
	@Test
	public void testDoViews() {
		long views = articleService.doViews(1) ;
		Assert.assertTrue(views > 0L);
	}
	
	@Test
	public void testDelete() {
		long delTotal = articleService.delete(2L);
		Assert.assertEquals(0L, delTotal);
	}
	
	@Test
	public void testGetArticleBySlug() {
		Article article = articleService.getArticleBySlug("test-article-by-redis") ;
		Assert.assertNotNull(article);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testUpdate() {
		Article article = articleService.get(1L) ;
		article.setContent("测试文章-正文-更新");
		articleService.update(article);
		
		// 缩略名相同，应抛出异常
		article.setSlug("test-article-by-redis");
		articleService.update(article);
	}
	
	@Test
	public void testList() {
		List<Article> list = articleService.list(new PageBean<Article>(5)) ;
		Assert.assertNotNull(list);
		Assert.assertEquals("测试文章-标题" ,list.get(0).getTitle());
	}
	
	@Test
	public void testAddTags() {
		articleService.addTags(1L, "Java" ,"Redis" ,"博客" ,"Redis");
	}
	
}
