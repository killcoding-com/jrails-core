package net.rails.active_record;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.active_record.exception.FieldNotFoundException;
import net.rails.active_record.exception.MessagesException;
import net.rails.active_record.exception.RecordNotFoundException;
import net.rails.active_record.validate.PresenceValidate;
import net.rails.active_record.validate.TypeException;
import net.rails.active_record.validate.Validate;
import net.rails.active_record.validate.exception.ConfigurException;
import net.rails.active_record.validate.exception.ValidateException;
import net.rails.ext.AbsGlobal;
import net.rails.ext.IndexMap;
import net.rails.ext.Json;
import net.rails.sql.Sql;
import net.rails.sql.query.Query;
import net.rails.sql.worker.DestroyWorker;
import net.rails.sql.worker.FindWorker;
import net.rails.sql.worker.HasWorker;
import net.rails.sql.worker.SqlWorker;
import net.rails.sql.worker.UpdateWorker;
import net.rails.support.Support;

@SuppressWarnings("serial")
public abstract class ActiveRecord extends IndexMap<String, Object> implements Cloneable, java.io.Serializable {
	
	public final static String ON_SAVE = "save";
	public final static String ON_CREATE = "create";
	public final static String ON_UPDATE = "update";	
	public boolean valid = false;	
	protected Map<String, Object> beforeRecord = new IndexMap<String, Object>();
	protected Map<String, Object> updateValues = new IndexMap<String,Object>(); 
	protected String saveAction;	
	protected Adapter readerAdapter;
	protected Adapter writerAdapter;
	protected AbsGlobal g;
	protected Logger log;
	
	public ActiveRecord(AbsGlobal g) {
		super();
		log = LoggerFactory.getLogger(getClass());
		this.g = g;
		this.saveAction = ON_CREATE;
		initAdapter();
	}

	public ActiveRecord(AbsGlobal g, Object id) throws SQLException,
			RecordNotFoundException {
		super();
		log = LoggerFactory.getLogger(getClass());
		this.g = g;
		this.saveAction = ON_UPDATE;
		initAdapter();
		find(id);
	}

	protected void messages(String on, Map<String, Object> values)
			throws ConfigurException, MessagesException {		
		List<String> list = new ArrayList<String>();
		List<String> attrs = getAttributes();
		attrs.remove(getReaderAdapter().getPrimaryKey());
		attrs.remove("deleted");
		attrs.remove("deleted_user_id");
		attrs.remove("deleted_at");
		attrs.remove("created_user_id");
		attrs.remove("created_at");
		attrs.remove("updated_user_id");
		attrs.remove("updated_at");
		
		for (String attr : attrs) {			
			try {
				Attribute a = getAttribute(attr);
				List<Validate> valis = a.getValidator();
				if (valis.size() > 0) {
					for (Validate v : valis) {
						if(v instanceof PresenceValidate && !values.containsKey(attr)){
							put(attr,null);
						}
						if (values.containsKey(attr)
								&& (v.getOn().equals(ON_SAVE) || v.getOn()
										.equals(on))) {
							v.pass(get(attr));
							if (v.getErrMsg() != null) {
								log.warn(v.getErrMsg());
								String msg = Support.string(v.getMessage()).def(v.getErrMsg());
								list.add(msg);
							}
						}
					}
				}
				put(attr, a.parse(values.get(attr)));
			} catch (TypeException e) {
				log.warn(e.getMessage(),e);
				list.add(e.getShowMsg());
			}
		}
		valid = true;
		if (list.size() > 0)
			throw new MessagesException(list);
	}


	protected void validates(String on, Map<String, Object> values,
			List<String> attrs) throws ValidateException, ConfigurException,
			TypeException {
		for (String attr : attrs) {
			Attribute a = getAttribute(attr);
			List<Validate> valis = a.getValidator();
//			if (valis.size() > 0) {
//				if (values.containsKey(attr))
//					put(attr, a.parse(values.get(attr)));
//				for (Validate v : valis) {
//					if (values.containsKey(attr)
//							&& (v.getOn().equals(ON_SAVE) || v.getOn().equals(
//									on))) {
//						v.passes(get(attr));
//					}
//				}
//			}else{
//				put(attr, a.parse(values.get(attr)));
//			}
			if (valis.size() > 0) {
				for (Validate v : valis) {
					if(v instanceof PresenceValidate && !values.containsKey(attr)){
						put(attr,null);
					}
					if (values.containsKey(attr)
							&& (v.getOn().equals(ON_SAVE) || v.getOn()
									.equals(on))) {
						v.passes(get(attr));
					}
				}
			}
			
		}
	}

