<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>文章列表</title>
</head>
<body>

	<h3>文章列表(${articleCount })</h3>
	
	<ul>
	<c:forEach items="${articles }" var="article">
		<li><a href="/article/${article.slug }" target="_blank">${article.title }</a></li>	
	</c:forEach>
	</ul>
	
	<!-- <script src="//cdn.bootcss.com/jquery/2.1.4/jquery.min.js"></script> -->
	<!-- <script src="/resources/js/article/article.js"></script> -->
	
</body>
</html>