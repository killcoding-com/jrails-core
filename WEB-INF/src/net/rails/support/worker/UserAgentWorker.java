package net.rails.support.worker;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rails.support.Support;

public final class UserAgentWorker {
	
	public final static List<String> Mobiles = Arrays.asList(Families.Android,Families.Windows_Phone,Families.iPhone,Families.Symbian);
	
	public static class Families {
		public final static String Windows = "Windows";
		public final static String Windows_Phone = "Windows Phone";
		public final static String Android = "Android";
		public final static String iPhone = "iPhone";
		public final static String Mac = "Mac";
		public final static String Symbian = "Symbian";
		public final static String Linux = "Linux"; 
	}
	
	public static class Engines {
		public final static String Trident = "Trident";
		public final static String WebKit = "WebKit";
		public final static String Gecko = "Gecko";
		public final static String Presto = "Presto";
	}
	
	public static class Versions {
		public final static String IE = "MSIE";
		public final static String Chrome = "Chrome";
		public final static String Firefox = "Firefox";
		public final static String Opera = "Opera";
		public final static String Safari = "Safari";
		public final static String UCBrowser = "UCBrowser";
	}
	
	private String ua;
	private TokenWorker os;
	private TokenWorker engine;
	private TokenWorker browser;
	
	public UserAgentWorker(String ua){
		super();
		this.ua = ua;
		for(UAToken ut :UAToken.values()){
			if(Support.string(this.ua).blank())
				break;
			
			 String[] formats = null;
			 if(ut.name().equals("OS"))
				 formats = UAToken.OS.formats();
			 else if(ut.name().equals("Engine"))
				 formats = UAToken.Engine.formats();
			 else if(ut.name().equals("Browser"))
				 formats = UAToken.Browser.formats();
			 
			 TokenWorker token = new TokenWorker();
			 for(int i = 0;i < formats.length;i++){
					 String frm = formats[i];
					 String[] fs = frm.split(" # ");
					 String c = fs[0].trim();
					 String f = fs[1].trim();
					 String[] cs = c.split("\\.");
					 String ca = cs[0]; //Category Name
					 String key = cs[1]; //group,name,version
					 Pattern p = Pattern.compile(f);
					 Matcher m = p.matcher(this.ua);					 
					 if(m.find()){
						 token.setFamily(ca);
						 String g = m.group();
						 if(g != null){							 
							 if(key.equals("group"))
								 token.setGroup(g);
							 else if(key.equals("name"))
								 token.setName(g);
							 else if(key.equals("version")){
								 token.setVersion(Float.parseFloat(g.replaceFirst("_", ".") + "F"));
							 }
						 }
						 else
							continue; 				 
					 }
			 }
			 if(ut.name().equals("OS"))
				 os = token.clone();
			 else if(ut.name().equals("Engine"))
				 engine = token.clone();			 
			 else if(ut.name().equals("Browser"))
				 browser = token.clone();

		}
	}
	
	public boolean isMobile(){
		return Mobiles.contains(os.getFamily());
	}
	
	public TokenWorker os(){
		return os;
	}
	
	public TokenWorker engine(){
		return engine;
	}
	
	public TokenWorker browser(){
		return browser;
	}
		
	public String ua(){
		return ua;
	}
}

enum UAToken {