	protected Map<String,Object> findFilter(Map<String, Object> map){		
		return map;
	}

	protected void initAdapter() {
		readerAdapter = new DBResource(g,getClass().getSimpleName(),
				DBResource.READER).getAdapter();
		writerAdapter = new DBResource(g,getClass().getSimpleName(),
				DBResource.WRITER).getAdapter();
	}

	protected void find(Object id) throws SQLException, RecordNotFoundException {		
		FindWorker worker = Sql.find(this);
		worker.wheres().add(String.format("%s.%s = :_id",readerAdapter.quoteSchemaAndTableName(), readerAdapter.quotePrimaryKey()));
		worker.params().put("_id", id);
		ActiveRecord ar = first(this, worker);
		if(ar == null)
			throw new RecordNotFoundException(getClass().getSimpleName() + " " + id);
		else{
			putAll(ar);
			putAllFindRecord(this);
		}
	}

	protected boolean deleteSlef() throws SQLException,
			FieldNotFoundException {
		Map<String, Object> values = new HashMap<String, Object>();
		List<String> attrs = getAttributes();
		if (attrs.contains("deleted_at"))
			values.put("deleted_at",
					new Timestamp(new java.util.Date().getTime()));

		if (attrs.contains("deleted_user_id"))
			values.put("deleted_user_id", g.getUserId());

		if (attrs.contains("deleted"))
			values.put("deleted", true);
		else
			throw new FieldNotFoundException(getClass().getSimpleName()
					+ ".deleted");

		UpdateWorker worker = Sql.update(this, values);
		worker.wheres().add(String.format("%s.%s = :_id",
				readerAdapter.quoteSchemaAndTableName(),
				readerAdapter.quotePrimaryKey()));
		worker.params().put("_id", getId());
		return writerAdapter.execute(Sql.sql(worker.getSql(), worker
				.params())) > 0;
	}

	protected boolean clearValue(String attr)
			throws SQLException, ValidateException, ConfigurException,
			TypeException {
		put(attr, null);
		return update();
	}

	protected boolean destroySlef() throws SQLException {
		DestroyWorker worker = Sql.destroy(this);
		worker.wheres().add(String.format("%s.%s = :_id",
				writerAdapter.quoteSchemaAndTableName(), writerAdapter.quotePrimaryKey()));
		worker.params().put("_id", getId());
		return writerAdapter.execute(Sql.sql(worker.getSql(), worker
				.params())) > 0;
	}
	
	@SuppressWarnings("unchecked")
	protected void recursion(String deleteOrDestroy) throws Exception {
		List<Map<String,Object>> deps = null;
		if(deleteOrDestroy.equals("delete"))
			deps = getDelete();
		else if(deleteOrDestroy.equals("destroy"))
			deps = getDestroy();
		
		String model = this.getClass().getSimpleName();	
		for (Map<String,Object> dep : deps) {
			if(dep != null){
				String depModel = Support.map(dep).keys().get(0).toString();
				Map<String,Object> depModelValues = (Map<String, Object>) dep.get(depModel);
				Map<String,Object> and = (Map<String, Object>) depModelValues.get("and");
				Map<String,Object> or = (Map<String, Object>) depModelValues.get("or");
				String defFk = Support.inflect(model).underscore() + "_id";
				String foreignKey = Support.object(depModelValues.get("foreign_key")).def(defFk).toString();
				String method = Support.object(depModelValues.get("method")).def("delete").toString();
				Query q = Query.from(g, depModel);
				q.and("eq_" + foreignKey,getId());
				q.and(and).or(or);
				List<ActiveRecord> list = q.find();				
				for (ActiveRecord a : list) {
					if(isUseTransaction()){
						a.useTransaction();
					}
					if (method.equals("delete")) {
						a.delete();
					} else if (method.equals("destroy")) {
						a.destroy();
					} else if (method.equals("clear")) {
						if(deleteOrDestroy.equals("delete"))
							a.deleteClearValue(foreignKey);
						
						if(deleteOrDestroy.equals("destroy"))
							a.destroyClearValue(foreignKey);
					}
				}
			}
		}
	}
	
	protected boolean deleteClearValue(String attr) throws Exception {
		if (getDelete() == null) {
			return clearValue(attr);
		} else {
			recursion("delete");
			return clearValue(attr);
		}
	}
	
