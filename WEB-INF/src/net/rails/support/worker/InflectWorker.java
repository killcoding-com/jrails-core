package net.rails.support.worker;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rails.support.Support;

public final class InflectWorker {
	
	private final List<String> UNCOUNTABLE = Arrays.asList(new String[]{
			"equipment","information","rice","money","species","series","fish","sheep","jeans"			
	});	
	
	private Pattern p = null;
	private Matcher m = null;
	
	private final String[][] PLURAL = {{"$","s"},
							            {"s$","s"},
							            {"(ax|test)is$","es"},
							            {"(octop|vir)us$","i"},
							            {"(alias|status)$","es"},
							            {"(bu)s$","ses"},
							            {"(buffal|tomat)o$","oes"},
							            {"([ti])um$","a"},
							            {"sis$","ses"},
							            {"([^f])fe$","ves"},
							            {"([lr])f$","ves"},
							            {"(hive)$","s"},
							            {"([^aeiouy]|qu)y$","ies"},
							            {"(x|ch|ss|sh)$","es"},
							            {"(matr|vert|ind)(?:ix|ex)$","ices"},
							            {"([m|l])ouse$","ice"},
							            {"^(ox)$", "en"},
							            {"(quiz)$","zes"},
							            {"^person$","people"},
							            {"^man$","men"},
							            {"^child$","children"},
							            {"^sex$","sexes"},
							            {"^move$","moves"},
							            {"^cow$","kine"}
							           };
	
	private final String[][] SINGULAR = {{"s$",""},
										  {"(n)ews$","ews"},
										  {"([ti])a$","um"},
										  {"((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$","sis"},
										  {"(^analy)ses$","sis"},
										  {"([^f])ves$","fe"},
										  {"(hive)s$",""},
										  {"([lr])ves$", "f"},
										  {"([^aeiouy]|qu)ies$","y"},
										  {"(s)eries$","eries"},
										  {"(m)ovies$","ovie"},
										  {"(x|ch|ss|sh)es$",""},
										  {"([m|l])ice$","1ouse"},
										  {"(bus)es$",""},
										  {"(o)es$",""},
										  {"(shoe)s$",""},
										  {"(cris|ax|test)es$","is"},
										  {"(octop|vir)i$","us"},
										  {"(alias|status)es$",""},
										  {"^(ox)en",""},
										  {"(vert|ind)ices$","ex"},
										  {"(matr)ices$","ix"},
										  {"(quiz)zes$",""},
										  {"(database)s$",""},
								          {"^people$","person"},
								          {"^men$","man"},
								          {"^children$","child"},
								          {"^sexes$","sex"},
								          {"^moves$","move"},
								          {"^kine$","cow"}
										};	


	private String source;
	
	public InflectWorker(String target){
		super();
		this.source = target;
	}

	public String pluralize(){
		if(Support.string(source).blank())
			return "";
		if(UNCOUNTABLE.contains(source))
			return source;
		
		int i = 0;
		int ci = 0;
		for(String[] plural : PLURAL){
			p = Pattern.compile(plural[0]);
			m = p.matcher(source);
			if(m.find()){
				ci = i;
			}
			i++;
		}
		return source.replaceFirst(lasts(PLURAL[ci][0]),PLURAL[ci][1]);
	}
	
	public String singularize(){	
		if(Support.string(source).blank())
			return source;
		
		if(UNCOUNTABLE.contains(source))
			return source;
		
		int i = 0;
		int ci = 0;
		Pattern p = null;
		Matcher m = null;
		for(String[] plural : SINGULAR){
			p = Pattern.compile(plural[0]);
			m = p.matcher(source);
			if(m.find()){
				ci = i;
			}
			i++;
		}
		return source.replaceFirst(lasts(SINGULAR[ci][0]),SINGULAR[ci][1]);
	}	
	
	public String underscore(){
		if(Support.string(source).blank())
			return source;		
		
		String s = "";
		p = Pattern.compile("([A-Z]+(?=[A-Z]{1}[a-z]{1}))|([A-Z]+[a-z]*)|([a-z]+)|([0-9]+)|([_]+)");
		m = p.matcher(source);
		while(m.find()){
			s += "_" + m.group();
		}
		return s.trim().toLowerCase().replaceAll("([_]+(?=[_]{1}))|(^[_]{1})","");
	}
	
	public String camelcase(){
		if(Support.string(source).blank())
			return source;		
		
		String s = "";
		p = Pattern.compile("([A-Z]+(?=[A-Z]{1}[a-z]{1}))|([A-Z]+[a-z]*)|([a-z]+)|([0-9]+)|([_]{2,})");
		m = p.matcher(source);
		while(m.find()){
			s +=  Support.string(m.group()).firstUpCase();
		}
		return s.trim().replaceAll("[_]+(?=[_]{1})","");
	}
	
	public String pascalcase(){
		String s = camelcase();
		return (s.charAt(0) + "").toLowerCase() + s.substring(1);
	}
	
	public String titlecase(){
		if(Support.string(source).blank())
			return source;
		
		source = underscore();
		StringBuffer sbf = new StringBuffer(source);
		p = Pattern.compile("_[a-zA-Z0-9]{1}");
		m = p.matcher(source);
		while(m.find()){
			sbf.delete(m.start(),m.end());
			sbf.insert(m.start(),m.group().replaceAll("_"," ").toUpperCase());
		}
		return (sbf.substring(0,1).toUpperCase() + sbf.substring(1)).trim();
	}
	
	private String lasts(String regex){
		p = Pattern.compile("([^\\)]+)$");
		m = p.matcher(regex);
		if(m.find())
		  return m.group();
		else
		  return null;
	}
	
	public static String underscore(String src){
		return new InflectWorker(src).underscore();
	}
	
	public static String pascalcase(String src){
		return new InflectWorker(src).pascalcase();
	}
	
	public static String camelcase(String src){
		return new InflectWorker(src).camelcase();
	}
	
	public static String titlecase(String src){
		return new InflectWorker(src).titlecase();
	}
	
	public static String pluralize(String src){
		return new InflectWorker(src).pluralize();
	}
	
	public static String singularize(String src){
		return new InflectWorker(src).singularize();
	}
	
}
