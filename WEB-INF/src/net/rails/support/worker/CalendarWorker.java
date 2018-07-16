package net.rails.support.worker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class CalendarWorker {

	private Calendar source;
	
	public CalendarWorker() {
		super();
		source = Calendar.getInstance();
		source.setTimeInMillis(new Date().getTime());
	}
	
	public CalendarWorker(long millis) {
		super();
		source = Calendar.getInstance();
		source.setTimeInMillis(millis);
	}
	
	public CalendarWorker(String pattern,String str) throws ParseException {
		super();
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		source.setTimeInMillis(sdf.parse(str).getTime());		
	}
	
	public String format(String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(source.getTime());
	}
	
	public Calendar getSource(){
		return source;
	}

}