	protected boolean destroyClearValue(String attr) throws Exception {
		if (getDestroy() == null) {
			return clearValue(attr);
		} else {
			recursion("destroy");
			return clearValue(attr);
		}
	}

	@Override
	public ActiveRecord clone() {
		try {
			ActiveRecord c = getClass().getConstructor(AbsGlobal.class).newInstance(getGlobal());
			c.putAll(this);
			c.putAllFindRecord(beforeRecord);
			c.updateValues.clear();
			c.updateValues.putAll(updateValues);
			c.setSaveAction(saveAction);
			return c;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return null;
		}
	}
	
	public void messages(String on) throws ConfigurException, MessagesException {
		messages(on, this);
	}

	public void messages() throws ConfigurException, MessagesException {
		messages(ON_SAVE, this);
	}

	public boolean onSave() throws ConfigurException, MessagesException,
			SQLException, ValidateException, TypeException {
		messages(saveAction, this);
		return save();
	}

	public boolean onCreate() throws ConfigurException, MessagesException, SQLException, ValidateException, TypeException {
		saveAction = ON_CREATE;
		messages(ON_CREATE, this);
		return save();
	}

	public boolean onUpdate() throws ConfigurException, MessagesException, SQLException, ValidateException, TypeException {
		saveAction = ON_UPDATE;
		messages(ON_UPDATE, this);
		return save();
	}
	
	public boolean save() throws SQLException, ValidateException,
			ConfigurException, TypeException {
		if (saveAction.equals(ON_CREATE))
			return create();
		else
			return update();
	}

	public boolean create() throws SQLException, ValidateException,
			ConfigurException, TypeException {
		List<String> attrs = getAttributes();
		saveAction = ON_CREATE;
		if(!valid)
			validates(ON_CREATE, this, attrs);

		if (attrs.contains("deleted"))
			put("deleted", false);
			
		if (attrs.contains("created_at"))
			put("created_at", new Timestamp(new java.util.Date().getTime()));
		
		if (attrs.contains("updated_at"))
			remove("updated_at");
		
		if (attrs.contains("deleted_at"))
			remove("deleted_at");

		if (attrs.contains("created_user_id"))
			put("created_user_id", g.getUserId());
		
		if (attrs.contains("updated_user_id"))
			remove("updated_user_id");
		
		if (attrs.contains("deleted_user_id"))
			remove("deleted_user_id");

		if(beforeCreate()){
			for(String attr : getAttributes()){
				put(attr,saveTrigger(attr,get(attr),null));
				put(attr,createTrigger(attr,get(attr)));
			}
			boolean b = writerAdapter.create(this);
			if(b)
				afterCreate();
			
			return b;
		}else
			return false;
	}

    public boolean update() throws SQLException, ValidateException,
			ConfigurException, TypeException {
		if(beforeUpdate()){
			List<String> keys = Support.map(this).keys();
			List<String> attrs = getAttributes();
			boolean updated = false;
		
			for (String key : keys) {
				if (!attrs.contains(key))
					continue;
	
				if (!Arrays.asList(writerAdapter.getPrimaryKey(),
						"created_user_id", "updated_user_id", "deleted_user_id",
						"created_at", "updated_at", "deleted_at", "deleted")
						.contains(key)) {
					Attribute a = getAttribute(key);
					Object ov = a.parse(beforeRecord.get(key));
					Object cv = get(key);
					Object pv = a.parse(cv);
	
					if (pv == null) {
						if (ov != null) {
							updated = true;
							pv = saveTrigger(key,pv,ov);
							pv = updateTrigger(key,pv,ov);						
							updateValues.put(key, pv);
						}
					} 
					else {
						if (!pv.equals(ov)) {
							updated = true;		
							pv = saveTrigger(key,pv,ov);
							pv = updateTrigger(key,pv,ov);						
							updateValues.put(key, pv);
						}
					}
				}
			}
		
			if (updated) {
				log.debug("Update Atts : {}",updateValues);			
				updateValues.remove("created_user_id");
				updateValues.remove("deleted_user_id");
				updateValues.remove("created_at");
				updateValues.remove("deleted_at");
		
				saveAction = ON_UPDATE;
				if(!valid){
					List<String> vkeys = Support.map(updateValues).keys();
					validates(ON_UPDATE, updateValues,vkeys);
				}
				if (attrs.contains("updated_at"))
					updateValues.put("updated_at",
							new Timestamp(new java.util.Date().getTime()));
	
				if (attrs.contains("updated_user_id"))
					updateValues.put("updated_user_id", g.getUserId());
	
				Object id = getId();			
				remove(writerAdapter.getPrimaryKey());
				UpdateWorker worker = Sql.update(this, updateValues);
				worker.wheres().add(String.format("%s.%s = :_id",
						writerAdapter.quoteSchemaAndTableName(),
						writerAdapter.quotePrimaryKey()));
				worker.params().put("_id", id);
			
				boolean b = writerAdapter.execute(Sql.sql(worker.getSql(), worker
						.params())) > 0;
				setId(id);
				if(b)
					afterUpdate();
				
				return b;
			}
			return false;
		} else {
			return false;
		}
	}
	
