package net.rails.tpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.rails.cache.Cache;
import net.rails.support.Support;
import net.rails.web.Route;

public abstract class TplCache {

	private String tplContent;
	protected String cacheKey;	
	protected int live;
	protected boolean force = false;
	protected abstract String execution();
	
	public TplCache(boolean force,int live,Route route,Object...cacheKeys) {
		super();
		this.force = force;
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		keys.add(0,route.getController());
		keys.add(1,route.getAction());
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(int live,Route route,Object...cacheKeys) {
		super();
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
			
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));		
		keys.add(0,route.getController());
		keys.add(1,route.getAction());
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(boolean force,int live,String controller,String action,Object...cacheKeys) {
		super();
		this.force = force;
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		keys.add(0,controller);
		keys.add(1,action);
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(int live,String controller,String action,Object...cacheKeys) {
		super();
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		keys.add(0,controller);
		keys.add(1,action);
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(boolean force,int live,Object...cacheKeys) {
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
	
	public TplCache(int live,Object...cacheKeys) {
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
			tplContent = (String)Cache.get(cacheKey);
		else{
			tplContent = execution();
			Cache.set(cacheKey,tplContent,live);
		}
	}	
	
	@Override
	public String toString(){
		return Support.string(tplContent).def("");
	}

}
