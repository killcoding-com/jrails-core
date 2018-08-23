package net.rails.sql.query;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.ext.AbsGlobal;
import net.rails.sql.Sql;
import net.rails.sql.query.worker.JoinWorker;
import net.rails.sql.query.worker.SelectWorker;
import net.rails.sql.query.worker.WhereWorker;
import net.rails.sql.worker.FindWorker;
import net.rails.support.Support;


public class Query implements Cloneable {

	protected Logger log;
	protected FindWorker worker;
	protected WhereWorker where;
	protected SelectWorker select;
	protected JoinWorker join;
	protected boolean autoJoin = false;
	protected boolean autoSelect = false;
	protected boolean autoGroup = false;
	protected boolean skipnil = false;
	protected boolean autoParseDate = false;
	protected boolean cacheForced = false;

	protected ActiveRecord from;
	protected String table;
	protected String qtable;
	protected String cacheName;
	protected int cacheSecond;

	public static Query from(AbsGlobal g, String table) {
		return new Query(ActiveRecord.eval(g, table));
	}

	public Query(ActiveRecord from) {
		super();
		log = LoggerFactory.getLogger(getClass());
		this.from = from;
		initQuery();
	}

	protected void initQuery() {
		table = from.getReaderAdapter().getTableName();
		qtable = from.getReaderAdapter().quoteSchemaAndTableName();
		worker = Sql.find(from);
		where = new WhereWorker(from) {
			@Override
			protected Object formater(String logic, String oper, String table,
					String column, Object value) {

				return format(logic, oper, table, column, value);
			}
		};
		where.froms().add(table);
		select = where.select(); // new SelectWorker(from);
		join = new JoinWorker(from);
	}

	@Override
	public Query clone() throws CloneNotSupportedException {
		final Query me = (Query) super.clone();
		final Query c = new Query(me.from);
		c.select.selects().clear();
		c.select.selects().addAll(me.select.selects());
		c.select.froms().clear();
		c.select.froms().addAll(me.select.froms());
		c.where.wheres().clear();
		c.where.wheres().addAll(me.where.wheres());
		c.where.havings().clear();
		c.where.havings().addAll(me.where.havings());
		c.where.froms().clear();
		c.where.froms().addAll(me.where.froms());
		c.where.groups().clear();
		c.where.groups().addAll(me.where.groups());
		c.where.orders().clear();
		c.where.orders().addAll(me.where.orders());
		c.where.ors().clear();
		c.where.ors().putAll(me.where.ors());
		c.where.ands().clear();
		c.where.ands().putAll(me.where.ands());
		c.join.joins().clear();
		c.join.joins().putAll(me.join.joins());
		c.join.otherJoins().clear();
		c.join.otherJoins().addAll(me.join.otherJoins());
		c.join.froms().clear();
		c.join.froms().addAll(me.join.froms());
		c.select(me.autoSelect);
		c.join(me.autoJoin);
		c.group(me.autoGroup);
		c.skipnil(me.skipnil);
		c.date(me.autoParseDate);
		c.cacheName = null;
		c.cacheSecond = 0;
		c.cacheForced = false;
		return c;
	}

	public Query count(String t, String c) {
		select.count(t, c);
		return this;
	}
	
	public Query count(String c) {
		select.count(table, c);
		return this;
	}
	
	public Query min(String t, String c) {
		select.min(t, c);
		return this;
	}
	
	public Query min(String c) {
		select.min(table, c);
		return this;
	}

	public Query max(String t, String c) {
		select.max(t, c);
		return this;
	}
	
	public Query max(String c) {
		select.max(table, c);
		return this;
	}

	public Query sum(String t, String c) {
		select.sum(t, c);
		return this;
	}

	public Query sum(String c) {
		select.sum(table, c);
		return this;
	}

	public Query avg(String t, String c) {
		select.avg(t, c);
		return this;
	}
	
	public Query avg(String c) {
		select.avg(table, c);
		return this;
	}
	
	public Query limit(Integer limit) {
		worker.setLimit(limit);
		return this;
	}

	public Query offset(Integer offset) {
		worker.setOffset(offset);
		return this;
	}