	protected Object saveTrigger(String attr,Object newValue,Object oldValue){
		return newValue;
	}
	
	protected Object createTrigger(String attr,Object newValue){
		return newValue;
	}
	
	protected Object updateTrigger(String attr,Object newValue,Object oldValue){
		return newValue;
	}
	
	protected boolean beforeCreate() {
		return true;
	}
	
	protected boolean beforeUpdate(){
		return true;
	}
	
	protected boolean beforeDelete(){
		return true;
	}
	
	protected boolean beforeDestroy(){
		return true;
	}
	
	protected void afterCreate(){
		
	}
	
	protected void afterUpdate(){
		
	}
	protected void afterDelete(){
		
	}
	
	protected void afterDestroy(){
		
	}

	public List<Map<String,Object>> getDelete() {
		return Support.config().getDelete().get(getClass().getSimpleName());
	}
	
	public List<Map<String,Object>> getDestroy() {
		return Support.config().getDestroy().get(getClass().getSimpleName());
	}

	public boolean destroy() throws Exception {
		boolean b = false;
		if(beforeDestroy()){
			if (getDestroy() == null) {
				b = destroySlef();
			} else {
				recursion("destroy");
				b = destroySlef();
			}
		}
		if(b)
			afterDestroy();
		
		return b;
	}

	public boolean delete() throws Exception {
		boolean b = false;
		if(containsKey("deleted") && isBoolean("deleted"))
			return false;
		
		if(beforeDelete()){
			if (getDelete() == null) {
				b = deleteSlef();
			} else {
				recursion("delete");
				b = deleteSlef();
			}
			if(b)
				afterDelete();
		}
		
		return b;
	}

	public <T extends ActiveRecord> T belongsTo(T t) throws SQLException {
		log.debug("belongsTo : {}",t.getClass().getSimpleName());
		HasWorker has = Sql.has();
		Query q = has.belongsTo(this,t.getClass().getSimpleName());
		return q.first();
	}
	
	public <T extends ActiveRecord> T belongsTo(String belongName) throws SQLException {
		log.debug("belongsTo : {}",belongName);
		HasWorker has = Sql.has();
		Query q = has.belongsTo(this,belongName);
		return q.first();
	}


	public <T extends ActiveRecord> T hasOne(T t) throws SQLException {
		log.debug("hasOne : {}",t.getClass().getSimpleName());
		HasWorker has = Sql.has();
		Query q = has.hasOne(this, t.getClass().getSimpleName());
		return q.first();
	}

	public <T extends ActiveRecord> T hasOne(String hasName) throws SQLException {
		log.debug("hasOne : {}",hasName);
		HasWorker has = Sql.has();
		Query q = has.hasOne(this,hasName);
		return q.first();
	}
	
	public <T extends ActiveRecord> List<T> hasMany(T t) throws SQLException {
		log.debug("hasMany : {}",t.getClass().getSimpleName());
		HasWorker has = Sql.has();
		Query q = has.hasMany(this, t.getClass().getSimpleName());
		return q.find();
	}

	public <T extends ActiveRecord> List<T> hasMany(String hasName) throws SQLException {
		log.debug("hasName : {}",hasName);
		HasWorker has = Sql.has();
		Query q = has.hasMany(this,hasName);
		return q.find();
	}

	public void refresh() throws SQLException, RecordNotFoundException {
		find(getId());
	}

	public List<String> getAttributes() {
		return readerAdapter.getColumnNames();
	}

	public Attribute getAttribute(String name) throws TypeException {
		return new Attribute(this, name);
	}

	public Adapter getReaderAdapter() {
		return readerAdapter;
	}

	public Adapter getWriterAdapter() {
		return writerAdapter;
	}

	public AbsGlobal getGlobal() {
		return g;
	}

	public void setId(Object id) {
		put(readerAdapter.getPrimaryKey(), id);
	}

