package net.rails.tpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

import net.rails.Define;
import net.rails.ext.AbsGlobal;
import net.rails.ext.IndexMap;
import net.rails.support.Support;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TplText {

	private final static Logger log = LoggerFactory.getLogger(TplText.class);
	private AbsGlobal g;
	private String name;
	private StringBuffer text;
	private final Map<String,Object> params = new IndexMap<String,Object>();
	
	static{
		initViewPath();
	}
	
	public TplText(String name,AbsGlobal g,StringBuffer text) {
		super();
		this.g = g;
		this.name = name;
		this.text = text;
	}
	
	public TplText(String name,AbsGlobal g,String tplFile) throws IOException {
		super();
		this.g = g;
		this.name = name;
		String s = FileUtils.readFileToString(new File(Define.VIEW_PATH + "/" + tplFile),Support.env().getApplicationCharset());
		this.text = new StringBuffer(s);
	}
	
	public TplText(String name,AbsGlobal g,StringBuffer text,Map<String,Object> params) {
		super();
		this.g = g;
		this.name = name;
		this.text = text;
		if(params != null)
			this.params.putAll(params);
	}
	
	public TplText(String name,AbsGlobal g,String tplFile,Map<String,Object> params) throws IOException {
		super();
		this.g = g;
		this.name = name;
		String s = FileUtils.readFileToString(new File(tplFile),Support.env().getApplicationCharset());
		this.text = new StringBuffer(s);
		if(params != null)
			this.params.putAll(params);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setText(StringBuffer text){
		this.text = text;
	}

	public StringBuffer getText() {
		return text;
	}
	
	public Map<String,Object> params(){
		return params;
	}
	
	private static void initViewPath() {	
		URL url = null;
		String path = null;
		try {
			if(Define.VIEW_PATH == null){
			    url = Thread.currentThread().getContextClassLoader().getResource("view/");
			    if(url != null){
					path = url.getFile();	
				}else{
					path = new File(System.getProperty("user.dir") + "/view/").getAbsolutePath();
				}
				Define.VIEW_PATH = URLDecoder.decode(path,System.getProperty("file.encoding"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

}
