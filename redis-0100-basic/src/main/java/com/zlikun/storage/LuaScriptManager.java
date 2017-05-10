package com.zlikun.storage ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * Redis用Lua脚本管理器
 * @author	zhanglikun
 * @date	2015年11月13日 下午5:01:25
 */
public class LuaScriptManager {

	// 文件名与lua脚本sha值Map
	private static final Map<String ,String> evalshaMap = new HashMap<String, String>() ;

	private static LuaScriptManager instance ;
	
	private Jedis jedis ;
	
	private LuaScriptManager() {
		this.jedis = new Jedis("redis.i.zlikun.com");
		this.jedis.auth("ablejava") ;
		this.jedis.select(5) ;
	}
	
	private LuaScriptManager(Jedis jedis) {
		if(jedis == null) throw new IllegalArgumentException("Jedis参数不能为空!") ;
		this.jedis = jedis ;
	}

	/**
	 * 获取LuaScriptManager实例
	 * @return
	 */
	public static final LuaScriptManager getInstance() {
		if(instance == null) {
			instance = new LuaScriptManager() ;
			instance.init() ;
		}
		return instance ;
	}

	public static final LuaScriptManager getInstance(Jedis jedis) {
		if(instance == null) {
			instance = new LuaScriptManager(jedis) ;
			instance.init() ;
		}
		return instance ;
	}
	
	public void init() {

		Set<File> luaFiles = new HashSet<File>() ;
		
		// 读取配置文件目录
		URL url = LuaScriptManager.class.getClassLoader().getResource("scripts") ;
		if(url != null) {
			File dir = new File(url.getFile()) ;
			// 获取所有脚本文件
			File [] files = dir.listFiles() ;
			if(files != null && files.length != 0) {
				for(File f : files) {
					if(f.getName().endsWith(".lua")) {
						luaFiles.add(f) ;
					}
				}
			}
		}
		
		// 遍历Lua脚本集合，获取
		if(luaFiles != null && !luaFiles.isEmpty()) {
			for(File f : luaFiles) {
				BufferedReader fis = null ;
				try {
					fis = new BufferedReader(new InputStreamReader(new FileInputStream(f))) ;
					StringBuffer sb = new StringBuffer() ;
					for(String line = fis.readLine() ; line != null ;line = fis.readLine()) {
						if(line == null || line.trim().equals("") || line.trim().startsWith("--")) continue ;
						sb.append(line + "\r\n") ;
					}
					String evalsha = this.jedis.scriptLoad(sb.toString()) ;
					evalshaMap.put(f.getName(), evalsha) ;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(fis != null)
						try {
							fis.close() ;
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
		}
		
		
	}
	
	/**
	 * 测试用例
	 * @param args
	 */
	public static void main(String[] args) {
		String evalsha = LuaScriptManager.getInstance().getEvalsha("video_watch.lua") ;
		System.out.println(evalsha);
	}

	/**
	 * 根据脚本文件名称获取脚本evalsha值
	 * @param scriptname
	 * @return
	 */
	public String getEvalsha(String scriptname) {
		return evalshaMap.get(scriptname) ;
	}
	
}