	OS(
			"Windows.group # Windows (XP|NT|CE|ME|2000|98|95|8|7)(?=( \\d+[_.]{0,1}\\d+){0,1})",
			"Windows.name # Windows (XP|NT|CE|ME|2000|98|95|8|7)( \\d+[_.]{0,1}\\d+){0,1}",
			"Windows.version # (?<=(Windows (Phone|XP|NT|CE|ME|2000|98|95|8|7) ))(\\d+[_.]{0,1}\\d+){0,1}",
			"Windows Phone.group # Windows Phone(?=( \\d+[_.]{0,1}\\d+))",
			"Windows Phone.name # Windows Phone( \\d+[_.]{0,1}\\d+){0,1}",
			"Windows Phone.version # (?<=(Windows Phone ))(\\d+[_.]{0,1}\\d+)",			
			"Linux.group # Linux x86(?=_64)",
			"Linux.name # Linux x86(_64)",
			"Linux.group # Linux x86(?=_32)",
			"Linux.name # Linux x86(_32)",
			"Linux.group # Linux i(?=(586|686))",
			"Linux.name # Linux i(586|686)",
			"Linux.version # (?<=(Linux i))(586|686)",			
			"Android.group # Android(?=([ ]\\d+[_.]{0,1}\\d+))",
			"Android.name # Android([ ]\\d+[_.]{0,1}\\d+)",
			"Android.version # (?<=(Android ))(\\d+[_.]{0,1}\\d+)",
			"iPhone.group # iPhone OS(?=([ ]\\d+[_.]{0,1}\\d+))",
			"iPhone.name # iPhone OS([ ]\\d+[_.]{0,1}\\d+)",
			"iPhone.version # (?<=(iPh OS ))(\\d+[_.]{0,1}\\d+)",
			"iPhone.group # iPh OS(?=([ ]\\d+[_.]{0,1}\\d+))",
			"iPhone.name # iPh OS([ ]\\d+[_.]{0,1}\\d+)",
			"iPhone.version # (?<=(iPhone OS ))(\\d+[_.]{0,1}\\d+)",
			"Mac.group # Mac OS X(?=([ ]\\d+[_.]{0,1}\\d+))",
			"Mac.name # Mac OS X([ ]\\d+[_.]{0,1}\\d+)",
			"Mac.version # (?<=(Mac OS X ))(\\d+[_.]{0,1}\\d+)",
			"Symbian.group # SymbianOS(?=(/\\d+[_.]{0,1}\\d+))",
			"Symbian.name # SymbianOS(/\\d+[_.]{0,1}\\d+)",
			"Symbian.version # (?<=(SymbianOS/))(\\d+[_.]{0,1}\\d+)"
			),
	Engine(
			"Trident.group # Trident(?=(/\\d+[_.]{0,1}\\d+))",
			"Trident.name # Trident/\\d+[_.]{0,1}\\d+",
			"Trident.version # (?<=Trident/)\\d+[_.]{0,1}\\d+",
			"WebKit.group # AppleWeb[Kk]it(?=(/\\d+[_.]{0,1}\\d))",
			"WebKit.name # AppleWeb[Kk]it(/\\d+[_.]{0,1}\\d)",
			"WebKit.version # (?<=AppleWeb[Kk]it/)\\d+[_.]{0,1}\\d+",
			"Gecko.group # Gecko(?=(/\\d+[_.]{0,1}\\d))",
			"Gecko.name # Gecko(/\\d+[_.]{0,1}\\d)",
			"Gecko.version # (?<=Gecko/)\\d+[_.]{0,1}\\d+",
			"Presto.group # Presto(?=(/\\d+[_.]{0,1}\\d))",
			"Presto.name # Presto(/\\d+[_.]{0,1}\\d)",
			"Presto.version # (?<=Presto/)\\d+[_.]{0,1}\\d+"
			),
		Browser(
			"IE.group # MSIE(?=( \\d+[_.]{0,1}\\d+))",
			"IE.name # MSIE \\d+[_.]{0,1}\\d+",
			"IE.version # (?<=MSIE )\\d+[_.]{0,1}\\d+",			
			"Firefox.group # Firefox(?=(/\\d+[_.]{0,1}\\d+))",
			"Firefox.name # Firefox/\\d+[_.]{0,1}\\d+",
			"Firefox.version # (?<=Firefox/)\\d+[_.]{0,1}\\d+",
			"Safari.group # Safari(?=(/\\d+[_.]{0,1}\\d+))",
			"Safari.name # Safari/\\d+[_.]{0,1}\\d+",
			"Safari.version # (?<=Safari/)\\d+[_.]{0,1}\\d+",
			"Chrome.group # Chrome(?=(/\\d+[_.]{0,1}\\d+))",
			"Chrome.name # Chrome/\\d+[_.]{0,1}\\d+",
			"Chrome.version # (?<=Chrome/)\\d+[_.]{0,1}\\d+",
			"Opera.group # Opera(?=([/ ]\\d+[_.]{0,1}\\d+))",
			"Opera.name # Opera[/ ]\\d+[_.]{0,1}\\d+",
			"Opera.version # (?<=Opera[/ ])\\d+[_.]{0,1}\\d+",
			"UCBrowser.group # UCBrowser(?=([/ ]\\d+[_.]{0,1}\\d+))",
			"UCBrowser.name # UCBrowser[/ ]\\d+[_.]{0,1}\\d+",
			"UCBrowser.version # (?<=UCBrowser[/ ])\\d+[_.]{0,1}\\d+"
		 );

	
	private String[] formats;

	public String[] formats() {
		return formats;
	}

	private UAToken(String... formats) {
		this.formats = formats;
	}

}


