package net.rails.active_record;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.rails.cache.Cache;
import net.rails.ext.IndexMap;
import net.rails.ext.Json;
import net.rails.log.LogPoint;
import net.rails.sql.Sql;
import net.rails.sql.worker.CreateWorker;
import net.rails.sql.worker.SqlWorker;
import net.rails.support.Support;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@SuppressWarnings("unchecked")
public class Adapter {
	
	private Logger log = LoggerFactory.getLogger(Adapter.class);	

	public static Map<String,DataSource> DATASOURCES;
	public static Map<String,String> MODEL_DATASOURC_MAPS; 
	
	protected String model;
	protected String tableName;
	protected String primaryKey;
	protected String schema;
	protected Boolean pluralized = false;
	protected String prefix;
	protected Map<String,Object> modcnf;	
	protected Map<String,Object> dbcnf;
	protected boolean autoCommit = true;
	protected Long threadId;
	
	protected static Map<String,Map<String,String>> COLUMN_TYPES;
	protected static Map<String,Map<String,String>> COLUMN_CLASSES;
	protected static Map<String,List<String>> COLUMN_NAMES;
	protected static Map<Long,Connection> CONNECTIONS;
	
	static{
		DATASOURCES = new HashMap<String,DataSource>();
		MODEL_DATASOURC_MAPS = new HashMap<String,String>(); 
		COLUMN_TYPES = new IndexMap<String,Map<String,String>>();
		COLUMN_CLASSES = new IndexMap<String,Map<String,String>>();
		COLUMN_NAMES = new IndexMap<String,List<String>>();
		CONNECTIONS = new HashMap<Long,Connection>();
	}
	
	public Adapter(Map<String,Object> dbcnf,String model) {
		super();
		this.dbcnf = dbcnf;
		this.model = model;
		init();		
	}
	
	private void init() {
		initDataSource();
		threadId = Thread.currentThread().getId();
		modcnf = Support.config().getModels().get(model);
		schema = Support.map(dbcnf).get("schema","");
		pluralized = Support.map(dbcnf).get("pluralized",false);
		prefix = Support.map(dbcnf).get("prefix","");
		String tname = null;
		if(pluralized){
		   tname = Support.inflect(Support.inflect(model).underscore()).pluralize();
		}else{
		   tname = Support.inflect(model).underscore();
		}
		tableName = prefix + tname;
		if(modcnf == null){
			primaryKey = "id";
		}else{
			tableName = Support.map(modcnf).get("table_name",tableName);
			primaryKey = Support.map(modcnf).get("primary_key","id");
		}
	}
	
