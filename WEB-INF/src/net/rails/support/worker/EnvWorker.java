package net.rails.support.worker;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.support.Support;
import java.io.File;
import org.apache.commons.io.FileUtils;

@SuppressWarnings("unchecked")
public final class EnvWorker {

	private Logger log = LoggerFactory.getLogger(EnvWorker.class);

	public EnvWorker() {
		super();
	}

	public Map<String, Object> getRoot() {
		return Support.config().getConfig().get("env");
	}

	public Number getNumber(String key, Number def) {
		return Support.number(getNumber(key)).def(def);
	}

	public Number getNumber(String key) {
		return (Number) get(key);
	}

	public String getString(String key, String def) {
		return Support.string(getString(key)).def(def);
	}

	public String getString(String key) {
		return (String) get(key);
	}

	public Boolean getBoolean(String key, Boolean def) {
		return (Boolean) Support.object(getBoolean(key)).def(def);
	}

	public Boolean getBoolean(String key) {
		return (Boolean) get(key);
	}

	public String getApplicationCharset() {
		return getString("application_charset", "UTF-8");
	}

	public String getServerCharset() {
		return getString("server_charset", "ISO-8859-1");
	}

	public String getLocale() {
		return getString("locale", "default");
	}

	public String getEnv() {
		Object value = get("env");
		if (value instanceof Map) {
			Map<String, String> o = (Map<String, String>) value;
			value = o.get(getHostname());
		}
		return (String) value;
	}

	public String getPrefix() {
		return getString("prefix", "");
	}

	public List<Object> getList(String key) {
		return (List<Object>) get(key);
	}

	public Map<String, Object> getMap(String key) {
		return (Map<String, Object>) get(key);
	}

	public <T extends Object> T get(String key) {
		return (T) getRoot().get(key);
	}

	public <T extends Object> T get(String key, T def) {
		T t = (T) getRoot().get(key);
		if (t == null) {
			return def;
		}
		return t;
	}

	public <V> V getOrDefault(String keys, V def) {
		V v = Support.map(getRoot()).gets(keys);
		return v == null ? def : v;
	}

	public <V> V gets(String keys) {
		return Support.map(getRoot()).gets(keys);
	}

	public <V> V gets(String...keyarr) {
		return Support.map(getRoot()).gets(keyarr);
	}

	public boolean isError() {
		return log.isErrorEnabled();
	}

	public String getHostname() {
		String hostname = System.getenv().get("HOSTNAME");
		try{
    		File hostnameFile = new File("/etc/hostname");
    		if(hostnameFile.exists()){
    		    hostname = FileUtils.readFileToString(hostnameFile);
    		    if(hostname != null){
    		        hostname = hostname.trim();
    		    }
    		}
		}catch(Exception e){
		    log.error(e.getMessage(), e);
		}
		if (hostname == null) {
			try {
				InetAddress addr;
				addr = InetAddress.getLocalHost();
				hostname = addr.getHostName();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return hostname;
	}

}
