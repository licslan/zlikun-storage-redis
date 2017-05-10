package com.zlikun.storage.blog.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.zlikun.storage.blog.helper.PageBean;
import com.zlikun.storage.blog.model.Article;
import com.zlikun.storage.blog.service.ArticleService;

@Controller
@RequestMapping("/article")
public class ArticleController extends BaseController {

	@Resource
	private ArticleService articleService ;
	
	@RequestMapping("/page/{pageIndex}")
	public ModelAndView list(@PathVariable("pageIndex") int pageIndex) {
		ModelAndView mav = new ModelAndView("article/list") ;
		long articleCount = articleService.countArticle() ;
		mav.addObject("articleCount", articleCount) ;
		if(articleCount > 0L) {
			List<Article> list = articleService.list(new PageBean<Article>(pageIndex - 1, 10)) ;
			if(!CollectionUtils.isEmpty(list)) {
				mav.addObject("articles", list) ;
			}
		}
		return mav ;
	}
	
	@RequestMapping("/{slug}")
	public ModelAndView details(@PathVariable("slug") String slug) {
		ModelAndView mav = new ModelAndView("article/details") ;
		mav.addObject("article", articleService.getArticleBySlug(slug)) ;
		return mav ;
	}
	
}
