package net.rails.sql.worker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.active_record.ActiveRecord;

public final class FindWorker extends AbsWorker implements Cloneable{	
	
	private final Logger log = LoggerFactory.getLogger(FindWorker.class);
	private final List<String> selects = new ArrayList<String>();
	private final List<String> orders = new ArrayList<String>();
	private final List<String> groups = new ArrayList<String>();
	private final List<String> wheres = new ArrayList<String>();
	private final List<String> joins = new ArrayList<String>();
	private final List<String> havings = new ArrayList<String>();	
	private final List<String> firsts = new ArrayList<String>();
	private final List<String> lasts = new ArrayList<String>();
	private String cacheName;
	private boolean cached = false;
	private boolean cacheForced = false;
	private int cacheSecond = 0;
	private Integer limit = null;
	private Integer offset = null;
	private boolean distinct = false;
	
	public FindWorker(ActiveRecord record){
		super(record);
		this.adapter = record.getReaderAdapter();
	}
	
	public FindWorker(ActiveRecord record,Map<String,Object> params){
		super(record,params);		
	}
	
	public void setLimit(Integer limit){
		this.limit = limit;
	}
	
	public Integer getLimit(){
		return limit;
	}
	
	public void setOffset(Integer offset){
		this.offset = offset;
	}
	
	public Integer getOffset(){
		return offset;
	}
	
	public void setDistinct(boolean distinct){
		this.distinct = distinct;
	}
	
	public boolean isDistinct(){
		return distinct;
	}
	
	@Override
    public FindWorker clone() throws CloneNotSupportedException {
		FindWorker o = (FindWorker)super.clone();
		FindWorker c = new FindWorker(o.record);
		c.firsts().addAll(o.firsts);
		c.groups().addAll(o.groups);
		c.havings().addAll(o.havings);
		c.joins().addAll(o.joins);
		c.lasts().addAll(o.lasts);
		c.orders().addAll(o.orders);
		c.selects().addAll(o.selects);
		c.wheres().addAll(o.wheres);
		c.params().putAll(o.params);
		c.setDistinct(o.isDistinct());
		c.limit = o.limit;
		c.offset = o.offset;
		c.setCached(false);
		c.setCacheName(null);
		c.setCacheSecond(0);
		c.setCacheForced(false);
		return c;
	}

	public List<String> selects() {
		return selects;
	}

	public StringBuffer createSelects() {
		final StringBuffer sbf = new StringBuffer();
		selects.remove("");
		int size = selects.size();
		for (int i = 0; i < size; i++) {
			sbf.append(selects.get(i));
			if (i < size - 1)
				sbf.append(",");
		}
		if (size == 0)
			sbf.append(adapter.quoteSchemaAndTableName() + ".*");
		return sbf;
	}

	public List<String> orders() {
		return orders;
	}

	public StringBuffer createOrders() {
		final StringBuffer sbf = new StringBuffer();
		orders.remove("");
		int size = orders.size();
		for (int i = 0; i < size; i++) {
			sbf.append(orders.get(i));
			if (i < size - 1)
				sbf.append(",");
		}
		return sbf;
	}

	public List<String> groups() {
		return groups;
	}

	public StringBuffer createGroups() {
		final StringBuffer sbf = new StringBuffer();
		groups.remove("");
		int size = groups.size();
		for (int i = 0; i < size; i++) {
			sbf.append(groups.get(i));
			if (i < size - 1)
				sbf.append(",");
		}
		return sbf;
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

	public List<String> havings() {
		return havings;
	}

	public StringBuffer createHavings() {
		final StringBuffer sbf = new StringBuffer();
		havings.remove("");
		int size = havings.size();
		for (int i = 0; i < size; i++) {
			sbf.append(havings.get(i));
			if (i < size - 1)
				sbf.append(" ");
		}
		return sbf;
	}

	public List<String> joins() {
		return joins;
	}

	public StringBuffer createJoins() {
		final StringBuffer sbf = new StringBuffer();
		joins.remove("");
		int size = joins.size();
		for (int i = 0; i < size; i++) {
			sbf.append(joins.get(i));
			if (i < size - 1)
				sbf.append(" ");
		}
		return sbf;
	}

	public List<String> firsts() {
		return firsts;
	}

	public StringBuffer createFirsts() {
		final StringBuffer sbf = new StringBuffer();
		firsts.remove("");
		int size = firsts.size();
		for (int i = 0; i < size; i++) {
			sbf.append(firsts.get(i));
			if (i < size - 1)
				sbf.append(" ");
		}
		return sbf;
	}

	public List<String> lasts() {
		return lasts;
	}

	public StringBuffer createLasts() {
		final StringBuffer sbf = new StringBuffer();
		lasts.remove("");
		int size = lasts.size();
		for (int i = 0; i < size; i++) {
			sbf.append(lasts.get(i));
			if (i < size - 1)
				sbf.append(" ");
		}
		return sbf;
	}
	
	public void setCached(boolean cached){
		this.cached = cached;
	}

	public boolean isCached(){
		return this.cached;
	}
	
	public void setCacheSecond(int cacheSecond){
		this.cacheSecond = cacheSecond;
	}
	
	public int getCacheSecond(){
		return this.cacheSecond;
	}
	
	public void setCacheName(String cacheName){
		this.cacheName = cacheName;
	}
	
	public String getCacheName(){
		return this.cacheName;
	}
	
	public void setCacheForced(boolean cacheForced){
		this.cacheForced = cacheForced;
	}
	
	public boolean isCacheForced(){
		return this.cacheForced;
	}
	
	public <T extends ActiveRecord> List<T> find() throws SQLException{	
		return (List<T>) ActiveRecord.find(record,this);
	}
	
	public <T extends ActiveRecord> T first() throws SQLException{	
		return (T) ActiveRecord.first(record,this);
	}
	
	@Override
	public String getSql() {
		String family = getAdapter().getDbcnf().get("family").toString();
		try{
			Class<Find> workerClass = (Class<Find>) Class.forName("net.rails.sql.worker." + family + "FindWorker");
			Find find = workerClass.newInstance();
			return find.getSql(this);
		}catch(Exception e){
			log.error("FindWorker not found");
			log.error(e.getMessage(), e);
			return null;
		}
	}	

}
