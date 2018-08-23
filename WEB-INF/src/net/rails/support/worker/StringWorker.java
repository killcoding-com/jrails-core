package net.rails.support.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringWorker {
	
	public String source;
	
	public StringWorker(String source){
		super();
		this.source = source;
	}

	public boolean nil(){
		return source == null;
	}
	
	public boolean blank(){
		return nil() || source.trim().equals("");
	}
	
	public String firstUpCase(){
		if(blank())
			return source;
		
		String s = source.substring(0,1).toUpperCase();
		if(source.length() > 1)
			 s += source.substring(1);
		return s;
	}
	
	public String firstLowerCase(){
		if(blank())
			return source;
		
		String s = source.substring(0,1).toLowerCase();
		if(source.length() > 1)
			 s += source.substring(1);
		return s;
	}
	
	public String lastUpCase(){
		if(blank())
			return source;
		
		String s = "";
		if(source.length() > 1){
			s = source.substring(0,source.length() - 1);
			s += source.substring(source.length() - 1).toUpperCase();
		}else{
			s = source.toUpperCase();
		}
		return s;
	}
	
	public String lastLowerCase(){
		if(blank())
			return source;
		
		String s = "";
		if(source.length() > 1){
			s = source.substring(0,source.length() - 1);
			s += source.substring(source.length() - 1).toLowerCase();
		}else{
			s = source.toUpperCase();
		}
		return s;
	}
	
	public String chop(){
		if(blank())
			return source;
		
		return source.substring(0,source.length() - 1);		
	}
	
	public String def(String defaultValue){
		if(blank())
			return defaultValue;
		else
			return source;
	}
	
	public boolean isDateFormat(String dateFormat){
		String f = dateFormat + "";
		Map<String, String> fs = new HashMap<String,String>();
		fs.put("y", "[0-9]{1}");
		fs.put("M", "[0-9]{1}");
		fs.put("MM", "([0-9]{1}|(0\\d){1}|10|11|12){1}");
		fs.put("d", "[0-9]{1}");
		fs.put("dd", "([1-9]{1}|(0\\d){1}|(1\\d){1}|(2[0-9]){1}|30|31){1}");
		fs.put("a", "(AM|am|PM|pm){1}");
		fs.put("H", "[0-9]{1}");
		fs.put("HH", "([0-9]{1}|(0\\d){1}|(1\\d){1}|(2[0-3])){1}");
		fs.put("k", "[0-9]{1}");
		fs.put("kk", "([1-9]{1}|(0\\d){1}|(1\\d){1}|(2[0-4])){1}");
		fs.put("K", "[0-9]{1}");
		fs.put("KK", "([0-9]{1}|(0\\d){1}|(10)|(11)){1}");
		fs.put("h", "[0-9]{1}");
		fs.put("hh", "([1-9]{1}|(0\\d){1}|(10)|(11)|(12)){1}");
		fs.put("m", "[0-9]{1}");
		fs.put("mm", "([0-9]{1}|(0\\d){1}|(1\\d){1}|(2\\d){1}|(3\\d){1}|(4\\d){1}|(5\\d){1}){1}");
		fs.put("s", "[0-9]{1}");
		fs.put("ss", "([0-9]{1}|(0\\d){1}|(1\\d){1}|(2\\d){1}|(3\\d){1}|(4\\d){1}|(5\\d){1}){1}");
		fs.put("S", "[0-9]{1}");
		fs.put("SS", "[0-9]{2}");
		fs.put("SSS", "([0-9]{1}|([0-9]\\d{1,2}){1}){1}");
		
		f = f.replaceAll("MM",fs.get("MM"));
		f = f.replaceAll("dd",fs.get("dd"));
		f = f.replaceAll("HH",fs.get("HH"));
		f = f.replaceAll("kk",fs.get("kk"));
		f = f.replaceAll("KK",fs.get("KK"));
		f = f.replaceAll("hh",fs.get("hh"));
		f = f.replaceAll("mm",fs.get("mm"));
		f = f.replaceAll("ss",fs.get("ss"));
		f = f.replaceAll("SSS",fs.get("SSS"));
		f = f.replaceAll("SS",fs.get("SS"));
		f = f.replaceAll("(y|M|d|H|k|K|h|m|s|S)","[0-9]{1}");
		f = f.replaceAll("a",fs.get("a"));
		
		Pattern p = Pattern.compile("^" + f + "$");
		Matcher m = p.matcher(source);
		return m.matches();
	}
	
	public String left(int len){
		if(blank()){
			return "";
		}
		int sourceLen = source.length();
        if(source.length() <= len){
			return source.substring(0,sourceLen);
		}else{
			return source.substring(0,len);
		}
	}
	
	public String right(int len){
		if(blank()){
			return "";
		}
		int sourceLen = source.length();
        if(source.length() <= len){
			return source;
		}else{
			return source.substring(sourceLen - len,sourceLen);
		}
	}

}
