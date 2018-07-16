package net.rails.sql.worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class SqlWorker implements Cloneable{	
	
	private List<Object> params;
	private String sql;
	private String cacheName;
	private boolean cached = false;
	private int cacheSecond = 0;
	private boolean cacheForced = false;
	private int maxRows;
	
	public SqlWorker(String sql){
		super();
		this.sql = sql;
		this.params = new ArrayList<Object>();
	}
	
	public SqlWorker(String sql,Map<String,Object> params){
		super();
		this.sql = sql;
		init(params);
	}
	
	public SqlWorker(String sql,Object[] params){
		super();
		this.sql = sql;
		init(Arrays.asList(params));
	}
	
	public SqlWorker(String sql,List<Object> params){
		super();
		this.sql = sql;
		init(params);
	}	
	
	@Override
    public SqlWorker clone() throws CloneNotSupportedException {
		SqlWorker o = (SqlWorker)super.clone();
		SqlWorker c = new SqlWorker(o.sql){};
		c.params.clear();
		c.params.addAll(o.params);
		c.sql = sql;
		c.cached = o.cached;
		c.cacheName = null;
		c.cacheSecond = 0;
		return c;
	}	
	
	private void init(Map<String,Object> params){
		this.params = new ArrayList<Object>();
		Pattern p = Pattern.compile(":[\\w-]+");
		Matcher m = p.matcher(sql);
		while(m.find()){
			String key = m.group().replaceFirst(":","");
			if(params.get(key) instanceof Object[] || params.get(key) instanceof List){
				sql = sql.replaceFirst(p.pattern(),join(params.get(key)));
			}else{				
				this.params.add(params.get(key));
				sql = sql.replaceFirst(p.pattern(),"?");
			}
		}
	}
	
	private void init(List<Object> params){
		params = new ArrayList<Object>(params);		
		Pattern p = Pattern.compile("\\?");
		Matcher m = p.matcher(sql);
		int index = 0;
		while(m.find()){
			if(params.get(index) instanceof Object[] || params.get(index) instanceof List){
				sql = new StringBuffer(sql).replace(m.start(),m.end(),join(params.get(index))).toString();
				m = p.matcher(sql);
				params.remove(index);
				index = 0;
			}else{
				index++;			
			}
		}
		this.params = params;
	}

	private String join(Object arrs){
		List<Object> list = null;
		StringBuffer sbf = new StringBuffer();
		if(arrs instanceof Object[]){
			list = new ArrayList(Arrays.asList((Object[])arrs));
		}else{
			list = new ArrayList((List<Object>) arrs);
		}
		list.remove(null);
		if(list.size() == 0){
			list.add("[SIZE_0]");
		}
		for(int i = 0;i < list.size();i++){
			if(list.get(i) instanceof Number)
				sbf.append(list.get(i));
			else
				sbf.append("'" + list.get(i).toString().replace("'","\\\\'") + "'");
			
			if(i < list.size() - 1)
				sbf.append(",");
		}
		return sbf.toString();
	}

	public List<Object> getParams() {
		return params;
	}

	public String getSql() {
		return sql;
	}
	
	public void setCached(boolean cached){
		this.cached = cached;
	}

	public boolean isCached(){
		return this.cached;
	}
	
	public void setCacheSecond(int cacheSecond){
		this.cacheSecond = cacheSecond;
	}
	
	public int getCacheSecond(){
		return this.cacheSecond;
	}
	
	public void setCacheName(String cacheName){
		this.cacheName = cacheName;
	}
	
	public String getCacheName(){
		return this.cacheName;
	}
	
	public void setCacheForced(boolean cacheForced){
		this.cacheForced = cacheForced;
	}
	
	public boolean isCacheForced(){
		return this.cacheForced;
	}

	public void setMaxRows(int maxRows){
		this.maxRows = maxRows;
	}
	
	public int getMaxRows(){
		return maxRows;
	}
	
}
