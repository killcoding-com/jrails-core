package net.rails.cache;

import net.rails.support.Support;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;

/**
 * Support ehcache-core-2.6.0.jar JDK 1.6 above
 * @author Jack
 *
 */
public final class EhcacheApi extends AbsCacheApi {
	
	private CacheManager cm;
	
	public EhcacheApi() {
		super();
		Configuration conf = new Configuration();
		conf.setUpdateCheck(false);
		conf.setDynamicConfig(true);
		DiskStoreConfiguration dconf = new DiskStoreConfiguration();
		dconf.setPath("java.io.tmpdir");
		conf.diskStore(dconf);
		cm = CacheManager.create(conf);
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @param live  -1 longtime,0 no cache, >= 1 live time rage
	 */
	@Override
	public synchronized void set(String name,Object value,int live){
		int t = 0;
		if(live > 0)
			t = live;
		
		int memory = Support.config().env().getNumber("cache_memory", 10).intValue();
		try{
			remove(name);
			cm.addCache(new Cache(name,memory,true,live <= -1,t,t));
			Cache cache = cm.getCache(name);
			Element element = new Element("Data",value);      
			cache.put(element);	
		} catch (Exception e) {
			
		}		
	}
	
	@Override
	public Object get(String name) {		
		if(!cm.cacheExists(name))
			return null;
		
		try{
			Cache cache = cm.getCache(name);
			if(cache != null && cache.getQuiet("Data") != null){
				Element element = (Element) cache.get("Data").clone();
				return element.getObjectValue();
			}else
				return null;
		}catch(Exception e){
			return null;
		}		
	}
	
	@Override
	public boolean included(String name){	
		return cm.cacheExists(name);
	}
	
	public synchronized void remove(String name){		
		boolean b = cm.cacheExists(name);
		if(b)
			cm.removeCache(name);
	
	}
	
	@Override
	public synchronized void removeAll(){		
		cm.removalAll();	
	}

	@Override
	public String[] getNames() {
		return cm.getCacheNames();
	}
	
}
