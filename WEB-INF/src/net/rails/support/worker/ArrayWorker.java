package net.rails.support.worker;

import java.util.HashSet;
import java.util.List;
import net.rails.ext.Json;
import net.rails.support.Support;

public final class ArrayWorker<T> {
	
	private List<T> source;
	
	public ArrayWorker(List<T> source){
		super();
		this.source = source;
	}
	
	public String join(String sym){
		if(source == null)
			return "";
		
		StringBuffer sbf = new StringBuffer();
		for(int i = 0;i < source.size();i++){
			sbf.append(source.get(i));
			if(i < source.size() - 1)
				sbf.append(sym);
		}
		return sbf.toString();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void unique(){
		HashSet hs = new HashSet(source);
		source.clear();
		source.addAll(hs);
	}
	
	public List<T> def(String defaultValue){
		if(Support.object(source).blank())
			return (List<T>) Json.parse(defaultValue);
		else
			return source;
	}
	
	public List<T> getSource(){
		return source;
	}
	
}
