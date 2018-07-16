package net.rails.sql.query.worker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.sql.query.Query;
import net.rails.support.Support;

public class SelectWorker {
	
	public void sum(String t,String c){
		from(t);
		String func = MessageFormat.format("SUM({0}.{1}) AS sum_{2}_{3}", fada.quoteSchemaDot() + quote(t),quote(c),t,c);
		select(func);
	}
	
	public void count(String t,String c){
		from(t);
		String ca = Support.string(c).def("*").equals("*") ? "*" : quote(c);
		String func = MessageFormat.format("COUNT({0}.{1}) AS count_{2}_{3}", fada.quoteSchemaDot() + quote(t),ca,t,c);
		select(func);
	}
	
	public void avg(String t,String c){
		from(t);
		String func = MessageFormat.format("AVG({0}.{1}) AS avg_{2}_{3}", fada.quoteSchemaDot() + quote(t),quote(c),t,c);
		select(func);
	}
	
	public void min(String t,String c){
		from(t);
		String func = MessageFormat.format("MIN({0}.{1}) AS min_{2}_{3}", fada.quoteSchemaDot() + quote(t),quote(c),t,c);
		select(func);
	}
	
	public void max(String t,String c){
		from(t);
		String func = MessageFormat.format("MAX({0}.{1}) AS max_{2}_{3}", fada.quoteSchemaDot() + quote(t),quote(c),t,c);
		select(func);
	}
	
	public void select(String t, String cols) {
		from(t);
		if (cols.trim().equals("*"))
			select(MessageFormat.format("{0}.*",fada.quoteSchemaDot() + quote(t)));

		String[] cs = cols.split(",");
		for (String c : cs) {
			select(MessageFormat.format("{0}.{1} AS as_{2}_{3}",fada.quoteSchemaDot() + quote(t),quote(c), t, c));
		}
	}
	
	public void select(String t, String col,String as) {
		from(t);
		select(MessageFormat.format("{0}.{1} AS {2}",fada.quoteSchemaDot() + quote(t),quote(col),as));
	}
	
	public void all(ActiveRecord table) {
		from(table.getReaderAdapter().getTableName());
		select(Query.as(table));
	}
	
	public void all(String table) {
		from(table);
		all(ActiveRecord.eval(from.getGlobal(), table));
	}
	
	public List<String> froms(){
		return froms;
	}
	
	public List<String> selects(){
		return selects;
	}
	
	private String quote(String keyword){
		return fada.quote(keyword);
	}
	
	private void from(String f){
		if(!froms.contains(f))
			froms.add(f);
	}

	protected void select(String s){
		if(!selects.contains(s))
			selects.add(s);
	}
	
	protected final List<String> selects = new ArrayList<String>();
	private final List<String> froms = new ArrayList<String>();
	private Adapter fada;
	private ActiveRecord from;
	
	public SelectWorker(ActiveRecord from){
		super();
		this.from = from;
		this.fada = from.getReaderAdapter();
	}
	
}
