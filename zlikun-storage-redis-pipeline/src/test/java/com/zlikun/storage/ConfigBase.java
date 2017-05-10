package com.zlikun.storage;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @auther zlikun <zlikun-dev@hotmail.com>
 * @date 2017/5/10 9:34
 */
public abstract class ConfigBase {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Jedis jedis ;
    private long time ;

    @Before
    public void init() {
        jedis = new Jedis("192.168.9.205" ,6379 ,1000);
        time = System.currentTimeMillis() ;
    }

    @After
    public void destroy() {
        logger.info("程序执行：{} 毫秒!" ,System.currentTimeMillis() - time) ;
        if(jedis != null) jedis.close();
    }

}
