package net.rails.sql.worker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.sql.Sql;
import net.rails.support.Support;

public class UpdateWorker extends AbsWorker implements Cloneable{	
	
	private final List<String> wheres = new ArrayList<String>();
	
	public UpdateWorker(ActiveRecord record) {
		super(record);
		this.adapter = record.getWriterAdapter();
	}
	
	public UpdateWorker(ActiveRecord record,Map<String,Object> params) {
		super(record,params);
		this.adapter = record.getWriterAdapter();
	}
	
	@Override
    public UpdateWorker clone() throws CloneNotSupportedException {
		UpdateWorker o = (UpdateWorker)super.clone();
		UpdateWorker c = new UpdateWorker(o.record);
		c.params().putAll(params);
		c.wheres().clear();
		c.wheres().addAll(wheres);
		return c;
	}

	public List<String> wheres() {
		return wheres;
	}

	public StringBuffer createWheres() {
		final StringBuffer sbf = new StringBuffer();
		wheres.remove("");
		int size = wheres.size();
		for (int i = 0; i < size; i++) {
			sbf.append(wheres.get(i));
			if (i < size - 1)
				sbf.append(" ");
		}
		return sbf;
	}
	
	public int execute() throws SQLException{
		return adapter.execute(Sql.sql(getSql(),params));
	}

	@Override
	public String getSql() {
		StringBuffer sql = new StringBuffer();
		List<String> columns = Support.map(params).keys();
		sql.append("UPDATE ");
		sql.append(adapter.quoteSchemaAndTableName());
		sql.append(" SET ");
		List<String> atts = new ArrayList<String>();
		for(String column : columns){
			if (!adapter.getPrimaryKey().equals(column) && adapter.getColumnNames().contains(column))
				atts.add(column);
		}	
		for (int i = 0; i < atts.size(); i++) {
			String column = atts.get(i);
			if (adapter.getColumnNames().contains(column)) {
				this.params.put(column, Support.object(params.get(column)).def(null));
				sql.append(adapter.quote(column));
				sql.append(" = ");
				sql.append(":" + column);
				if (i < atts.size() - 1 ) {
					sql.append(",");
				}
			}
		}
		final StringBuffer where = createWheres();
		if (!where.toString().equals("")) {
			where.insert(0, " WHERE ");
		}
		return sql.toString() + where.toString();
	}
	
}
