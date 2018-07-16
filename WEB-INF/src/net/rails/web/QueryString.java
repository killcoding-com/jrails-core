package net.rails.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.ext.IndexMap;
import net.rails.support.Support;

@SuppressWarnings("serial")
public class QueryString extends IndexMap<String,Object> {
	
	protected Logger log;
	
	public QueryString(){
		super();
		log = LoggerFactory.getLogger(getClass());
	}

	public QueryString(String key,Object value){
		super();
		put(key,value);
	}
	
	public QueryString(Map<String,Object> m){
		super(m);
	}
	
	public static Map<String,Object> parse(String queryString){
		if(queryString == null)
			return null;
		
		Map<String, Object> map = new HashMap<String,Object>();
		String[] qss = queryString.split("&");
		for(String kvs : qss){
			String[] kv = kvs.split("=");
			if(kv.length == 2){
				String k = kv[0];
				String v = kv[1];
				if(map.containsKey(k)){
					if(map.get(k) instanceof List){
						List<String> vs = (List)map.get(k);
						vs.add(v);
						map.put(k,vs);
					}else{
						List<String> vs = new ArrayList<String>();
						vs.add((String)map.get(k));
						vs.add(v);
						map.put(k,vs);
					}
				}else{
					map.put(k, v);
				}
			}else{
				map.put(kv[0],"");
			}
		}
		return map;
	}	
	
	@SuppressWarnings("unchecked")
	public Object put(String key,Object value){
		List<Object> values = null;
		if(this.containsKey(key)){
			if(get(key) instanceof List){
				values = (List<Object>)get(key);
				values.add(value);
			}else{
				values = new ArrayList<Object>();
				values.add(get(key));
				values.add(value);
			}			
			return super.put(key, values);
		}else{
			return super.put(key, value);
		}
	}
	
	public QueryString append(String key,Object value){
		put(key,value);
		return this;
	}
	
	@SuppressWarnings({"unchecked" })
	protected String toQueryString(){
		try{
			final List<String> keys = Support.map(this).keys();
			final StringBuffer qs = new StringBuffer();
			for(int i = 0;i < keys.size();i++){
				String k = keys.get(i);
				Object v = this.get(k);
				if(v instanceof List){
					@SuppressWarnings("rawtypes")
					List ls = (List)v; 
					for(int j = 0;j < ls.size();j++){
						String val = Support.object(ls.get(j)).def("").toString();
						qs.append(MessageFormat.format("{0}={1}",k,URLEncoder.encode(val,Support.config().env().getApplicationCharset())));
						if(j < ls.size()-1)
							qs.append("&");
					}				
				}else{
					String val = Support.object(v).def("").toString();
					qs.append(MessageFormat.format("{0}={1}",k,URLEncoder.encode(val,Support.config().env().getApplicationCharset())));
				}
				if(i < keys.size()-1)
					qs.append("&");
			}
			return qs.toString();
		}catch(IOException e){
			log.error(e.getMessage(),e);
			return null;
		}
	}
	
	@Override
	public String toString(){
		return toQueryString();
	}
	
}
