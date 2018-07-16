package net.rails.log;

import java.util.Properties;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import net.rails.log.LogPoint;

public final class LogFilter extends Filter {

	private final Properties properties = LogPoint.getConfigure();
	
	@Override
	public int decide(LoggingEvent event) {
		int decide = Filter.DENY;
		String level = event.getLevel().toString();
		boolean use = level.equals("INFO") || level.equals("DEBUG");
		if(use){
			boolean isMarkJobSystem = Boolean.parseBoolean(properties.getProperty(LogPoint.JOB_SYSTEM));
			boolean isMarkJobApp = Boolean.parseBoolean(properties.getProperty(LogPoint.JOB_APP));
     		String tn = event.getThreadName();
			boolean isRunningJobSystem = tn.startsWith("SYSTEM_SCHEDULER-");
			boolean isRunningJobApp = tn.startsWith("APP_SCHEDULE-");

			if(isRunningJobSystem || isRunningJobApp){
				if(isMarkJobSystem && isRunningJobSystem){
					decide = Filter.NEUTRAL;
				}
				if(isMarkJobApp && isRunningJobApp){
					decide = Filter.NEUTRAL;
				}
				if(decide == Filter.NEUTRAL){
					decide = decideMarks(event);
				}
			}else{
				decide = decideMarks(event);
			}
			return decide;
		}
		return Filter.NEUTRAL;
	}
	
	private int decideMarks(LoggingEvent event){
		int decide = Filter.DENY;
		boolean isMarkYour = Boolean.parseBoolean(properties.getProperty(LogPoint.YOUR));
		boolean isMarkSql = Boolean.parseBoolean(properties.getProperty(LogPoint.SQL));
		boolean isMarkSqlConn = Boolean.parseBoolean(properties.getProperty(LogPoint.SQL_CONN));
		boolean isMarkSqlCache = Boolean.parseBoolean(properties.getProperty(LogPoint.SQL_CACHE));
		boolean isMarkSqlRows = Boolean.parseBoolean(properties.getProperty(LogPoint.SQL_ROWS));
		boolean isMarkSqlResult = Boolean.parseBoolean(properties.getProperty(LogPoint.SQL_RESULT));
		boolean isMarkWebUrl = Boolean.parseBoolean(properties.getProperty(LogPoint.WEB_URL));
		boolean isMarkWebParams = Boolean.parseBoolean(properties.getProperty(LogPoint.WEB_PARAMS));
		boolean isMarkWebHeader = Boolean.parseBoolean(properties.getProperty(LogPoint.WEB_HEADER));
		boolean isMarkWebUserAgent = Boolean.parseBoolean(properties.getProperty(LogPoint.WEB_USERAGENT));
		boolean isMarkWebCookie = Boolean.parseBoolean(properties.getProperty(LogPoint.WEB_COOKIE));
		
		if(isMarkYour && LogPoint.isMarkYour()) {
			decide = Filter.NEUTRAL;
		} 
		if(isMarkSql && LogPoint.isMarkSql()) {
			decide = Filter.NEUTRAL;
		} 
		if(isMarkSqlConn && LogPoint.isMarkSqlConn()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkSqlCache && LogPoint.isMarkSqlCache()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkSqlRows && LogPoint.isMarkSqlRows()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkSqlResult && LogPoint.isMarkSqlResult()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkWebUrl && LogPoint.isMarkWebUrl()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkWebParams && LogPoint.isMarkWebParams()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkWebHeader && LogPoint.isMarkWebHeader()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkWebUserAgent && LogPoint.isMarkWebUserAgent()){
			decide = Filter.NEUTRAL;
		}
		if(isMarkWebCookie && LogPoint.isMarkWebCookie()){
			decide = Filter.NEUTRAL;
		}
		return decide;
	}

}