	public <T extends ActiveRecord> List<T> find(boolean disc, String first,
			String last) throws SQLException {
		FindWorker fw = this.getWorker();
		if (first != null)
			fw.firsts().add(first);
		if (last != null)
			fw.lasts().add(last);

		if (disc) {
			if (fw.selects().size() > 0) {
				String ds = "DISTINCT " + fw.selects().get(0);
				fw.selects().remove(0);
				fw.selects().add(0, ds);
			} else {
				String ds = "DISTINCT " + qtable + ".*";
				fw.selects().add(ds);
			}
		}
		return fw.find();
	}

	public <T extends ActiveRecord> List<T> find(String first, String last)
			throws SQLException {
		return find(false, first, last);
	}

	public <T extends ActiveRecord> List<T> find(boolean disc, String last)
			throws SQLException {
		return find(disc, null, last);
	}

	public <T extends ActiveRecord> List<T> find(String last)
			throws SQLException {
		return find(false, null, last);
	}

	public <T extends ActiveRecord> List<T> find() throws SQLException {
		return find(false, null, null);
	}

	public <T extends ActiveRecord> List<T> find(boolean disc)
			throws SQLException {
		return find(disc, null, null);
	}

	public <T extends ActiveRecord> ActiveRecord first(boolean disc,
			String first, String last) throws SQLException {
		FindWorker fw = this.getWorker();
		if (first != null)
			fw.firsts().add(first);
		if (last != null)
			fw.lasts().add(last);

		if (disc) {
			if (fw.selects().size() > 0) {
				String ds = "DISTINCT " + fw.selects().get(0);
				fw.selects().remove(0);
				fw.selects().add(0, ds);
			} else {
				String ds = "DISTINCT " + qtable + ".*";
				fw.selects().add(ds);
			}
		}
		return (T) fw.first();
	}

	@SuppressWarnings("unchecked")
	public <T extends ActiveRecord> T first(String first, String last)
			throws SQLException {
		return (T) first(false, first, last);
	}

	@SuppressWarnings("unchecked")
	public <T extends ActiveRecord> T first(String last) throws SQLException {
		return (T) first(false, null, last);
	}

	@SuppressWarnings("unchecked")
	public <T extends ActiveRecord> T first(boolean disc, String last)
			throws SQLException {
		return (T) first(disc, null, last);
	}

	@SuppressWarnings("unchecked")
	public <T extends ActiveRecord> T first() throws SQLException {
		return (T) first(false, null, null);
	}

	@SuppressWarnings("unchecked")
	public <T extends ActiveRecord> T first(boolean disc) throws SQLException {
		return (T) first(disc, null, null);
	}

