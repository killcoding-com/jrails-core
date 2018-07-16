package net.rails.sql.query.worker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.ext.IndexMap;
import net.rails.support.Support;

public class WhereWorker {
	
	private final static Map<String, String> LOGICALS = new IndexMap<String, String>();
	
	static {
		LOGICALS.put("eq", "{0} = {1}");
		LOGICALS.put("ne", "{0} <> {1}");
		LOGICALS.put("gt", "{0} > {1}");
		LOGICALS.put("lt", "{0} < {1}");
		LOGICALS.put("ge", "{0} >= {1}");
		LOGICALS.put("le", "{0} <= {1}");
		LOGICALS.put("any", "{0} LIKE {1}");
		LOGICALS.put("start", "{0} LIKE {1}");
		LOGICALS.put("end", "{0} LIKE {1}");
		LOGICALS.put("nil", "({0} IS NULL OR {0} = '''')");
		LOGICALS.put("nnil", "({0} IS NOT NULL OR {0} <> '''')"); // protected
		LOGICALS.put("in", "{0} IN ({1})");
		LOGICALS.put("ni", "{0} NOT IN ({1})");
		LOGICALS.put("count_eq", "COUNT({0}) = {1}");
		LOGICALS.put("count_ne", "COUNT({0}) <> {1}");
		LOGICALS.put("count_le", "COUNT({0}) <= {1}"); 
		LOGICALS.put("count_lt", "COUNT({0}) < {1}");
		LOGICALS.put("count_ge", "COUNT({0}) >= {1}");
		LOGICALS.put("count_gt", "COUNT({0}) > {1}");
		LOGICALS.put("min_eq", "MIN({0}) = {1}");
		LOGICALS.put("min_ne", "MIN({0}) <> {1}");
		LOGICALS.put("min_le", "MIN({0}) <= {1}");
		LOGICALS.put("min_lt", "MIN({0}) < {1}");
		LOGICALS.put("min_ge", "MIN({0}) >= {1}");
		LOGICALS.put("min_gt", "MIN({0}) > {1}");		
		LOGICALS.put("max_eq", "MAX({0}) = {1}");
		LOGICALS.put("max_ne", "MAX({0}) <> {1}");
		LOGICALS.put("max_le", "MAX({0}) <= {1}");
		LOGICALS.put("max_lt", "MAX({0}) < {1}");
		LOGICALS.put("max_ge", "MAX({0}) >= {1}");
		LOGICALS.put("max_gt", "MAX({0}) > {1}");		
		LOGICALS.put("sum_eq", "SUM({0}) = {1}");
		LOGICALS.put("sum_ne", "SUM({0}) <> {1}");
		LOGICALS.put("sum_le", "SUM({0}) <= {1}");
		LOGICALS.put("sum_lt", "SUM({0}) < {1}");
		LOGICALS.put("sum_ge", "SUM({0}) >= {1}");
		LOGICALS.put("sum_gt", "SUM({0}) > {1}");		
		LOGICALS.put("avg_eq", "AVG({0}) = {1}");
		LOGICALS.put("avg_ne", "AVG({0}) <> {1}");
		LOGICALS.put("avg_le", "AVG({0}) <= {1}");
		LOGICALS.put("avg_lt", "AVG({0}) < {1}");
		LOGICALS.put("avg_ge", "AVG({0}) >= {1}");
		LOGICALS.put("avg_gt", "AVG({0}) > {1}");		
		LOGICALS.put("min_having", "MIN({0})");
		LOGICALS.put("max_having", "MAX({0})");
	}
	protected Map<String,String> tables = new HashMap<String,String>();
	
	private List<String> wheres = new ArrayList<String>(); // where table names
	private List<String> havings = new ArrayList<String>(); // havings table names
	private List<String> froms = new ArrayList<String>(); // froms table names
	private List<String> orders = new ArrayList<String>(); // order by list
	private List<String> groups = new ArrayList<String>(); // group by list
	private Map<String,Object> ors = new IndexMap<String,Object>();
	private Map<String,Object> ands = new IndexMap<String,Object>();
	private Map<String,Object> params = new IndexMap<String,Object>();
	private SelectWorker select;
	private Adapter fada;
	private ActiveRecord from;
	private String table;
	private String qtable;
	private boolean skipnil = false;
	private boolean group = false;
	
	public WhereWorker(ActiveRecord from){
		super();
		this.from = from;
		this.fada = this.from.getReaderAdapter();
		select = new SelectWorker(from);
		table = fada.getTableName();
		qtable = fada.quoteSchemaAndTableName();
	}
	
