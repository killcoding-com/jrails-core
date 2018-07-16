package net.rails.sql.worker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.sql.Sql;

/**
 * DELETE语句生成类
 * @author Jack
 *
 */
public final class DestroyWorker extends AbsWorker implements Cloneable{
	
	private final List<String> wheres = new ArrayList<String>();
	
	public DestroyWorker(ActiveRecord record) {
		super(record);
		this.adapter = record.getWriterAdapter();
	}
	
	public DestroyWorker(ActiveRecord record,Map<String,Object> params) {
		super(record,params);
		this.adapter = record.getWriterAdapter();
	}
	
	@Override
    public DestroyWorker clone() throws CloneNotSupportedException {
		DestroyWorker o = (DestroyWorker) super.clone();
		DestroyWorker c = new DestroyWorker(o.record);
		c.params().putAll(o.params);
		c.wheres.addAll(o.wheres);
		return c;
	}
	
	public List<String> wheres(){
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
	
	/**
	 * 执行此次删除语句。
	 * @return 受影响行数
	 * @throws SQLException
	 */
	public int execute() throws SQLException{
		return adapter.execute(Sql.sql(getSql(),params));
	}
	
	/**
	 * 获取DELETE语句。
	 */
	@Override
	public String getSql() {
		final StringBuffer where = createWheres();
		if (!where.toString().equals("")) {
			where.insert(0, " WHERE ");
		}
		return String.format("DELETE FROM %s%s",adapter.quoteSchemaAndTableName(), where);
	}

}
