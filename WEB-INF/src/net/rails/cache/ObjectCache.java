package net.rails.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.rails.support.Support;

/**
 * 对象缓存管理类
 * @author Jack
 *
 */
public abstract class ObjectCache {

	private Object object;
	protected String cacheKey;	
	protected int live;
	protected boolean force = false;
	protected abstract Object execution();
	
	public ObjectCache(boolean force,int live,Object...cacheKeys) {
		super();
		this.force = force;
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public ObjectCache(int live,Object...cacheKeys) {
		super();
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	private void init(){
		if(force)
			Cache.remove(cacheKey);
		
		if(Cache.included(cacheKey))
			object = Cache.get(cacheKey);
		else{
			object = execution();
			Cache.set(cacheKey,object,live);
		}
	}	
	
	public Object getObject(){
		return object;
	}
	

}
