package net.rails.cache;

import net.rails.support.Support;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


public final class EhcacheApi extends AbsCacheApi {
	
	private static Logger log = LoggerFactory.getLogger(EhcacheApi.class);
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

	@Override
	public synchronized void set(String name,Object value,int live){
	    if(!included(name)){
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
    			log.info(e.getMessage(),e);
    		}	
	    }
	}
	
	@Override
	public Object get(String name) {		
		if(!included(name))
			return null;
		
		try{
			Cache cache = cm.getCache(name);
			if(cache != null && cache.getQuiet("Data") != null){
				Element element = (Element) cache.get("Data").clone();
				return element.getObjectValue();
			}else
				return null;
		}catch(Exception e){
		    log.info(e.getMessage(),e);
			return null;
		}		
	}
	
	@Override
	public boolean included(String name){
	    if(cm.cacheExists(name)){
	        Cache cache = cm.getCache(name);
	        Element element = (Element)cache.get("Data");
	        if(element == null){
	            return false;
	        }else{
	            if(element.isExpired()){
	                cache.removeAll();
	                cm.removeCache(name);
	                return false;
	            }else{
	                return true;
	            }
	        }
	    }else{
	        return false;
	    }
	}
	
	public synchronized void remove(String name){		
		boolean b = cm.cacheExists(name);
		if(b){
		    Cache cache = cm.getCache(name);
		    cache.removeAll();
			cm.removeCache(name);
		}
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
