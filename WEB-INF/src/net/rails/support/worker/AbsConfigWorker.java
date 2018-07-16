package net.rails.support.worker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ho.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.Define;
import net.rails.support.Support;

@SuppressWarnings("unchecked")
public abstract class AbsConfigWorker {
	
	public abstract <K,V> Map<String,Map<K,V>> getConfs();
	protected abstract String getResource();
	protected Logger log;
	
	static{
		initConfigPath();
	}
	
	public AbsConfigWorker(){
		super();
		log = LoggerFactory.getLogger(getClass());
	}

	public <K,V> Map<K, V> get(String key) {				
		return (Map<K, V>) getConfs().get(key);
	}
	
	public String getFolder() {	
		String path = null;
		try {
			path = new File(Define.CONFIG_PATH + "/" + getResource()).getAbsolutePath();
			return URLDecoder.decode(path,System.getProperty("file.encoding"));
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return path;
		}
	}
	
	public File[] getYmls() throws UnsupportedEncodingException {
		File dir = new File(getFolder());
		return dir.listFiles(getFilter());
	}	
	
	public <K,V> Map<String, Map<K,V>> loadYmls(){
		Map<String, Map<K,V>> confs = new HashMap<String, Map<K,V>>();
			try {
				File[] files = getYmls();
				if(files != null){					
					for(File file : files){
						try{
							Map<K,V> map = (Map<K,V>)Yaml.load(FileUtils.readFileToString(file,Define.CONFIG_FILE_CHARSET));
							confs.put(file.getName().replaceFirst("(.[yY][mM][lL])$",""),map);
						}catch(Exception e){
							log.error("(File: "+ file.getName() +")" + e.getMessage(),e);
						}
					}
				}
				return confs;
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(),e);
				return null;
			}
	}
	
	public <V> V getValues(String file,String...keys){
		Map<Object,Object> vs = get(file);
		if(keys.length == 0)
			return (V)vs;
		
		int len = keys.length;
		
		for(int i = 0;i < len - 1;i++){
			String key = keys[i];
			if(vs == null)
				return null;
			else
				vs = (Map<Object,Object>)vs.get(key);
		}
		if(vs == null)
			return null;
		else
			return (V)vs.get(keys[len - 1]);
	}
	
	public <V> V gets(String...keyarr){
		return Support.map(getConfs()).gets(keyarr);
	}
	
	public <V> V gets(String keys){
		return Support.map(getConfs()).gets(keys);
	}
	
	private FilenameFilter getFilter(){
		return new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".yml");
			}
		};
	}
	
	private static void initConfigPath() {	
		URL url = null;
		String path = null;
		try {
			if(Define.CONFIG_PATH == null){
			    url = Thread.currentThread().getContextClassLoader().getResource("config/");
				if(url != null){
					path = url.getFile();	
				}else{
					path = new File(System.getProperty("user.dir") + "/config/").getAbsolutePath();
				}
				Define.CONFIG_PATH = URLDecoder.decode(path,System.getProperty("file.encoding"));
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(AbsConfigWorker.class).error(e.getMessage(),e);;
		}
	}

}

