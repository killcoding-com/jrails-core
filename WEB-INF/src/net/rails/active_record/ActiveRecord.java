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

/**
 * Mapping for database records.
 * <h5>Defaults:</h5>
 * <p>Sub class only in package "app.model".</p>
 * <p>Sub class name example: (e.g. table_name --> TableName).</p>
 */
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
	
    /**
     * Construct an ActiveRecord object.
     * @param g
     */
	public ActiveRecord(AbsGlobal g) {
		super();
		log = LoggerFactory.getLogger(getClass());
		this.g = g;
		this.saveAction = ON_CREATE;
		initAdapter();
	}

	/**
	 * Construct an ActiveRecord object from table a record.
	 * @param g
	 * @param id The record id.
	 * @throws SQLException
	 * @throws RecordNotFoundException Table no exist record.
	 */
	public ActiveRecord(AbsGlobal g, Object id) throws SQLException,
			RecordNotFoundException {
		super();
		log = LoggerFactory.getLogger(getClass());
		this.g = g;
		this.saveAction = ON_UPDATE;
		initAdapter();
		find(id);
	}

	//Begin protected methods
	/**
	 * Validate the object attributes values.
	 * @param on See the class defines ActiveRecord.ON_SAVE、ActiveRecord.ON_CREATE、ActiveRecord.ON_UPDATE.
	 * @param values Mapping keys and names.
	 * @throws ConfigurException Load failure in config/models/*.yml.
	 * @throws MessagesException Validate results.
	 */
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



	/**
	 * Attributes validates
	 * @param on values only is ActiveRecord.ON_SAVE,ActiveRecord.ON_CREATE,ActiveRecord.ON_UPDATE
	 * @param values 验证内容
	 * @param attrs 验证属性
	 * @throws ValidateException 数据验证失败抛出
	 * @throws ConfigurException 配置内容错误抛出
	 * @throws TypeException 数据类型错误抛出
	 */
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


	
	/**
	 * 重写这方法将可改变查询结果的值。
	 * @param map 单行数据
	 * @return
	 */
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
	//End protected methods
	
	//Begin public methods
	/**
	 * Clone all values of the same active record(NonuseTransaction).
	 * @return New ActiveRecord object.
	 */
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
	
	/**
	 * Validate attributes input value.
	 * <p><b>Defaults:</b></P>
	 * <p>values: Current attribute values.</p>
	 * @param on See the class defines ActiveRecord.ON_SAVE、ActiveRecord.ON_CREATE、ActiveRecord.ON_UPDATE.
	 * @throws ConfigurException Load failure in config/models/*.yml .
	 * @throws MessagesException Validate failed.
	 */
	public void messages(String on) throws ConfigurException, MessagesException {
		messages(on, this);
	}
	
	/**
	 * Validate attributes input value.
	 * <p><b>Defaults:</b></P>
	 * <p>on: ON_SAVE.</p>
	 * <p>values: Current attribute values.</p>
	 * @throws ConfigurException Load failure in config/models/*.yml .
	 * @throws MessagesException Validate failed.
	 */
	public void messages() throws ConfigurException, MessagesException {
		messages(ON_SAVE, this);
	}

	/**
	 * Save changed.
	 * @return Return true if successful save else return false.
	 * @throws ConfigurException Load failure in config/models/*.yml .
	 * @throws MessagesException Validate failed.
	 * @throws SQLException
	 * @throws ValidateException Input values validate failed.
	 * @throws TypeException Input values data type failed.
	 */
	public boolean onSave() throws ConfigurException, MessagesException,
			SQLException, ValidateException, TypeException {
		messages(saveAction, this);
		return save();
	}

	/**
	 * <p>Execute SQL "INSERT INTO ...".</p>
	 * <p>Validate by "config/models/modelName.yml".</p>
	 * @return Success return true else return false.
	 * @throws ConfigurException Load yml failed.
	 * @throws MessagesException Validate all values failed.
	 * @throws SQLException
	 * @throws ValidateException Validate first value failed.
	 * @throws TypeException Attribyte data type failed.
	 */
	public boolean onCreate() throws ConfigurException, MessagesException, SQLException, ValidateException, TypeException {
		saveAction = ON_CREATE;
		messages(ON_CREATE, this);
		return save();
	}

	/**
	 * Execute update the record, trigger validate on after update.
	 * Validate spec define in config/models/modelName.yml.
	 * @return Return true on success, else false. 
	 * @throws ConfigurException Validate config invalid.
	 * @throws MessagesException For each attributes validate error messages.
	 * @throws SQLException Connection or execute sql invalid
	 * @throws ValidateException Record invalid.
	 * @throws TypeException Attributes data type invalid. 
	 */
	public boolean onUpdate() throws ConfigurException, MessagesException, SQLException, ValidateException, TypeException {
		saveAction = ON_UPDATE;
		messages(ON_UPDATE, this);
		return save();
	}
	
	/**
	 * 插入或者更新数据。
	 * @return 执行成功返回true否则返回false
	 * @throws SQLException
	 * @throws ValidateException 数据验证失败抛出
	 * @throws ConfigurException 配置内容错误抛出
	 * @throws TypeException 数据类型错误抛出
	 */
	public boolean save() throws SQLException, ValidateException,
			ConfigurException, TypeException {
		if (saveAction.equals(ON_CREATE))
			return create();
		else
			return update();
	}

	/**
	 * 执行插入数据。
	 * @return 执行成功返回true否则返回false
	 * @throws SQLException
	 * @throws ValidateException 数据验证失败抛出
	 * @throws ConfigurException 配置内容错误抛出
	 * @throws TypeException 数据类型错误抛出
	 */
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

	
	/**
	 * 执行更新数据。
	 * @return 执行成功返回true否则返回false
	 * @throws SQLException
	 * @throws ValidateException 数据验证失败抛出
	 * @throws ConfigurException 配置内容错误抛出
	 * @throws TypeException 数据类型错误抛出
	 */
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
	
	/**
	 * 保存触发器
	 * @param newValue
	 * @param oldValue
	 */
	protected Object saveTrigger(String attr,Object newValue,Object oldValue){
		return newValue;
	}
	
	/**
	 * 创建触发器
	 * @param newValue
	 * @param oldValue
	 */
	protected Object createTrigger(String attr,Object newValue){
		return newValue;
	}
	
	/**
	 * 更新触发器
	 * @param newValue
	 * @param oldValue
	 */
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

	/**
	 * 获取config/models/modelName.yml,dependent配置内容
	 * @return List<Map<String,Object>>
	 */
	public List<Map<String,Object>> getDelete() {
		return Support.config().getDelete().get(getClass().getSimpleName());
	}
	
	public List<Map<String,Object>> getDestroy() {
		return Support.config().getDestroy().get(getClass().getSimpleName());
	}


	/**
	 * 物理删除数据；
	 * 若config/models/Model.yml配置了dependent内容，将会引起循环去处理依赖模型。
	 * @return 执行成功返回true否则返回false
	 * @throws Exception
	 */
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

	/**
	 * 软删除方法，将deleted字段标识成true状态；
	 * 若config/models/modelName.yml配置了dependent内容，将会引起循环去处理依赖模型。
	 * @return 执行成功返回true否则返回false
	 * @throws Exception
	 */
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

	/**
	 * 查询关联对象列表(一对一关系)，child.belongsTo(parent) 将返回一个parent；
	 * @param t ActiveRecord关联模型
	 * @return <T extends ActiveRecord> T 关联模型实例
	 * @throws SQLException
	 */
	public <T extends ActiveRecord> T belongsTo(T t) throws SQLException {
		log.debug("belongsTo : {}",t.getClass().getSimpleName());
		HasWorker has = Sql.has();
		Query q = has.belongsTo(this,t.getClass().getSimpleName());
		return q.first();
	}
	
	/**
	 * 查询关联对象列表(一对一关系)，child.belongsTo(parent) 将返回一个parent
	 * @param hasName 自定义名称，配置文件必须指定 classify
	 * @return
	 * @throws SQLException
	 */
	public <T extends ActiveRecord> T belongsTo(String belongName) throws SQLException {
		log.debug("belongsTo : {}",belongName);
		HasWorker has = Sql.has();
		Query q = has.belongsTo(this,belongName);
		return q.first();
	}


	/**
	 * 查询关联对象列表(一对一关系)，例如： parent.hasOne(child) 这方法将返回一个child
	 * @param t ActiveRecord关联模型
	 * @return <T extends ActiveRecord> T 关联模型实例
	 * @throws SQLException
	 */
	public <T extends ActiveRecord> T hasOne(T t) throws SQLException {
		log.debug("hasOne : {}",t.getClass().getSimpleName());
		HasWorker has = Sql.has();
		Query q = has.hasOne(this, t.getClass().getSimpleName());
		return q.first();
	}

	/**
	 * 查询关联对象列表(一对一关系)，例如： parent.hasOne(child) 这方法将返回一个child
	 * @param hasName 自定义名称，配置文件必须指定 classify
	 * @return
	 * @throws SQLException
	 */
	public <T extends ActiveRecord> T hasOne(String hasName) throws SQLException {
		log.debug("hasOne : {}",hasName);
		HasWorker has = Sql.has();
		Query q = has.hasOne(this,hasName);
		return q.first();
	}
	
	/**
	 * 查询关联对象列表(一对一关系)，例如： parent.hasMany(child) 这方法将返回多个child
	 * @param t ActiveRecord 关联模型
	 * @return <T extends ActiveRecord> List<T> 关联模型实例
	 * @throws SQLException
	 */
	public <T extends ActiveRecord> List<T> hasMany(T t) throws SQLException {
		log.debug("hasMany : {}",t.getClass().getSimpleName());
		HasWorker has = Sql.has();
		Query q = has.hasMany(this, t.getClass().getSimpleName());
		return q.find();
	}

	/**
	 * 查询关联对象列表(一对一关系)，例如： parent.hasMany(child) 这方法将返回多个child
	 * @param hasName 自定义名称，配置文件必须指定 classify
	 * @return <T extends ActiveRecord> List<T> 关联模型实例
	 * @throws SQLException
	 */
	public <T extends ActiveRecord> List<T> hasMany(String hasName) throws SQLException {
		log.debug("hasName : {}",hasName);
		HasWorker has = Sql.has();
		Query q = has.hasMany(this,hasName);
		return q.find();
	}

	/**
	 * 执行查询重新刷新值。
	 * @throws SQLException
	 * @throws RecordNotFoundException 
	 */
	public void refresh() throws SQLException, RecordNotFoundException {
		find(getId());
	}

	/**
	 * 获取模型的属性列表。
	 * @return List<String>
	 */
	public List<String> getAttributes() {
		return readerAdapter.getColumnNames();
	}

	/**
	 * 获取一个属性。
	 * @param name 属性名称
	 * @return Attribute
	 * @throws TypeException
	 */
	public Attribute getAttribute(String name) throws TypeException {
		return new Attribute(this, name);
	}

	/**
	 * 如果你的config/database.yml文件是配置了读写分离，这方法可获取读数据时的数据库适配器；
	 * 若没有读写分离配置则返回标准的数据库适配器。
	 * @return Adapter
	 */
	public Adapter getReaderAdapter() {
		return readerAdapter;
	}

	/**
	 * 如果你的config/database.yml文件是配置了读写分离，这方法可获取写数据时的数据库适配器；
	 * 若没有读写分离配置则返回标准的数据库适配器。
	 * @return Adapter
	 */
	public Adapter getWriterAdapter() {
		return writerAdapter;
	}

	/**
	 * 获取一个全局配置对像。
	 * @return AbsGlobal
	 */
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
	
	/**
	 * 设置调用save方法时应该执行的动作。
	 * @param saveAction
	 */
	public void setSaveAction(String saveAction) {
		this.saveAction = saveAction;
	}
	
	/**
	 * 获取调用save方法时应该执行的动作。
	 * @return String
	 */
	public String getSaveAction() {
		return saveAction;
	}

	/**
	 * 获取记录find(id)的单个查询结果。
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getBeforeRecord() {
		return beforeRecord;
	}
	
	/**
	 * 获取被更新值Map。
	 * @return Map<String,Object>
	 */
	public Map<String,Object> getUpdateValues(){
		return updateValues;
	}

	/**
	 * 用作记录查询结果用作更新值比较，通常用在框架内部。
	 * @param m 单个查询结果
	 */
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
	//End public methods
	
	//Begin static methods
	/**
	 * Construct a defalut ActiveRecord of table name.
	 * @param g is a AbsGlobal extend object.
	 * @param table Table name.
	 * @return ActiveRecord object.
	 */
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
	
	/**
	 * Construct a query result of ActiveRecord.
	 * <p><b>Run script:</b> SELECT * FROM table_name WHERE id = 'params id'.</p>
	 * @param g is a AbsGlobal extend object.
	 * @param table Table name.
	 * @param id of data record.
	 * @return ActiveRecord object.
	 */
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

	/**
	 * Execute "Select sql" function.
	 * @param t is a ActiveRecord of sub class object.
	 * @param sql SqlWorker object.
	 * @return List<T>.
	 */ 
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
	
	/**
	 * Execute "Select sql" function.
	 * @param t is an ActiveRecord of sub class object.
	 * @param worker FindWorker.
	 * @return List<T>.
	 */ 
	public static <T extends ActiveRecord> List<T> find(T t, FindWorker worker)
			throws SQLException {
		SqlWorker sql = Sql.sql(worker.getSql(), worker.params());
		sql.setCached(worker.isCached());
		sql.setCacheSecond(worker.getCacheSecond());
		sql.setCacheName(worker.getCacheName());
		sql.setCacheForced(worker.isCacheForced());		
		return find(t, sql);
	}

	/**
	 * Execute "Select sql" function.
	 * @param t is an ActiveRecord of sub class object.
	 * @param worker FindWorker
	 * @return T.
	 */
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
	
    //End static methods
	
	//Begin private methods

	//End private methods
	
}