	protected void bindValues(PreparedStatement statement,List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			if (params.get(i) != null){
				Object v = params.get(i);
				if(v instanceof Timestamp){
					String frm = (String)Support.map(dbcnf).gets("formats","datetime");
					frm = Support.string(frm).def("yyyy-MM-dd HH:mm:ss.SSS");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else if(v instanceof java.sql.Date){
					String frm = (String)Support.map(dbcnf).gets("formats","date");
					frm = Support.string(frm).def("yyyy-MM-dd");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else if(v instanceof java.sql.Time){
					String frm = (String)Support.map(dbcnf).gets("formats","time");
					frm = Support.string(frm).def("HH:mm:ss.SSS");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else
					statement.setObject(i + 1,v);
			}else
				statement.setNull(i + 1, Types.VARCHAR);
		}
	}	
	
	public synchronized List<Map<String,Object>> syncFind(SqlWorker sql) throws SQLException{
		LogPoint.markSqlCache();
		String cacheName = sql.getCacheName();		
		if(sql.getCacheName() == null){
			cacheName = Support.code().md5(sql.getSql() + sql.getParams() + sql.getMaxRows());
		}		
		if(sql.isCacheForced()){
			Cache.remove(cacheName);
		}
		List<Map<String, Object>> list = null;
		if(Cache.included(cacheName)){
			list = (List<Map<String, Object>>) Cache.get(cacheName);
			if(list == null){
				list =  findSql(sql);
				Cache.set(cacheName,list,sql.getCacheSecond());	
				LogPoint.markSqlRows();
				log.info("Records: {}",list.size());
				LogPoint.markSqlResult();
				log.debug("Results: {}",list);
				LogPoint.unmark();
				return list;
			}else{
				LogPoint.markSqlRows();
				log.info("Records: {}",list.size());
				LogPoint.markSqlResult();
				log.debug("Results: {}",list);
				LogPoint.unmark();
				return list;
			}
		}else{
		    list = findSql(sql);
		    if(sql.isCached()){
		      Cache.set(cacheName,list,sql.getCacheSecond());
		    }
			return list;
		}
	}
	
	public List<Map<String,Object>> find(SqlWorker sql) throws SQLException{	
		if(sql.isCached()){
			return syncFind(sql);
		}else
			return findSql(sql);
	}
	
	private List<Map<String,Object>> findSql(SqlWorker sql) throws SQLException{
		LogPoint.markSql();
		Map<String, Object> row = null;
		ResultSet result = null;
		PreparedStatement statement = null;
		Connection connection = null;		
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		try{
			log.info("Find SQL By DB: {}",sql.getSql());
			log.info("SQL Params: {}",sql.getParams());	
		    connection = openQueryConnection();
			statement = connection.prepareStatement(sql.getSql());
			if(sql.getMaxRows() > 0){
				statement.setMaxRows(sql.getMaxRows());
			}
			bindValues(statement,sql.getParams());
			result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			final int c = rsmd.getColumnCount();
			while (result.next()) {
				row = new HashMap<String, Object>();
				for (int i = 0; i < c; i++) {
					row.put((rsmd.getColumnLabel(i + 1)).toLowerCase(), result.getObject(i + 1));
				}
				rows.add(row);
			}
			LogPoint.markSqlRows();
			log.info("Records: {}",rows.size());
			LogPoint.markSqlResult();
			log.debug("Results: {}",rows);
		    return rows;
		}finally{
			LogPoint.unmark();
			closeQueryConnection(result,statement,connection);
		}
	}
	
	public boolean create(ActiveRecord record) throws SQLException{
		LogPoint.markSql();
		PreparedStatement statement = null;
		Connection connection = null;
		ResultSet rs = null;
		try{
			CreateWorker cw = Sql.create(record);
			SqlWorker sql = Sql.sql(cw.getSql(),cw.params());
			log.info("Execute SQL: {}",sql.getSql());
			log.info("SQL Params: {}",sql.getParams());
			connection = open();
			statement = connection.prepareStatement(sql.getSql(),new String[] {record.getWriterAdapter().getPrimaryKey()});
			bindValues(statement,sql.getParams());
			final int r = statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			if(rs.next()) {
				record.setId(rs.getObject(1));
			}
			rs.close();
			log.info("Updated: {}",r);
			return r > 0;
		}catch(SQLException e){
			throw e;
		}finally{
			LogPoint.unmark();
			closeConnection(statement,connection);
		}
	}
	
	public <T extends ActiveRecord> int[] create(List<T> records) throws SQLException{
		LogPoint.markSql();
		boolean first = true;
		PreparedStatement statement = null;
		Connection connection = null;
		try{			
			Timestamp curr = new Timestamp(new Date().getTime());
			for(ActiveRecord record : records){
				record.put("created_at", curr);
				record.put("created_user_id",record.getGlobal().getUserId());
				record.put("deleted", false);				
				CreateWorker cw = Sql.create(record);
				SqlWorker sql = Sql.sql(cw.getSql(),cw.params());	
				if(first){		
					connection = open();
					statement = connection.prepareStatement(sql.getSql(),new String[] {record.getWriterAdapter().getPrimaryKey()});
					first = false;
				}
				log.info("Execute SQL: {}",sql.getSql());
				log.info("SQL Params: {}",sql.getParams());
				bindValues(statement,sql.getParams());
				statement.addBatch();
			}
			if(records.size() > 0){
				return statement.executeBatch();
			}else{
				return null;
			}
		}catch(SQLException e){
			throw e;
		}finally{
			LogPoint.unmark();
			closeConnection(statement,connection);
		}
	}
	
	public int execute(SqlWorker sql) throws SQLException{
		LogPoint.markSql();
		PreparedStatement statement = null;
		Connection connection = null;
		log.info("Execute SQL: {}",sql.getSql());
		log.info("SQL Params: {}",sql.getParams());
		try{
			connection = open();
			statement = connection.prepareStatement(sql.getSql());
			bindValues(statement,sql.getParams());
			int r = statement.executeUpdate();
			log.info("Updated: {}",r);
			return r;
		}catch(SQLException e){
			throw e;
		}finally{
			LogPoint.unmark();
			closeConnection(statement,connection);
		}
	}
	
	public List<String> getColumnNames() {
		ResultSet result = null;
		PreparedStatement statement = null;
		Connection connection = null;
		if(COLUMN_NAMES.containsKey(model)){
			return new ArrayList<String>(COLUMN_NAMES.get(model));
		}
		log.debug("Get Column names for {}", model);	
		try{
			connection = openQueryConnection();
			statement = connection.prepareStatement("SELECT * FROM " + quoteSchemaAndTableName() + " WHERE 1 = 0");
			final List<String> list = new ArrayList<String>();
			result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				list.add(rsmd.getColumnLabel(i + 1).toLowerCase().trim());
			}
			result.close();
			COLUMN_NAMES.put(model,list);
			return getColumnNames();
		}catch(SQLException e){
			log.error( e.getMessage(),e);
			return null;
		}finally{
			closeQueryConnection(result, statement, connection);
		}		
	}
	
	public Map<String,String> getColumnClasses() {	
		ResultSet result = null;
		PreparedStatement statement = null;
		Connection connection = null;
		if(COLUMN_CLASSES.containsKey(model))
			return new HashMap<String,String>(COLUMN_CLASSES.get(model));
		
		log.debug("Get column classes for {}",model);
		final Map<String,String> types = new HashMap<String,String>();
		try{
			connection = openQueryConnection();
			statement = connection.prepareStatement("SELECT * FROM " + quoteSchemaAndTableName() + " WHERE 1 = 0");
			result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				String cn = rsmd.getColumnClassName(i + 1).replaceFirst("java.lang.","");
				types.put(rsmd.getColumnLabel(i + 1).toLowerCase(),cn);
			}
			COLUMN_CLASSES.put(model,types);
			return getColumnClasses();
		}catch(SQLException e){
			log.error(e.getMessage(),e);
			return null;
		}finally{
			closeQueryConnection(result,statement,connection);
		}
	}
	
