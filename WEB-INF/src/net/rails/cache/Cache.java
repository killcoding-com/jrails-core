package net.rails.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.support.Support;

/**
 * 缓存管理
 * @author Jack
 *
 */
public final class Cache {

	private static AbsCacheApi api;
	private static final Logger log = LoggerFactory.getLogger(Cache.class);
	
	static{
		try{
			String apiCls = (String)Support.config().getConfig().get("env").get("cache_api");
			apiCls = Support.string(apiCls).def(net.rails.cache.EhcacheApi.class.getName());
			api = (AbsCacheApi) Class.forName(apiCls).getConstructor().newInstance();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * 设置缓存。
	 * @param name 缓存名称
	 * @param value 缓存内容
	 * @param live 缓存生命时间（单位：秒）
	 */
	public static void set(String name,Object value,int live){
		if(live == 0)
			return;
		
		log.debug("Set Cache : "  + name);
		api.set(name, value, live);
	}
	
	/**
	 * 获取一个缓存。
	 * @param name 缓存名称
	 * @return 缓存结果
	 */
	public static Object get(String name){
		log.debug("Get Cache : " + name);
		return api.get(name);
	}
	
	/**
	 * 检查缓存是否已经存在。
	 * @param name 缓存名称
	 * @return 缓存存在返回true否则返回false
	 */
	public static boolean included(String name){
		log.debug("Included Cache : " + name);
		boolean b = api.included(name);
		log.debug("Included : " + b);
		return b;
	}
	
	/**
	 * 删除缓存。
	 * @param name 指定删除的缓存名称
	 */
	public static void remove(String name){
		log.debug("Remove Cache : " + name);
		api.remove(name);
	}
	
	/**
	 * 删除所有缓存。
	 */
	public static void removeAll(){
		log.debug("Remove All Cache");
		api.removeAll();
	}

}