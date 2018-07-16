package net.rails.ext;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.rails.support.Support;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class Json<K,V> extends IndexMap<K,V> {
	
	protected boolean skipNull = false;
	protected boolean skipBlank = false;
	
	private final static Logger log = LoggerFactory.getLogger(Json.class);
	
	public final static class Foramts {
		public static String Datetime = "yyyy-MM-dd HH:mm:ss.SSS";
		public static String Date = "yyyy-MM-dd";
		public static String Time = "HH:mm:ss.SSS";
	}
	
	public Json(){
		super();
	}
	
	public Json(K key,V value){
		super();
		put(key,value);
	}
	
	public Json(Map<K,V> m){
		super(m);
	}
	
	public Json(int initialCapacity) {
		super(initialCapacity);
	}
	
	protected String quote(String value){
		if (value == null)
			return "null";
	
		final StringBuffer sbf = new StringBuffer("\"");
//		sbf.append(value.replace("'", "\\'").replace("\n","\\n").replace("\r", "\\r").replace("\"", "\\\""));
		sbf.append(Support.code().js(value));
		sbf.append("\"");
		return sbf.toString();
	}
	
	protected String quote(Character value){
		if (value == null)
			return "null";
		else
			return quote(value.toString());
	}
	
	protected String quote(Json<K,V> value){
		if (value == null)
			return "null";
		else
			return value.toString();
	}
	
	protected String quote(Object[] value){
		if (value == null)
			return "null";
		
		return quotes(Arrays.asList(value));
	}
	
	protected String quote(List<Object> value){
		if (value == null)
			return "null";
		
		final int size = value.size();
		final StringBuffer s = new StringBuffer("[");
		s.append(debugSequence());
		for (int i = 0; i < size; i++) {
			s.append(quotes(value.get(i)));
			if (i < size - 1)
				s.append(",");

			s.append(debugSequence());
		}
		s.append("]");
		return s.toString();
	}
	
	protected String quote(Map<K,V> value){
		if (value == null)
			return "null";
		
		final StringBuffer s = new StringBuffer("{");
		s.append(debugSequence());
		List<K> keys = new ArrayList<K>(value.keySet());
		int size = keys.size();
		for (int i = 0; i < size; i++) {
			K key = keys.get(i);
			V v = value.get(key);
			s.append(quote(key,v));
			if (i < size - 1) 
				s.append(",");
			
			s.append(debugSequence());
		}
		s.append("}");
		return s.toString();
	}
	
	protected String quote(Number value){
		if(value == null)
			return "null";
		else
			return value.toString();		
	}

	protected String quote(Boolean value){
		if(value == null)
			return "null";
		else
			return value.toString();		
	}
	
	protected String quote(Object value){
		if(value == null)
			return "null";
		else
			return quote(value.toString());		
	}
	
	protected String quote(Timestamp value){
		if(value == null)
			return "null";
		else{
			SimpleDateFormat f = new SimpleDateFormat(Foramts.Datetime);
			return quote(f.format((Timestamp)value));	
		}
	}
	
	protected String quote(Date value){
		if(value == null)
			return "null";
		else{
			SimpleDateFormat f = new SimpleDateFormat(Foramts.Date);
			return quote(f.format((Date)value));	
		}
	}
	
	protected String quote(Time value){
		if(value == null)
			return "null";
		else{
			SimpleDateFormat f = new SimpleDateFormat(Foramts.Time);
			return quote(f.format((Time)value));	
		}
	}

	public static String format(Object value){
		return new Json().quotes(value);
	}
	
	@SuppressWarnings("unchecked")
	protected String quotes(Object value){
		if(value == null)
			return "null";
		
		if(value instanceof String){
			return quote(value.toString());
		}else if(value instanceof Character){
			return quote((Character)value);
		}else if(value instanceof Number){
			return quote((Number)value);
		}else if(value instanceof Boolean){
			return quote((Boolean)value);
		}else if(value instanceof Json){
			return quote((Json<K,V>)value);
		}else if(value instanceof Map){
			return quote((Map<K,V>)value);
		}else if(value instanceof List){
			return quote((List<Object>)value);
		}else if(value instanceof Timestamp){
			return quote((Timestamp)value);
		}else if(value instanceof Date){
			return quote((Date)value);
		}else if(value instanceof Time){
			return quote((Time)value);
		}else if(value instanceof Object[]){
			return quote((Object[])value);
		}else{
			return quote(value);
		}
	}
	
	protected String quoteKey(K key){
		return quote(key);
	}
	
	protected String quote(K key, V value) {
		return quoteKey(key) + ":" + quotes(value);
	}
	
	public final String generate(){
		final StringBuffer s = new StringBuffer("{");
		s.append(debugSequence());
		final List<K> keys = new ArrayList<K>(keySet());
		int size = keys.size();
		for (int i = 0; i < size; i++) {
			K key = keys.get(i);
			V value = this.get(key);
			if(skipNull && value == null)
				continue;
			
			if(skipBlank && Support.object(value).blank())
				continue;
			
			s.append(quote(key,value));
			if (i < size - 1) 
				s.append(",");
			
			s.append(debugSequence());
		}
		s.append("}");
		return s.toString();
	}
	
	@Override
	public String toString(){
		return generate();
	}
	
	public Json<K,V> append(K key,V value){
		put(key, value);
		return this;
	}
	
	public Json<K,V> append(Map<K,V> m){
		putAll(m);
		return this;
	}
	
	public Json<K,V> skipNull(boolean skipNull){
		this.skipNull = skipNull;
		return this;
	}
	
	public Json<K,V> skipBlank(boolean skipBlank){
		this.skipBlank = skipBlank;
		return this;
	}
	
	public Map<K,V> toMap(){
		return new HashMap<K,V>(this);
	}
	
	@Override
	public Json<K, V> clone() {
		return new Json<K,V>(this);
	}
	
	protected static String debugSequence() {
		if(!log.isErrorEnabled()){
			return "\n";
		}else{
			return "";
		}
	}
	
	private static Map<String,Object> parse(JSONObject json){
		JSONArray keys = json.names();
		Map<String, Object> map = new HashMap<String, Object>();
		if(keys != null){			
			for(int i = 0;i < keys.length();i++){			
				try {
					String key = keys.getString(i);
					Object value = json.get(key);
					if(value == null){
						map.put(key, null);
					}else if(value == JSONObject.NULL){
						map.put(key, null);
					}else if(value instanceof JSONObject){
						map.put(key, parse((JSONObject)value));
					}else if(value instanceof JSONArray){
						map.put(key, parse((JSONArray)value));
					}else{
						map.put(key, value);
					}
				} catch (JSONException e) {
					log.error(e.getMessage(),e);
				}				
			}			
		}
		return map;
	}
	
	public static List<Object> parse(JSONArray array){
		List<Object> list = null;
		if(array == null)
			return null;
		if(array.length() == 0)
			return new ArrayList<Object>();
		
		for(int i = 0;i < array.length();i++){
			if(list == null)
				list = new ArrayList<Object>();
			
			try {
				Object value = array.get(i);
				if(value == null){
					list.add(null);
				}else if(value == JSONObject.NULL){
					list.add(null);
				}else if(value instanceof JSONObject){
					list.add(parse((JSONObject)value));
				}else if(value instanceof JSONArray){
					list.add(parse((JSONArray)value));
				}else{
					list.add(value);
				}					
			} catch (JSONException e) {
				log.error(e.getMessage(),e);
			}			
		}
		return list;
	}
	
	public static <T extends Object> T parse(String source){
		return Json.parse(source," ");
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T parse(String source,String def){
		try{
			source = Support.string(source).def(def);
			Object o = null;
			if(source.charAt(0) == '{'){
				o = Json.parse(new JSONObject(source));
				return (T) new Json<Object,Object>((Map<Object,Object>)o);
			}else if(source.charAt(0) == '['){			
				o = Json.parse(new JSONArray(source));
				return (T) o;
			}else
				return null;
		}catch(JSONException e){
			log.error(e.getMessage(),e);
			return null;
		}		
	}
	
}
