package net.rails.sql.worker;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.validate.TypeException;
import net.rails.support.Support;

/**
 * INSERT SQL语句工人。
 * @author Jack
 *
 */
public final class CreateWorker extends AbsWorker implements Cloneable{
	
	private Logger log = LoggerFactory.getLogger(CreateWorker.class);
	
	/**
	 * 构造方法。
	 * @param record
	 */
	public CreateWorker(ActiveRecord record) {
		super(record);
		this.adapter = record.getWriterAdapter();
	}
	
	/**
	 * 构造方法。
	 * @param record
	 * @param params
	 */
	public CreateWorker(ActiveRecord record,Map<String,Object> params) {
		super(record,params);
		this.adapter = record.getWriterAdapter();
	}
	
	@Override
    public CreateWorker clone() throws CloneNotSupportedException {
		CreateWorker o = (CreateWorker) super.clone();
		CreateWorker c = new CreateWorker(o.record);
		c.adapter = o.adapter;
		c.params().putAll(o.params);
		return c;
	}

	/**
	 * 获取一个INSERT语句。
	 */
	@Override
	public String getSql() {
		List<String> columns = adapter.getColumnNames();
		if(params.get(record.getReaderAdapter().getPrimaryKey()) == null){
			params.remove(record.getReaderAdapter().getPrimaryKey());
		}
		StringBuffer sql = new StringBuffer();
		StringBuffer values = new StringBuffer();
		sql.append("INSERT INTO ");
		sql.append(adapter.quoteSchemaAndTableName());
		sql.append("(");
		for (int i = 0; i < columns.size(); i++) {
			String column = columns.get(i);
			Object defaultValue = null;
			try {	
				defaultValue = record.getAttribute(column).getDefaultValue();
			} catch (TypeException e) {
				log.error(e.getMessage(),e);
			}
			if (Support.object(params.get(column)).blank()) {
				params.put(column,defaultValue);				
			}
			if (params.containsKey(column)) {		
				sql.append(adapter.quote(column));
				values.append(":" + column);
				sql.append(",");
				values.append(",");
			}
		}
		sql = new StringBuffer(sql.toString().replaceFirst(",$",""));
		values = new StringBuffer(values.toString().replaceFirst(",$",""));
		sql.append(") VALUES(");
		sql.append(values.toString());
		sql.append(")");
		return sql.toString();
	}

}
