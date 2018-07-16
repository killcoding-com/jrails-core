package net.rails.sql;

import java.util.List;
import java.util.Map;

import net.rails.active_record.ActiveRecord;
import net.rails.sql.query.Query;
import net.rails.sql.worker.CreateWorker;
import net.rails.sql.worker.DestroyWorker;
import net.rails.sql.worker.FindWorker;
import net.rails.sql.worker.HasWorker;
import net.rails.sql.worker.SqlWorker;
import net.rails.sql.worker.UpdateWorker;

/**
 * SQL Base Class
 * @author Jack
 *
 */
public final class Sql {
	
	public static Query query(ActiveRecord from){
		return new Query(from);
	}
	
	public static SqlWorker sql(String sql){
		return new SqlWorker(sql);
	}
	
	public static SqlWorker sql(String sql,List<Object> params){
		return new SqlWorker(sql,params);
	}
	
	public static SqlWorker sql(String sql,Map<String,Object> params){
		return new SqlWorker(sql,params);
	}
	
	public static SqlWorker sql(String sql,Object[] params){
		return new SqlWorker(sql,params);
	}
	
	public static FindWorker find(ActiveRecord record){
		return new FindWorker(record);
	}
	
	public static FindWorker find(ActiveRecord record,Map<String,Object> params){
		return new FindWorker(record,params);
	}

	public static CreateWorker create(ActiveRecord record){
		return new CreateWorker(record);
	}
	
	public static CreateWorker create(ActiveRecord record,Map<String,Object> params){
		return new CreateWorker(record,params);
	}
	
	public static DestroyWorker destroy(ActiveRecord record){
		return new DestroyWorker(record);
	}
	
	public static DestroyWorker destroy(ActiveRecord record,Map<String,Object> params){
		return new DestroyWorker(record,params);
	}
	
	public static UpdateWorker update(ActiveRecord record){
		return new UpdateWorker(record);
	}
	
	public static UpdateWorker update(ActiveRecord record,Map<String,Object> params){
		return new UpdateWorker(record,params);
	}
	
	public static HasWorker has(){
		return new HasWorker();
	}

}