	public Query generate() {
		where.skipnil(skipnil);
		where.group(autoGroup);
		where.generate();

//		List<String> paramKeys = Support.map(where.params()).keys();
//		for (String pk : paramKeys) {
//			Object v = where.params().get(pk);
//			if (v == null)
//				continue;
//
//			String sv = v.toString().toLowerCase();
//			if (sv.equals("true")) {
//				where.params().put(pk, true);
//			} else if (sv.equals("false")) {
//				where.params().put(pk, false);
//			}
//		}

		worker.params().putAll(where.params());
		worker.setCached(cacheSecond != 0);
		worker.setCacheName(cacheName);
		worker.setCacheSecond(cacheSecond);
		worker.setCacheForced(cacheForced);

		final List<String> froms = new ArrayList<String>();
		froms.addAll(select.froms());
		froms.addAll(where.froms());
		unique(froms);

		if (autoSelect) {
			for (String t : froms) {
				if (!select.selects().contains(t))
					select(t);
			}
		}

		if (select.selects().size() > 0)
			worker.selects().addAll(select.selects());

		unique(worker.selects());

		/** Begin where conds **/
		int size = where.wheres().size();
		for (int i = size - 1; i > 0; i--) {
			where.wheres().add(i, "AND");
		}

		if (where.wheres().size() > 0) {
			if (worker.wheres().size() > 0)
				worker.wheres().add("AND");

			worker.wheres().addAll(where.wheres());
		}
		/** End where conds **/

		/** Begin having conds **/
		size = where.havings().size();
		for (int i = size - 1; i > 0; i--) {
			where.havings().add(i, "AND");
		}

		if (where.havings().size() > 0) {
			if (worker.havings().size() > 0)
				worker.havings().add("AND");

			worker.havings().addAll(where.havings());
		}
		/** End having conds **/

		if (autoJoin) {
			for (String t : froms) {
				if (!t.equals(table) && !join.joins().containsKey(t)) {
					inner(t);
				}
			}
		}

		/** Join table **/
		final List<String> js = join.generate();
		if (js.size() > 0) {
			worker.joins().addAll(js);
		}
		unique(worker.joins());

		if (where.orders().size() > 0)
			worker.orders().addAll(where.orders());

		unique(worker.orders());

		if (where.groups().size() > 0)
			worker.groups().addAll(where.groups());

		unique(worker.orders());

		/** Reset worker **/
		where = new WhereWorker(from) {
			@Override
			protected Object formater(String logic, String oper, String table,
					String column, Object value) {

				return format(logic, oper, table, column, value);
			}
		};
		where.skipnil(skipnil);
		where.group(autoGroup);
		select = new SelectWorker(from);
		join = new JoinWorker(from);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void unique(List list) {
		HashSet hs = new HashSet(list);
		list.clear();
		list.addAll(hs);
	}
	
	public ActiveRecord getFrom(){
		return from;
	}

	public String getTable() {
		return table;
	}

	public FindWorker getWorker() {
		generate();
		return worker;
	}

	public WhereWorker getWhere() {
		return where;
	}

	public SelectWorker getSelect() {
		return select;
	}

	public JoinWorker getJoin() {
		return join;
	}

	public Query and(String logic, Object value) {
		where.and(logic, value);
		return this;
	}

	public Query and(String logic) {
		where.and(logic, true);
		return this;
	}

	public Query and(Map<String, Object> ands) {
		where.and(ands);
		return this;
	}

	public Query or(String logic, Object value) {
		where.or(logic, value);
		return this;
	}
	
	public Query or(String logic) {
		where.or(logic, true);
		return this;
	}

	public Query or(Map<String, Object> ors) {
		where.or(ors);
		return this;
	}

	public Query order(String t, String c, String dire) {
		where.and("order_" + c + "_from_" + t, dire);
		return this;
	}

	public Query order(String c, String dire) {
		where.and("order_" + c, dire);
		return this;
	}
	
	public Query group(String t,String c){
		where.and("group_" + c + "_from_" + t,true);
		return this;
	}
	
	public Query group(String c){
		where.and("group_" + c,true);
		return this;
	}

	public Query asc(String t, String c) {
		where.and("order_" + c + "_from_" + t, "ASC");
		return this;
	}

	public Query asc(String c) {
		where.and("order_" + c, "ASC");
		return this;
	}

	public Query desc(String t, String c) {
		where.and("order_" + c + "_from_" + t, "DESC");
		return this;
	}

	public Query desc(String c) {
		where.and("order_" + c, "DESC");
		return this;
	}

	public Query select(String t, String c) {
		select.select(t, c);
		return this;
	}

	public Query select(String t, String c, String as) {
		select.select(t, c, as);
		return this;
	}

	public Query select(String t) {
		select.all(t);
		return this;
	}

	public Query select(ActiveRecord t) {
		select.all(t);
		return this;
	}

	public Query as(String c) {
		select.select(table, c);
		return this;
	}

	public Query as(String c, String as) {
		select.select(table, c, as);
		return this;
	}

	public Query as(String t, String c, String as) {
		select.select(t, c, as);
		return this;
	}

	public Query inner(String t) {
		join.inner(t);
		return this;
	}

	public Query inner(String t, String fk) {
		join.inner(t, fk);
		return this;
	}

	// public Query inners(String t){
	// join.inner(t);
	// return this;
	// }

	public Query innerAs(String t, String as) {
		join.innerAs(t, as);
		return this;
	}

	public Query innerAs(String t, String as, String fk) {
		join.innerAs(t, as, fk);
		return this;
	}

	public Query left(String t) {
		join.left(t);
		return this;
	}

	public Query left(String t, String fk) {
		join.left(t, fk);
		return this;
	}

	public Query leftAs(String t, String as) {
		join.leftAs(t, as);
		return this;
	}

	public Query leftAs(String t, String as, String fk) {
		join.leftAs(t, as, fk);
		return this;
	}

	public Query right(String t) {
		join.right(t);
		return this;
	}

	public Query right(String t, String fk) {
		join.right(t, fk);
		return this;
	}

	public Query rightAs(String t, String as) {
		join.rightAs(t, as);
		return this;
	}

	public Query rightAs(String t, String as, String fk) {
		join.rightAs(t, as, fk);
		return this;
	}

	public Query skipnil(boolean a) {
		this.skipnil = a;
		return this;
	}

	public Query group(boolean a) {
		this.autoGroup = a;
		return this;
	}

	public Query select(boolean a) {
		this.autoSelect = a;
		return this;
	}

	public Query join(boolean a) {
		this.autoJoin = a;
		return this;
	}

	public Query date(boolean a) {
		this.autoParseDate = a;
		return this;
	}

	public Query cache(String n) {
		this.cacheName = n;
		return this;
	}

	public Query cache(int t) {
		this.cacheSecond = t;
		return this;
	}

	public Query cache(int t, boolean forced) {
		this.cacheSecond = t;
		this.cacheForced = forced;
		return this;
	}

	public Query cache(boolean forced) {
		this.cacheForced = forced;
		return this;
	}

	public static String as(ActiveRecord from) {
		Adapter adapter = from.getReaderAdapter();
		String table = adapter.getTableName();
		String quoteTable = adapter.quoteSchemaAndTableName();
		List<String> names = from.getAttributes();
		int i = 0;
		for (String name : names) {
			names.set(i, MessageFormat.format("{0}.{1} AS as_{2}_{3}",
					quoteTable, adapter.quote(name), table, name));
			i++;
		}
		return Support.array(names).join(",");
	}

	public static <T extends ActiveRecord> T parseAs(T from,
			Map<String, Object> result) {
		String tbName = from.getReaderAdapter().getTableName();
		List<String> attrs = from.getAttributes();
		from.clear();
		for (String attr : attrs) {
			String asName = "as_" + tbName + "_" + attr;
			if (result.containsKey(asName)) {
				from.put(attr, result.get(asName));
			}
		}
		from.putAllFindRecord(from);
		from.setSaveAction(ActiveRecord.ON_UPDATE);
		return from;
	}

	protected Object format(String logic, String oper, String table,
			String column, Object value) {
		if (value instanceof String) {
			AbsGlobal g = from.getGlobal();
			String sv = value.toString().trim();
			if (autoParseDate) {
				try {
					Object v = null;
					if(sv.equals("@now"))
						v = new Timestamp(new Date().getTime());
					else if(sv.equals("@today"))
						v = new java.sql.Date(new Date().getTime());
					else if(Support.string(sv).isDateFormat(g.t("formats","datetime")))
						v = g.text2datetime(value.toString());
					else if(Support.string(sv).isDateFormat(g.t("formats","date")))
						v = g.text2data(value.toString());
					else if(Support.string(sv).isDateFormat(g.t("formats","time")))
						v = g.text2time(value.toString());
					else
						v = value.toString().trim();

					return v;
				} catch (ParseException e) {
					log.error(e.getMessage(),e);
					return value.toString().trim();
				}
			}
			
			if(sv.equals("true")){
				return true;
			}else if(sv.equals("false")){
				return false;
			}else if(parseField(sv)){
				String key = sv.replaceFirst("^:", "");
				if(from.containsKey(key)){
					Object param = from.get(key);
					where.params().put(logic,param);
					return param;
				}
				return sv;
			}else {
				return sv;
			}
		}
		return value;
	}
	
	private boolean parseField(String key){
		 Pattern p = Pattern.compile("^:[\\w-]+$");
		 Matcher m = p.matcher(key);
		 return m.find();
	}

}