	public Map<String,String> getColumnTypes() {
		ResultSet result = null;
		PreparedStatement statement = null;
		Connection connection = null;
		if(COLUMN_TYPES.containsKey(model))
			return new HashMap<String,String>(COLUMN_TYPES.get(model));
		
		log.debug("Get column types for {}",model);
		final Map<String,String> types = new HashMap<String,String>();
		try{
			connection = openQueryConnection();
			statement = connection.prepareStatement("SELECT * FROM " + quoteSchemaAndTableName() + " WHERE 1 = 0");
			result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				types.put(rsmd.getColumnLabel(i + 1), rsmd.getColumnTypeName(i + 1));
			}
			COLUMN_TYPES.put(model,types);
			return getColumnTypes();
		}catch(SQLException e){
			log.error(e.getMessage(),e);
			return null;
		}finally{
			closeQueryConnection(result,statement,connection);
		}
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public String getPrimaryKey(){
		return primaryKey;
	}	
	
	public String getSchema(){
		return schema;
	}

	public Boolean isPluralized(){
		return pluralized;
	}
	
	public String getPrefix(){
		return prefix;
	}
	
	public String quotePrimaryKey(){
		return  quote(getPrimaryKey());
	}
	
	public String quote(String keyword){
		String symbol = Support.map(dbcnf).get("quote","keyword");
		return symbol.replace("keyword", keyword);
	}
	
	public String quoteTableName(){
		return quote(getTableName());
	}
	
	public String quoteSchemaDot(){
		if(Support.string(schema).blank()){
			return "";
		}else{
			return quote(getSchema()) + ".";
		}
	}
	
	public String quoteSchema(){
		return quote(getSchema());
	}
	
	public String quoteSchemaAndTableName(){
		return quoteSchemaDot() + quoteTableName();
	}

	public Map<String,Object> getDbcnf(){
		return dbcnf;
	}
	
	public String getModel(){
		return model;
	}
	
	public DataSource getDataSource(){
		return DATASOURCES.get(MODEL_DATASOURC_MAPS.get(model));
	}
	
	public void setAutoCommit(boolean autoCommit){
		this.autoCommit = autoCommit;
	}
	
    public boolean isAutoCommit(){
		return autoCommit;
	}
	
	private Connection open() throws SQLException{
		LogPoint.markSqlConn();
		Connection connection = null;
		if(isAutoCommit()){
			log.debug("Open Connection");
			LogPoint.unmark();
			connection = getDataSource().getConnection();
			connection.setAutoCommit(isAutoCommit());
			return connection;
		}else{
			if(CONNECTIONS.containsKey(threadId)){
				log.debug("Get Transaction Connection");
				LogPoint.unmark();
				return CONNECTIONS.get(threadId);
			}else{
				log.debug("Open Transaction Connection");
				connection = getDataSource().getConnection();
				connection.setAutoCommit(isAutoCommit());
				CONNECTIONS.put(threadId, connection);
				LogPoint.unmark();
				return CONNECTIONS.get(threadId);
			}
		}
	}
	
