package net.rails.support;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.rails.ext.AbsGlobal;
import net.rails.ext.Json;
import net.rails.support.job.worker.JobWorker;
import net.rails.support.worker.ArrayWorker;
import net.rails.support.worker.Base64Worker;
import net.rails.support.worker.CalendarWorker;
import net.rails.support.worker.CodeWorker;
import net.rails.support.worker.ConfigWorker;
import net.rails.support.worker.EnvWorker;
import net.rails.support.worker.InflectWorker;
import net.rails.support.worker.MapWorker;
import net.rails.support.worker.NumberWorker;
import net.rails.support.worker.ObjectWorker;
import net.rails.support.worker.StringWorker;
import net.rails.support.worker.UserAgentWorker;
import net.rails.support.worker.ValidateMessageWorker;

public final class Support {

	public Support(){
		super();
	}
	
	public static Json<String,Object> json(){
		return new Json<String,Object>();
	}

	public static CodeWorker code() {
		return new CodeWorker();
	}

	public static InflectWorker inflect(String target) {
		return new InflectWorker(target);
	}
	
	public static StringWorker string(String target) {
		return new StringWorker(target);
	}
	
	public static NumberWorker number(Number target) {
		return new NumberWorker(target);
	}
	
	public static NumberWorker number(String pattern,String source) throws ParseException {
		return new NumberWorker(pattern,source);
	}
	
	public static CalendarWorker calendar() {
		return new CalendarWorker();
	}
	
	public static CalendarWorker calendar(long millis) {
		return new CalendarWorker(millis);
	}
	
	public static CalendarWorker calendar(Date target) {
		return new CalendarWorker(target.getTime());
	}
	
	public static CalendarWorker calendar(String pattern,String source) throws ParseException {
		return new CalendarWorker(pattern,source);
	}
	
	public static <K, V> MapWorker<K,V> map(Map<K,V> target) {
		return new MapWorker<K,V>(target);
	}
	
	public static <T> ArrayWorker<T> array(List<T> target) {
		return new ArrayWorker<T>(target);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <O> ObjectWorker object(O target) {
		return new ObjectWorker(target);
	}

	public static ConfigWorker config(){
		return new ConfigWorker();
	}
	
	public static <K,V> Map<K,V> config(String file,String...keys){
		return new ConfigWorker().getConfig().getValues(file, keys);
	}
	
	public static ValidateMessageWorker validateMessage(AbsGlobal g,String of,String model,String attr){
		return new ValidateMessageWorker(g,of,model,attr);
	}
	
	public static Base64Worker base64(){
		return new Base64Worker();
	}
	
	public static EnvWorker env(){
		return new EnvWorker();
	}
	
	public static UserAgentWorker userAgent(String ua){
		return new UserAgentWorker(ua);
	}
	
	public static JobWorker job(){
		return new JobWorker();
	}

}
