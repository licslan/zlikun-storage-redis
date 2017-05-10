package com.zlikun.storage.blog.service;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.zlikun.storage.blog.TestBase;
import com.zlikun.storage.blog.model.Comment;

public class CommentServiceTest extends TestBase {

	@Resource
	private CommentService commentService ;
	
	@Test @Ignore
	public void testCreate() {
		Comment comment = new Comment() ;
		comment.setContent("测试个评论");
		comment.setUsername("jackson");
		comment.setEmail("jackson@v-log.cn");
		commentService.create(comment, 1L);
	}
	
	@Test
	public void testList() {
		List<Comment> list = commentService.list(1L, null) ;
		Assert.assertNotNull(list);
	}
	
	@Test
	public void testCount() {
		Assert.assertTrue(commentService.count(1L) >= 1L);
	}
	
}
