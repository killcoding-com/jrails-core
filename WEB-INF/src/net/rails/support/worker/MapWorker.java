package net.rails.support.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.ext.Json;
import net.rails.support.Support;

@SuppressWarnings("hiding")
public class MapWorker<K,V> {
	
	private Map<K,V> source;
	
	public MapWorker(Map<K,V> source){
		super();
		this.source = source;
	}
	
	@SuppressWarnings("unchecked")
	public <K,V> V get(K key,V def){
		return (V) Support.object(source.get(key)).def(def);
	}
	
	@SuppressWarnings("unchecked")
	public <K,V> V get(K key){
		return (V)source.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <K extends Object> List<K> keys() {
		return new ArrayList<K>(((Collection<? extends K>) source.keySet()));
	}

	public <V> V gets(String keys) {
		return gets(keys.split("[:|\\.|/|\\\\]"));
	}

	@SuppressWarnings("unchecked")
	public <V> V gets(String... keyarr) {
		V o = null;
		if (source == null)
			return null;
		
		Map<K, V> m = (Map<K, V>) source;
		for (int i = 0; i < keyarr.length; i++) {
			o = m.get(keyarr[i]);
			if (o == null) {
				break;
			} else {
				if (o instanceof Map) {
					m = (Map<K, V>) o;
					continue;
				} else {
					if (i != keyarr.length - 1)
						o = null;
				}
			}
		}
		return o;
	}
	
	public Map<String,V> containsKey(String regex){
		List<String> keys = Support.map(source).keys();
		Map<String,V> map = new HashMap<String,V>();
		for(String k : keys){
			if(k.matches(regex))
			   map.put(k,source.get(k));
		}
		return map;
	}
	
	public Map<K, V> def(String jsonStr){
		if(Support.object(source).blank())
			return (Map<K, V>) Json.parse(jsonStr);
		else
			return source;
	}
	
	public Map<K,V> getSource(){
		return source;
	}

}