	public Object getId() {
		return get(readerAdapter.getPrimaryKey());
	}
	
	public Boolean getBoolean(String attr){
		return isBoolean(attr);
	}
	
	public Boolean getBoolean(String attr,Boolean def){
		return isBoolean(attr, def);
	}

	public Boolean isBoolean(String attr) {
		return isBoolean(attr,false);
	}
	
	public Boolean isBoolean(String attr,Boolean def) {
		if (get(attr) == null)
			return def;

		if (get(attr) instanceof Boolean)
			return (Boolean) get(attr);
		else if (get(attr) instanceof Number)
			return ((Number) get(attr)).intValue() == 1;
		else
			return Boolean.parseBoolean(get(attr).toString());
	}

	public Timestamp getTimestamp(String attr) {
		if (get(attr) == null)
			return null;

		if (get(attr) instanceof java.util.Date)
			return new Timestamp(((java.util.Date) get(attr)).getTime());
		else if (get(attr) instanceof Date)
			return new Timestamp(((Date) get(attr)).getTime());
		else if (get(attr) instanceof Timestamp)
			return (Timestamp) get(attr);
		else if (get(attr) instanceof Number)
			return new Timestamp(((Number) get(attr)).longValue());
		else
			return Timestamp.valueOf(get(attr) + "");
	}

	public Date getDate(String attr) {
		if (get(attr) == null)
			return null;

		if (get(attr) instanceof Date)
			return (java.sql.Date) get(attr);
		else if (get(attr) instanceof java.util.Date) {
			return new Date(((java.util.Date) get(attr)).getTime());
		} else if (get(attr) instanceof Number)
			return new java.sql.Date(((Number) get(attr)).longValue());
		else
			return java.sql.Date.valueOf(get(attr) + "");
	}

	public Time getTime(String attr) {
		if (get(attr) == null)
			return null;

		if (get(attr) instanceof Time)
			return (Time) get(attr);
		else if (get(attr) instanceof Number)
			return new Time(((Number) get(attr)).longValue());
		else
			return Time.valueOf(get(attr) + "");
	}

	public Object getObject(String attr) {
		return getObject(attr,null);
	}
	
	public Object getObject(String attr,Object def) {
		if (get(attr) == null)
			return def;
		return get(attr);
	}
	
	public String getString(String attr) {
		return getString(attr,null);
	}
	
	public String getString(String attr,String def) {
		if (get(attr) == null)
			return def;
		return get(attr).toString();
	}

	public Number getNumber(String attr) {
		return getNumber(attr,null);
	}
	
	public Number getNumber(String attr,Number def) {
		Object v = getObject(attr);
		if (v == null)
			return def;
		if(v instanceof Number)
			return (Number) v;
		try{
			DecimalFormat df = new DecimalFormat();
			return df.parse(v.toString());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			return null;
		}
	}
	
	public Short getShort(String attr,Short def){
		return getNumber(attr,def).shortValue();
	}
	
	public Short getShort(String attr){
		Number v = getNumber(attr);
		if(v == null)
			return null;
		return v.shortValue();
	}
	
	public Byte getByte(String attr,Byte def){
		return getNumber(attr,def).byteValue();
	}
	
	public Byte getByte(String attr){
		Number v = getNumber(attr);
		if(v == null)
			return null;
		return v.byteValue();
	}
	
	public Integer getInteger(String attr,Integer def){
		return getNumber(attr,def).intValue();
	}
	
	public Integer getInteger(String attr){
		Number v = getNumber(attr);
		if(v == null)
			return null;
		return v.intValue();
	}
	
	public Long getLong(String attr,Long def){
		return getNumber(attr,def).longValue();
	}
	
	public Long getLong(String attr){
		Number v = getNumber(attr);
		if(v == null)
			return null;
		return v.longValue();
	}
	
	public Float getLong(String attr,Float def){
		return getNumber(attr,def).floatValue();
	}
	
	public Float getFloat(String attr){
		Number v = getNumber(attr);
		if(v == null)
			return null;
		return v.floatValue();
	}
	
	public Float getFloat(String attr,Float def){
		return getNumber(attr,def).floatValue();
	}
	
	public Double getDouble(String attr,Double def){
		return getNumber(attr,def).doubleValue();
	}
	
	public Double getDouble(String attr){
		Number v = getNumber(attr);
		if(v == null)
			return null;
		return v.doubleValue();
	}
	
	public BigInteger getBigInteger(String attr){
		return getBigInteger(attr,null);
	}
	