	public void or(String key,Object value){
		if(!Support.string(key).blank())
			ors.put(key, value);
	}
	
	public void or(Map<String,Object> ors){
		if(ors != null)
			this.ors.putAll(ors);
	}
	
	public void and(String key,Object value){
		if(!Support.string(key).blank())
			ands.put(key, value);
	}
	
	public void and(Map<String,Object> ands){
		if(ands != null)
			this.ands.putAll(ands);
	}
	
	public void generate(){
		final List<String> aws = new ArrayList<String>();
		final List<String> ows = new ArrayList<String>();
		final List<String> haws = new ArrayList<String>();
		final List<String> hows = new ArrayList<String>();
		wheres(ands, aws,haws);
		wheres(ors, ows,hows);
		if(aws.size() > 0)
			wheres.addAll(aws);
		if(ows.size() > 0)
			wheres.add(connect(ows));		
		if(haws.size() > 0)
			havings.addAll(haws);
		if(hows.size() > 0)
			havings.add(connect(hows));
		
	}
	
	private String connect(List<String> ws){
		int size = ws.size();
		for(int i = size - 1;i > 0;i--){
			ws.add(i, " OR ");
		}
		String s = Support.array(ws).join("");
		return size > 1 ? "(" + s + ")" : s;
	}
	
	private  void wheres(Map<String, Object> andsOrs,List<String> andsOrsWheres,List<String> andsOrsHavings) {
		String firstLogic = "^(eq|ne|gt|lt|ge|le|any|start|end|in|ni|nil|"
							+ "order|asc|desc|group|"							
							+ "count_eq|count_le|count_lt|count_ge|count_gt|"						
							+ "min_eq|min_le|min_lt|min_ge|min_gt|min_having|"
							+ "max_eq|max_le|max_lt|max_ge|max_gt|max_having|"
							+ "sum_eq|sum_le|sum_lt|sum_ge|sum_gt|"
							+ "avg_eq|avg_le|avg_lt|avg_ge|avg_gt)\\_";
		Pattern p = null;
		Matcher m = null;
		List<String> logics = Support.map(andsOrs).keys();
		for (String logic : logics) {
			p = Pattern.compile(firstLogic);
			m = p.matcher(logic);
			if (m.find()) {
				String oper = m.group().replaceFirst("\\_$", "");
				// Column and table split
				String[] ct = m.replaceFirst("").split("_from_");
				Object value = andsOrs.get(logic);
				if (skipnil && Support.object(value).blank()){
					continue;
				}
				String t = null;
				String c = null;
				String[] names = null;
				switch (ct.length) {
				case 1:
					names = ct[0].split("-");
					t = table;
					c = names[0];
					break;
				case 2:					
					names = ct[1].split("-");
					t = names[0];
					c = ct[0];
					break;
				default:
					break;
				}
				changes(andsOrsWheres,andsOrsHavings,logic, oper, t, c, value);
			}
		}
	}
	
