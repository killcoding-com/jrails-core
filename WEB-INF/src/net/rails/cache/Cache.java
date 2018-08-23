package net.rails.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.support.Support;

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

	public static void set(String name,Object value,int live){
		if(live == 0)
			return;
		
		log.debug("Set Cache : "  + name);
		api.set(name, value, live);
	}
	
	public static Object get(String name){
		log.debug("Get Cache : " + name);
		return api.get(name);
	}
	
	public static boolean included(String name){
		log.debug("Included Cache : " + name);
		boolean b = api.included(name);
		log.debug("Included : " + b);
		return b;
	}
	
	public static void remove(String name){
		log.debug("Remove Cache : " + name);
		api.remove(name);
	}
	
	public static void removeAll(){
		log.debug("Remove All Cache");
		api.removeAll();
	}

}