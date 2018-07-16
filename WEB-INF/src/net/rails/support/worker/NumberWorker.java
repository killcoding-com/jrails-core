package net.rails.support.worker;

import java.text.DecimalFormat;
import java.text.ParseException;

public final class NumberWorker {
	
	private Number source;
	
	public NumberWorker(Number source) {
		super();
		this.source = source;
	}
	
	public NumberWorker(String pattern,String str) throws ParseException {
		super();
		DecimalFormat df = new DecimalFormat(pattern);
		this.source = df.parse(str);
	}
	
	public String format(String pattern){		
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(source);
	}
	
	public Number def(Number def){
		if(source == null)
			return def;
		
		return source;
	}
	
	public Number getSource(){
		return this.source;
	}

}
