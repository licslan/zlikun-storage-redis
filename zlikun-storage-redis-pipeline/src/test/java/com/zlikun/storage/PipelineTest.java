package com.zlikun.storage;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther zlikun <zlikun-dev@hotmail.com>
 * @date 2017/5/10 9:41
 */
public class PipelineTest extends ConfigBase {

    @Test
    public void pipe() {

        // 准备数据
        initData(this.jedis) ;

        // 查询数据
        logger.info("开始执行有返回值管道查询：");
        query(this.jedis);

        logger.info("开始执行无返回值管道查询：");
        query_2(this.jedis);
    }

    /**
     * 使用管道查询数据，管道有返回值
     * @param jedis
     */
    private void query(Jedis jedis) {
        String [] keys = new String [3] ;

        Pipeline pipe = jedis.pipelined() ;

        for (int i = 0; i < keys.length; i++) {
            keys[i] = "zlikun:" + (i + 1) + ":login" ;
            pipe.hmget(keys[i] ,"lastLoginTime" ,"loginCount") ;
        }

        // 返回值与命令返回信息一致，如本例中，返回List<String>类型数据
        List<Object> list = pipe.syncAndReturnAll() ;

        // 由输出结果可知：即使没有查到的元素，也不会返回空值，但返回值中包含的元素是空
        // 返回值信息中不包含Key信息，所以单凭结果无法建立对应映射关系(键与值之间)，但实际上返回结果与查询顺序是一致的，所以可以通过索引来获取相应的键与值关系
        int index = 0 ;
        for (Object obj : list) {
            List<String> data = (List<String>) obj;
            if(data == null) continue ;
            logger.info("fields = {} ,key = {} ,lastLoginTime = {} ,loginCount = {}" ,data.size() ,keys[index ++] ,data.get(0) ,data.get(1));
        }

    }

    /**
     * 使用管道查询数据，管道无返回值
     * @param jedis
     */
    private void query_2(Jedis jedis) {
        String [] keys = new String [3] ;

        List<Response<List<String>>> list = new ArrayList<Response<List<String>>>() ;
        Pipeline pipe = jedis.pipelined() ;

        for (int i = 0; i < keys.length; i++) {
            keys[i] = "zlikun:" + (i + 1) + ":login" ;
            list.add(pipe.hmget(keys[i] ,"lastLoginTime" ,"loginCount")) ;
        }

        // 执行同步，不直接返回信息
        pipe.sync() ;

        // 由输出结果可知：即使没有查到的元素，也不会返回空值，但返回值中包含的元素是空
        // 返回值信息中不包含Key信息，所以单凭结果无法建立对应映射关系(键与值之间)，但实际上返回结果与查询顺序是一致的，所以可以通过索引来获取相应的键与值关系
        int index = 0 ;
        for (Response<List<String>> response : list) {
            List<String> data = response.get() ;
            if(data == null) continue ;
            logger.info("fields = {} ,key = {} ,lastLoginTime = {} ,loginCount = {}" ,data.size() ,keys[index ++] ,data.get(0) ,data.get(1));
        }

    }

    /**
     * 初始化测试数据
     * @param jedis
     */
    private void initData(Jedis jedis) {

        // 清除原数据，避免干扰测试
        jedis.del("zlikun:1:login") ;
        jedis.del("zlikun:2:login") ;

        // 准备数据，使用管道批量写入数据
        Pipeline pipe = jedis.pipelined() ;

        pipe.hset("zlikun:1:login" ,"lastLoginTime" ,"2017-5-4 12:00:00") ;
        pipe.hincrBy("zlikun:1:login" ,"loginCount" ,1) ;

        pipe.hset("zlikun:1:login" ,"lastLoginTime" ,"2017-5-4 12:40:00") ;
        pipe.hincrBy("zlikun:1:login" ,"loginCount" ,1) ;

        pipe.hset("zlikun:2:login" ,"lastLoginTime" ,"2017-5-4 12:10:00") ;
        pipe.hincrBy("zlikun:2:login" ,"loginCount" ,1) ;

        // 执行同步，无返回值
        pipe.sync();
    }

}
