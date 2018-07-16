package net.rails.log;

import java.util.Properties;
import org.apache.log4j.MDC;

public final class LogPoint {

	private final static Properties properties = new Properties();
	public static String POINT_KEY = "log4j.jrails.point";
	public static String YOUR = "log4j.jrails.your";
	public static String SQL = "log4j.jrails.sql";
	public static String SQL_CONN = "log4j.jrails.sql.conn";
	public static String SQL_CACHE = "log4j.jrails.sql.cache";
	public static String SQL_ROWS = "log4j.jrails.sql.rows";
	public static String SQL_RESULT = "log4j.jrails.sql.result";
	public static String WEB_URL = "log4j.jrails.web.url";
	public static String WEB_PARAMS = "log4j.jrails.web.params";
	public static String WEB_HEADER = "log4j.jrails.web.header";
	public static String WEB_USERAGENT = "log4j.jrails.web.useragent";
	public static String WEB_COOKIE = "log4j.jrails.web.cookie";
	public static String JOB_SYSTEM = "log4j.jrails.job.system";
	public static String JOB_APP = "log4j.jrails.job.app";
	
	public static void configure(Properties all){
		properties.putAll(all);
	}
	
	public static Properties getConfigure(){
		return properties;
	}
	
	private static void mark(String point){
		MDC.put(POINT_KEY, point);
	}
	
	public static void unmark(){
		MDC.remove(POINT_KEY);
	}
	
	public static boolean isMarkYour(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(YOUR);
	}
	
	public static boolean isMarkSql(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(SQL);
	}
	
	public static boolean isMarkSqlConn(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(SQL_CONN);
	}
	
	public static boolean isMarkSqlCache(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(SQL_CACHE);
	}
	
	public static boolean isMarkSqlRows(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(SQL_ROWS);
	}
	
	public static boolean isMarkSqlResult(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(SQL_RESULT);
	}
	
	public static boolean isMarkWebUrl(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(WEB_URL);
	}
	
	public static boolean isMarkWebParams(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(WEB_PARAMS);
	}
	
	public static boolean isMarkWebUserAgent(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(WEB_USERAGENT);
	}
	
	public static boolean isMarkWebHeader(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(WEB_HEADER);
	}
	
	public static boolean isMarkWebCookie(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(WEB_COOKIE);
	}
	
	public static boolean isMarkJobSystem(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(JOB_SYSTEM);
	}
	
	public static boolean isMarkJobApp(){
		String val = (String)MDC.get(POINT_KEY);
		if(val == null){
			return false;
		}
		return val.equals(JOB_APP);
	}
	
	public static void markYour(){
		mark(YOUR);
	}
	
	public static void markSql(){
		mark(SQL);
	}
	
	public static void markSqlConn(){
		mark(SQL_CONN);
	}
	
	public static void markSqlCache(){
		mark(SQL_CACHE);
	}
	
	public static void markSqlRows(){
		mark(SQL_ROWS);
	}
	
	public static void markSqlResult(){
		mark(SQL_RESULT);
	}
	
	public static void markWebUrl(){
		mark(WEB_URL);
	}
	
	public static void markWebParams(){
		mark(WEB_PARAMS);
	}
	
	public static void markWebHeader(){
		mark(WEB_HEADER);
	}
	
	public static void markWebCookie(){
		mark(WEB_COOKIE);
	}
	
	public static void markJobSystem(){
		mark(JOB_SYSTEM);
	}
	
	public static void markJobApp(){
		mark(JOB_APP);
	}

}