	private void changes(List<String> andsOrsWheres,List<String> andsOrsHavings,String logic, String oper, String ftable,
			String column, Object value) {
		Object param = null;
		String where = null;
		if (oper.equals("eq") || oper.equals("ne") || oper.equals("gt")
				|| oper.equals("lt") || oper.equals("ge") || oper.equals("le")) {	
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper),fada.quoteSchemaDot() + quote(ftable) + "." + quote(column), ":" + logic);
			param = formater(logic,oper,ftable,column,value);
			andsOrsWheres.add(where);
			param(logic,param);
		} else if (oper.equals("in") || oper.equals("ni")) {
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() + quote(ftable) + "." + quote(column), ":" + logic);
			
			if (value instanceof List || value instanceof Object[]){
				param = formater(logic,oper,ftable,column,value);
			}else {
				List<Object> ls = new ArrayList<Object>();
				ls.add(formater(logic,oper,ftable,column,value));
				param = ls;
			}
			andsOrsWheres.add(where);
			param(logic,param);
		} else if (oper.equals("nil")) {
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() + quote(ftable) + "." + quote(column), ":" + logic);
			param = Boolean.parseBoolean(formater(logic,oper,ftable,column,value) + "");
			if ((Boolean) param)
				where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() + quote(ftable) + "."
						+ quote(column));
			else
				where = MessageFormat.format(LOGICALS.get("nnil"), fada.quoteSchemaDot() + quote(ftable) + "."
						+ quote(column));
			
			andsOrsWheres.add(where);
		} else if (oper.equals("any")) {
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() +  quote(ftable) + "." + quote(column), ":" + logic);
			param = "%" + formater(logic,oper,ftable,column,value) + "%";
			andsOrsWheres.add(where);
			param(logic,param);
		} else if (oper.equals("start")) {
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() + quote(ftable) + "." + quote(column), ":" + logic);
			param = formater(logic,oper,ftable,column,value) + "%";
			andsOrsWheres.add(where);
			param(logic,param);
		} else if (oper.equals("end")) {
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() + quote(ftable) + "." + quote(column), ":" + logic);
			param = "%" + formater(logic,oper,ftable,column,value);
			andsOrsWheres.add(where);
			param(logic,param);
		} else if (oper.equals("order")){
			from(ftable);
				if(logic.indexOf("_from_") > -1)
					order(MessageFormat.format("{0}.{1} {2}",fada.quoteSchemaDot() + quote(ftable),quote(column),value));
				else
					order(MessageFormat.format("{0} {1}",column,value));
		}else if (oper.equals("asc") || oper.equals("desc")){	
			from(ftable);
				from(ftable);
				if(logic.indexOf("_from_") > -1)
					order(MessageFormat.format("{0}.{1} {2}",fada.quoteSchemaDot() + quote(ftable),quote(column),oper.toUpperCase()));
				else
					order(MessageFormat.format("{0} {1}",column,oper.toUpperCase()));	
		} else if(oper.equals("group")){
			from(ftable);
			if(logic.indexOf("_from_") > -1){
				group(MessageFormat.format("{0}.{1}",fada.quoteSchemaDot() + quote(ftable),quote(column)));
			}else{
				group(MessageFormat.format("{0}",column));
			}
		}else if (Arrays.asList("count_eq","count_ne","count_le","count_lt","count_ge","count_gt",
								"min_eq","min_ne","min_le","min_lt","min_ge","min_gt",
								"max_eq","max_ne","max_le","max_lt","max_ge","max_gt",
								"sum_eq","sum_ne","sum_le","sum_lt","sum_ge","sum_gt",
								"avg_eq","avg_ne","avg_le","avg_lt","avg_ge","avg_gt").contains(oper)) {
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() + quote(ftable) + "." + quote(column), ":" + logic);
			param = formater(logic,oper,ftable,column,value);
			andsOrsHavings.add(where);
			param(logic,param);	
			if(group){
				if(logic.indexOf("_from_") > -1)
					group(MessageFormat.format("{0}.{1}",fada.quoteSchemaDot() + quote(ftable),quote(column)));
				else
					group(MessageFormat.format("{0}",column));
			}
		}else if (Arrays.asList("min_having","max_having").contains(oper)) {
			from(ftable);
			where = MessageFormat.format(LOGICALS.get(oper), fada.quoteSchemaDot() + quote(ftable) + "." + quote(column));
			param = formater(logic,oper,ftable,column,value);
			andsOrsHavings.add(where);
			param(logic,param);	
			if(group){
				if(logic.indexOf("_from_") > -1)
					group(MessageFormat.format("{0}.{1}",fada.quoteSchemaDot() + quote(ftable),quote(column)));
				else
					group(MessageFormat.format("{0}",column));
			}
		} 
	}
	
	/*protected methods*/
	
	protected Object formater(String logic,String oper,String table,String column, Object value) {
		if (value instanceof String){
			return value.toString().trim();
		}
		return value;
	}
	
	private String quote(String keyword){
		return fada.quote(keyword);
	}
	
	private void from(String f){
		if(!froms.contains(f))
			froms.add(f);
	}
	
	private void param(String key,Object value){
		params.put(key, value);
	}

	private void order(String o){
		if(!orders.contains(o))
			orders.add(o);
	}
	
	private void group(String g){
		if(!groups.contains(g))
			groups.add(g);
	}	
	
	/**shar methods**/

	public void skipnil(boolean a){
		this.skipnil = a;
	}
	
	public void group(boolean a){
		this.group = a;
	}
	
	public List<String> wheres(){
		return wheres;
	}
	
	public List<String> orders(){
		return orders;
	}
	
	public List<String> groups(){
		return groups;
	}
	
	public List<String> havings(){
		return havings;
	}
	
	public List<String> froms(){
		return froms;
	}
	
	public Map<String,Object> params(){
		return params;
	}
	
	public Map<String,Object> ands(){
		return ands;
	}
	
	public Map<String,Object> ors(){
		return ors;
	}
	
	public SelectWorker select(){
		return select;
	}
}
