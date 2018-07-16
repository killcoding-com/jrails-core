package net.rails.ext;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import net.rails.support.Support;
import net.rails.support.worker.ConfigWorker;

public abstract class AbsGlobal {
	
	public abstract void setUserId(Object userId);
	public abstract Object getUserId();
	public abstract void setSessionId(Object sessionId);
	public abstract Object getSessionId();	
	public abstract String getRealPath();

	public final Map<String,Object> options = new HashMap<String,Object>();	
	protected String locale;
	protected ConfigWorker config;
	
	public AbsGlobal(){
		super();
		locale = Support.config().env().getString("locale","default");
		config = Support.config();
	}
	
	public void setLocale(String locale){
		this.locale = locale;
	}
	
	public String getLocale(){
		return locale;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T locale(String...keys){
		if(keys == null)
			return (T)config.getLocales().getValues(locale);
		else if (keys.length == 1){			
			return (T)config.getLocales().getValues(locale,keys[0].split("[,|\\.]"));
		}else{
			return (T)config.getLocales().getValues(locale,keys);
		}
	}
	
//	@SuppressWarnings("unchecked")
//	public <T extends Object> T locale2(String[] keys){
//		if(keys == null)
//			return (T)config.getLocales().getValues(locale);
//		else if (keys.length == 1){			
//			return (T)config.getLocales().getValues(locale,keys[0].split("[,|\\.]"));
//		}else{
//			return (T)config.getLocales().getValues(locale,keys);
//		}
//	}
	
	public String t(String...keys){
		String v = (String)locale(keys);
		return v;
	}
	
	public String tf(String[] keys,Object[] params){
		String s = t(keys);
		return s == null ? null : MessageFormat.format(s,params);
	}
	
	public String a(String model,String attribute){
		return t("attributes",model,attribute);
	}
	
	public String a(String modelAttr){
		return t("attributes," + modelAttr);
	}
	
	public String m(String model){
		return t("models",model);
	}
	
	public Map<String,Object> getOptions(){
		return options;
	}
	
	public String datetime2text(Timestamp timestamp){
		return timestamp2text(timestamp);
	}
	
	public Timestamp text2datetime(String text) throws ParseException{
		return text2timestamp(text);
	}
	
	public String timestamp2text(Timestamp timestamp){
		if(timestamp == null)
			return null;
		
		SimpleDateFormat f = new SimpleDateFormat(t("formats","datetime"));
		return f.format(timestamp);
	}
	
	public Timestamp text2timestamp(String text) throws ParseException{
		if(text == null)
			return null;
		
		SimpleDateFormat f = new SimpleDateFormat(t("formats","datetime"));
		return new Timestamp(f.parse(text).getTime());
	}
	
	public String date2text(Date date){
		if(date == null)
			return null;
		
		SimpleDateFormat f = new SimpleDateFormat(t("formats","date"));
		return f.format(date);
	}
	
	public Date text2data(String text) throws ParseException{
		if(text == null)
			return null;
		
		SimpleDateFormat f = new SimpleDateFormat(t("formats","date"));
		return new Date(f.parse(text).getTime());
	}
	
	public String time2text(Time time){
		if(time == null)
			return null;
		
		SimpleDateFormat f = new SimpleDateFormat(t("formats","time"));
		return f.format(time);
	}
	
	public Time text2time(String text) throws ParseException{
		if(text == null)
			return null;
		
		SimpleDateFormat f = new SimpleDateFormat(t("formats","time"));
		return new Time(f.parse(text).getTime());
	}
	
	public Map<String,Object> currentLocale(){
		return Support.config().getLocales().get(locale);
	}
	
	public static String getServerCharset(){
		return Support.config().env().getServerCharset();
	}
	
	public static String getApplicationCharset(){
		return Support.config().env().getApplicationCharset();
	}
	
}
