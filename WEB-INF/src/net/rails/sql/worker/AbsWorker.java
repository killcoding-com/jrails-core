package net.rails.sql.worker;

import java.util.HashMap;
import java.util.Map;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.support.Support;
import net.rails.support.worker.AbsConfigWorker;

public abstract class AbsWorker{
	
	protected final static AbsConfigWorker models = Support.config().getModels();
	protected final Map<String,Object> params = new HashMap<String, Object>();
	protected ActiveRecord record;
	protected Adapter adapter;
	
	public abstract String getSql();
	
	public AbsWorker(ActiveRecord record){
		super();
		this.record = record;
		params.putAll(record);
	}
	
	public AbsWorker(ActiveRecord record,Map<String,Object> params){
		super();
		this.record = record;
		this.params.putAll(params);
	}
	
	/**
	 * 获取参数
	 * @return
	 */
	public Map<String,Object> params(){
		return params;
	}
	
	/**
	 * 获取适配器。
	 * @return
	 */
	public Adapter getAdapter(){
		return adapter;
	}
	
	/**
	 * 获取ActiveRecord
	 * @return
	 */
	public ActiveRecord getRecord(){
		return record;
	}
	
}