	public BigInteger getBigInteger(String attr,BigInteger def){
		if (get(attr) == null)
			return  def;
		return get(attr) instanceof BigInteger ? (BigInteger) get(attr) : new BigInteger(get(attr).toString().trim());
	}
	
	public BigDecimal getBigDecimal(String attr){
		return getBigDecimal(attr,null);
	}
	
	public BigDecimal getBigDecimal(String attr,BigDecimal def){
		if (get(attr) == null)
			return def;
		return get(attr) instanceof BigDecimal ? (BigDecimal) get(attr) : new BigDecimal(get(attr).toString().trim());
	}
	
	public void setSaveAction(String saveAction) {
		this.saveAction = saveAction;
	}
	
	public String getSaveAction() {
		return saveAction;
	}

	public Map<String, Object> getBeforeRecord() {
		return beforeRecord;
	}

	public Map<String,Object> getUpdateValues(){
		return updateValues;
	}

	public void putAllFindRecord(Map<String, Object> m) {
		beforeRecord = new IndexMap<String, Object>();
		final List<String> attrs = getAttributes();
		List<String> keys = Support.map(m).keys();
		for (String key : keys) {
			if (!attrs.contains(key))
				m.remove(key);
		}
		beforeRecord.putAll(m);
	}
	
	public Json<String,Object> toJson(){
		return new Json<String, Object>(this);
	}
	
	public void useTransaction(){
		writerAdapter.setAutoCommit(false);
	}
	
	public void nonuseTransaction(){
		writerAdapter.setAutoCommit(true);
	}
	
	public boolean isUseTransaction(){
		return !writerAdapter.isAutoCommit();
	}
	
	public void commit(){
		writerAdapter.commit();
	}
	
	public void rollback(){
		writerAdapter.rollback();
	}

	public static ActiveRecord eval(AbsGlobal g,String table){
		try{
			Class cls = Class.forName("app.model." + Support.inflect(table).camelcase());
			Constructor con = cls.getConstructor(AbsGlobal.class);
			return (ActiveRecord) con.newInstance(g);
		}catch(Exception e){
			LoggerFactory.getLogger(ActiveRecord.class).error(e.getMessage(),e);
			return null;
		}
	}
	
	public static ActiveRecord eval(AbsGlobal g,String table,Object id){
		try{
			Class cls = Class.forName("app.model." + Support.inflect(table).camelcase());
			Constructor con = cls.getConstructor(AbsGlobal.class,Object.class);
			return (ActiveRecord) con.newInstance(g,id);
		}catch(Exception e){
			LoggerFactory.getLogger(ActiveRecord.class).error(e.getMessage(),e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends ActiveRecord> List<T> find(T t, SqlWorker sql)
			throws SQLException {
		DBResource database = new DBResource(t.getGlobal(),t.getClass().getSimpleName(),DBResource.READER);
		Adapter adapter = database.getAdapter();
		List<T> list = new ArrayList<T>();
		List<Map<String, Object>> ls = adapter.find(sql);
		for (Map<String, Object> m : ls) {
			t.clear();
			t.putAll(t.findFilter(m));
			t.putAllFindRecord(t.clone());
			t.setSaveAction(ON_UPDATE);
			list.add((T)t.clone());		
		}
		return list;
	}
	
	public static <T extends ActiveRecord> List<T> find(T t, FindWorker worker)
			throws SQLException {
		SqlWorker sql = Sql.sql(worker.getSql(), worker.params());
		sql.setCached(worker.isCached());
		sql.setCacheSecond(worker.getCacheSecond());
		sql.setCacheName(worker.getCacheName());
		sql.setCacheForced(worker.isCacheForced());		
		return find(t, sql);
	}

	public static <T extends ActiveRecord> T first(T t, FindWorker worker)
			throws SQLException {		
		SqlWorker sql = Sql.sql(worker.getSql(), worker.params());
		sql.setCached(worker.isCached());
		sql.setCacheName(worker.getCacheName());
		sql.setCacheSecond(worker.getCacheSecond());
		sql.setCacheForced(worker.isCacheForced());
		sql.setMaxRows(1);
		t.setSaveAction(ON_UPDATE);		
		List<T> list = find(t,sql);
		if (list.size() == 0){
			return null;
		}else{
			return list.get(0);
		}
	}

	public static <T extends ActiveRecord> int[] createBatch(List<T> batchs) throws SQLException {		
		return batchs.get(0).getWriterAdapter().create(batchs);
	}
	
}
