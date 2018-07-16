package net.rails.active_record.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.active_record.Attribute;
import net.rails.sql.query.Query;
import net.rails.support.Support;

public final class UniquenessValidate extends Validate {
	
	private ActiveRecord record;
	private Object id;
	private Object parseValue;
	private Adapter adapter;
	private List<String> scope;
	private Map<String,Object> values;
	private Map<String,Object> and;
	private Map<String,Object> or;

	@SuppressWarnings("unchecked")
	public UniquenessValidate(Attribute attribute,Map<String,Object> values) {
		super(attribute);
		this.record = attribute.getRecord();
		this.adapter = record.getReaderAdapter();
		this.id = record.get(adapter.getPrimaryKey());
		this.values = values;
		scope = new ArrayList<String>();
		if(valiCnf != null){	
			Object o = valiCnf.get("scope");
			if(o != null)
				scope = (List<String>)o;

			and = (Map<String, Object>) valiCnf.get("and");
			or = (Map<String, Object>) valiCnf.get("or");
		}else{
			valiCnf = new HashMap<String,Object>();
		}
	}
	
	@Override
	public String getOf() {
		return "validates_uniqueness_of";
	}
	
	@Override
	public <T extends Object> T pass(Object value) {
		try{
			parseValue = attribute.parse(value);
		}catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}

		if(Support.object(parseValue).blank() && isAllowBlank()){
			return (T) parseValue;
		}
		values.put(name,value);
		String table = adapter.getTableName();
		String pk = adapter.getPrimaryKey();
		try {			
			Query q = new Query(record.clone());
			q.select(false);
			q.join(true);
			q.date(true);
			q.group(true);
			q.and(and);
			q.or(or);
			q.count(table,pk);
			for(String sc : scope){
				q.and("eq_" + sc,values.get(sc));				
			}
			if(value == null)
				q.and("nil_" + name);
			else
				q.and("eq_" + name,parseValue);
			
			String saveAction = Support.string(record.getSaveAction()).def("save");
			if(saveAction.equals(ActiveRecord.ON_UPDATE)){
				q.and("ne_" + pk,id);
			}		
			int count = q.first().getNumber("count_" + table + "_" + pk).intValue();
			if(count > 0)
				error("message",parseValue,parseValue);
			
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
		}
		return (T) parseValue;
	}
	
}