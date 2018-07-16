package net.rails.sql.worker;

import java.util.HashMap;
import java.util.Map;

import net.rails.active_record.ActiveRecord;
import net.rails.ext.AbsGlobal;
import net.rails.sql.query.Query;
import net.rails.support.Support;

public class HasWorker {
	
	@SuppressWarnings("unchecked")
	public Query hasOneOrMany(String relation,ActiveRecord myself,String hasName){
		AbsGlobal g = myself.getGlobal();
		Map<String, Object> hasCnf = Support.config().getModels().get(myself.getClass().getSimpleName());	
		if(hasCnf == null){
			hasCnf = new HashMap<String,Object>();
		}
		Map<String,Map<String,Object>> hasRelationCnf = (Map<String, Map<String,Object>>) hasCnf.get(relation);
		if(hasRelationCnf == null)
			hasRelationCnf = new HashMap<String, Map<String,Object>>();
		
		Map<String,Object> hasNameCnf = hasRelationCnf.get(hasName);
		if(hasNameCnf == null)
			hasNameCnf = new HashMap<String, Object>();
			
		String defFk = myself.getReaderAdapter().getTableName() +  "_id";
		String foreignKey = Support.object(hasNameCnf.get("foreign_key")).def(defFk).toString();
		String classify = Support.object(hasNameCnf.get("classify")).def(hasName).toString();
		Map<String,Object> and = (Map<String, Object>) hasNameCnf.get("and");
		Map<String,Object> or = (Map<String, Object>) hasNameCnf.get("or");
		Integer cache = (Integer) hasNameCnf.get("cache");
		Integer limit = (Integer) hasNameCnf.get("limit");
		Integer offset = (Integer) hasNameCnf.get("offset");
		
		Query q = Query.from(g,classify);
		q.join(true);
		q.date(true);
		if(limit != null)
			q.limit(limit);
		if(offset != null)
			q.offset(offset);
		if(cache != null)
			q.cache(cache);
		
		q.and("eq_" + foreignKey,myself.getId());
		q.and(and).or(or);
		return q;
	}
	
	public Query hasMany(ActiveRecord myself,String hasName){
		return hasOneOrMany("has_many",myself,hasName);
	}
	
	public Query hasOne(ActiveRecord myself,String hasName){
		return hasOneOrMany("has_one",myself,hasName);
	}
	
	@SuppressWarnings("unchecked")
	public Query belongsTo(ActiveRecord myself,String belongName){
		AbsGlobal g = myself.getGlobal();
		Map<String, Object> hasCnf = Support.config().getModels().get(myself.getClass().getSimpleName());		
		if(hasCnf == null){
			hasCnf = new HashMap<String,Object>();
		}
		Map<String,Map<String,Object>> belongsToCnf = (Map<String, Map<String,Object>>) hasCnf.get("belongs_to");
		Map<String,Object> belongNameCnf = null;
		if(belongsToCnf == null)
			belongsToCnf = new HashMap<String,Map<String,Object>>();
		
		belongNameCnf = belongsToCnf.get(belongName);		
		if(belongNameCnf == null)
			belongNameCnf = new HashMap<String, Object>();
		
		String classify = Support.object(belongNameCnf.get("classify")).def(belongName).toString();
		ActiveRecord belongModel = ActiveRecord.eval(g, classify);		
		String defFk = belongModel.getReaderAdapter().getTableName() +  "_id";
		String foreignKey = Support.object(belongNameCnf.get("foreign_key")).def(defFk).toString();		
		Map<String,Object> and = (Map<String, Object>) belongNameCnf.get("and");
		Map<String,Object> or = (Map<String, Object>) belongNameCnf.get("or");	
		Integer cache = (Integer) belongNameCnf.get("cache");
		Integer limit = (Integer) belongNameCnf.get("limit");
		Integer offset = (Integer) belongNameCnf.get("offset");
		Query q = new Query(belongModel);
		q.join(true);
		q.date(true);
		if(limit != null)
			q.limit(limit);
		if(offset != null)
			q.offset(offset);
		if(cache != null)
			q.cache(cache);
		
		q.and("eq_id",myself.get(foreignKey));
		q.and(and).or(or);
		return q;
	}

}
