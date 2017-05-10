$(function(){
	
	// 加载文章列表
	$.get("/article/list.json" ,{pageIndex : 0} ,function(data){
		console.log(data) ;
	}) ;
	
}) ;