	private Connection openQueryConnection() throws SQLException{		
		LogPoint.markSqlConn();
		log.debug("Open Qyery Connection");
		LogPoint.unmark();
		return getDataSource().getConnection();
	}
	
	private void closeQueryConnection(ResultSet result,PreparedStatement statement,Connection connection) {
		LogPoint.markSqlConn();
		try{
			if (result != null){
				log.debug("Close Query Result");
				result.close();
			}
			if (statement != null){
				log.debug("Close Query Statement");
				statement.close();
			}
			if (connection != null){
				log.debug("Close Query Connection");
				connection.close();
			}
		}catch(SQLException e){
			log.error(e.getMessage(),e);
		}finally{
			LogPoint.unmark();
		}
	}
	
	private void closeConnection(PreparedStatement statement,Connection connection) {
		LogPoint.markSqlConn();
		if(isAutoCommit()){
			try{
				if (statement != null){
					log.debug("Close Statement");
					statement.close();
				}
				if (connection != null){
					log.debug("Close Connection");
					connection.close();
				}
			}catch(SQLException e){
				log.error(e.getMessage(),e);
			}
		}
		LogPoint.unmark();
	}
	
	private void closeTransactionConnection() {
		LogPoint.markSqlConn();
		Connection connection = CONNECTIONS.get(threadId);
		try{
			if (connection != null){
				log.debug("Close Transaction Connection");
				connection.close();
			}
		}catch(SQLException e){
			log.error(e.getMessage(),e);
		}finally{
			LogPoint.unmark();
			CONNECTIONS.remove(threadId);
		}
	}
	
	public void rollback() {
		LogPoint.markSqlConn();
		Connection connection = CONNECTIONS.get(threadId);
		try{
			if (connection != null){
				log.info("Rollback Connection");
				connection.rollback();
			}
		}catch(SQLException e){
			log.error(e.getMessage(),e);
		}finally{
			LogPoint.unmark();
			closeTransactionConnection();
		}
	}
	
	public void commit() {
		LogPoint.markSqlConn();
		Connection connection = CONNECTIONS.get(threadId);
		try{
			if (connection != null){
				log.info("Commit Connection");
				connection.commit();
			}
		}catch(SQLException e){
			log.error(e.getMessage(),e);
		}finally{
			LogPoint.unmark();
			closeTransactionConnection();
		}
	}
		
	public synchronized static void cleaupDataSource() throws Exception{
		List<String> dsKeys = Support.map(DATASOURCES).keys();
		for (Iterator<String> iterator = dsKeys.iterator(); iterator.hasNext();) {
			DataSource ds = DATASOURCES.get(iterator.next());
			Class c = ds.getClass();
			Method mtd = c.getMethod("close");
			mtd.invoke(ds);
		}		
		DATASOURCES.clear();
		MODEL_DATASOURC_MAPS.clear();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while(drivers.hasMoreElements()){
			Driver driver = drivers.nextElement();
			DriverManager.deregisterDriver(driver);
		}
	}
	
	private synchronized void initDataSource() {
		LogPoint.markSqlConn();
		DataSource dataSource = null;
		try {
			if(!MODEL_DATASOURC_MAPS.containsKey(model)){
				String dsKey = Support.code().md5(new Json(dbcnf).generate());
				if(DATASOURCES.containsKey(dsKey)){
					MODEL_DATASOURC_MAPS.put(model, dsKey);
				}else{
					if(dbcnf.containsKey("jndi")){
						Context cxt = new InitialContext();
					    dataSource = (DataSource)cxt.lookup(dbcnf.get("jndi").toString());
					}else{
						final Class<DataSource> c = (Class<DataSource>) Class.forName(dbcnf.get("datasource").toString());
						dataSource = c.newInstance();
						Method mtd = c.getDeclaredMethod("setDriverClassName", String.class);
						mtd.invoke(dataSource,dbcnf.get("driver"));
						mtd = c.getDeclaredMethod("setUrl", String.class);
						mtd.invoke(dataSource,dbcnf.get("url")); 
						mtd = c.getDeclaredMethod("setUsername", String.class);
						mtd.invoke(dataSource, dbcnf.get("username"));
						mtd = c.getDeclaredMethod("setPassword", String.class);
						mtd.invoke(dataSource,dbcnf.get("password"));
					}
					MODEL_DATASOURC_MAPS.put(model, dsKey);
					DATASOURCES.put(dsKey, dataSource);
				}
			}
		}catch (Exception e) {
			log.error(e.getMessage(),e);
		}finally{
			LogPoint.unmark();
		}
	}
	
}