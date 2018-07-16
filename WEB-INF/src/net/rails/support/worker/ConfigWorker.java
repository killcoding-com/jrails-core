package net.rails.support.worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.support.Support;

public final class ConfigWorker {

	public static Map<String, Map<Object, Object>> CONFS = null;
	public static Map<String, Map<Object, Object>> MODEL_CONFS = null;
	public static Map<String, Map<Object, Object>> LOCALE_CONFS = null;
	public static Map<String, List<Map<String, Object>>> DELETE_CONFS = null;
	public static Map<String, List<Map<String, Object>>> DESTROY_CONFS = null;

	public ConfigWorker(){
		super();
	}
	
	public AbsConfigWorker getConfig() {
		AbsConfigWorker c = new AbsConfigWorker() {
			@Override
			public Map<String, Map<Object, Object>> getConfs() {
				if (CONFS == null) {
					CONFS = this.loadYmls();
					log.debug("{}",CONFS);
				}
				return CONFS;
			}

			@Override
			protected String getResource() {
				return "/";
			}
		};
		return c;
	}

	public EnvWorker env() {
		return new EnvWorker();
	}

	public AbsConfigWorker getModels() {
		AbsConfigWorker c = new AbsConfigWorker() {
			@Override
			public synchronized Map<String, Map<Object, Object>> getConfs() {
				if (MODEL_CONFS == null) {
					MODEL_CONFS = this.loadYmls();
				}
				return MODEL_CONFS;
			}

			@Override
			protected String getResource() {
				return "/models/";
			}
		};
		return c;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, List<Map<String, Object>>> getDelete() {
		if (DELETE_CONFS == null) {
			DELETE_CONFS = new HashMap<String, List<Map<String, Object>>>();
			Map<String, Map<String, Object>> cnfs = getModels().getConfs();
			List<String> models = Support.map(cnfs).keys();
			for (String model : models) {
				List<Map<String, Object>> depcnf = (List<Map<String, Object>>) cnfs.get(model).get("delete");
				if (depcnf != null) {
					DELETE_CONFS.put(model, depcnf);
				}
			}
		}
		return DELETE_CONFS;
	}
	
	public Map<String, List<Map<String, Object>>> getDestroy() {
		if (DESTROY_CONFS == null) {
			DESTROY_CONFS = new HashMap<String, List<Map<String, Object>>>();
			Map<String, Map<String, Object>> cnfs = getModels().getConfs();
			List<String> models = Support.map(cnfs).keys();
			for (String model : models) {
				List<Map<String, Object>> depcnf = (List<Map<String, Object>>) cnfs.get(model).get("destroy");
				if (depcnf != null) {
					DESTROY_CONFS.put(model, depcnf);
				}
			}
		}
		return DESTROY_CONFS;
	}

	public AbsConfigWorker getLocales() {
		AbsConfigWorker c = new AbsConfigWorker() {
			@Override
			public synchronized Map<String, Map<Object, Object>> getConfs() {
				if (LOCALE_CONFS == null) {
					LOCALE_CONFS = this.loadYmls();
				}
				return LOCALE_CONFS;
			}

			@Override
			protected String getResource() {
				return "/locales/";
			}
		};
		return c;
	}
}

