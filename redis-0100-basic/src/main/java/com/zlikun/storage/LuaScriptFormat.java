package com.zlikun.storage ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * Redis脚本生成器(去除注释、空行等)
 * @author	zhanglikun
 * @date	2015年12月3日 下午8:17:42
 */
public class LuaScriptFormat {

	public static void main(String [] args) {
		
		Jedis jedis = new Jedis("redis.i.zlikun.com");
		jedis.auth("ablejava") ;
		jedis.select(5) ;

		Set<File> luaFiles = new HashSet<File>() ;
		
		// 读取配置文件目录
		URL url = LuaScriptFormat.class.getClassLoader().getResource("scripts") ;
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
					StringBuffer content = new StringBuffer() ;
					StringBuffer formatContent = new StringBuffer() ;
					for(String line = fis.readLine() ; line != null ;line = fis.readLine()) {
						line = line.trim() ;
						if(line.equals("") || line.startsWith("--")) continue ;
						content.append(line + " ") ;
						formatContent.append(line + "\r\n") ;
					}
					
					String evalsha = jedis.scriptLoad(content.toString()) ;
					
					System.out.println(String.format("%s -> %s", f.getName() ,evalsha));
					System.out.println("=========================带格式===================================");
					System.out.println(formatContent.toString());
					System.out.println("========================不带格式===================================");
					System.out.println(content.toString());
					System.out.println("==========================结束====================================");
					System.out.println("\r\n\r\n");
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
		
		if(jedis != null) jedis.close();
		
	}
	
}
