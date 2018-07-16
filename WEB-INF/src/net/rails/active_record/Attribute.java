package net.rails.active_record;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.active_record.validate.BooleanValidate;
import net.rails.active_record.validate.DateValidate;
import net.rails.active_record.validate.ExclusionValidate;
import net.rails.active_record.validate.FormatValidate;
import net.rails.active_record.validate.InclusionValidate;
import net.rails.active_record.validate.LengthValidate;
import net.rails.active_record.validate.NumberValidate;
import net.rails.active_record.validate.PresenceValidate;
import net.rails.active_record.validate.TimeValidate;
import net.rails.active_record.validate.TimestampValidate;
import net.rails.active_record.validate.TypeException;
import net.rails.active_record.validate.UniquenessValidate;
import net.rails.active_record.validate.Validate;
import net.rails.active_record.validate.exception.ConfigurException;
import net.rails.ext.AbsGlobal;
import net.rails.support.Support;

@SuppressWarnings("unchecked")
public final class Attribute {
	
	private final static List<Object[]> VALIDATES = new ArrayList<Object[]>();	
	static{
		VALIDATES.add(new Object[]{"validates_presence_of", PresenceValidate.class});
		VALIDATES.add(new Object[]{"validates_length_of", LengthValidate.class});
		VALIDATES.add(new Object[]{"validates_format_of", FormatValidate.class});
		VALIDATES.add(new Object[]{"validates_boolean_of", BooleanValidate.class});
		VALIDATES.add(new Object[]{"validates_inclusion_of", InclusionValidate.class});	
		VALIDATES.add(new Object[]{"validates_exclusion_of", ExclusionValidate.class});
		VALIDATES.add(new Object[]{"validates_number_of", NumberValidate.class});		
		VALIDATES.add(new Object[]{"validates_date_of", DateValidate.class});
		VALIDATES.add(new Object[]{"validates_timestamp_of", TimestampValidate.class});
		VALIDATES.add(new Object[]{"validates_time_of", TimeValidate.class});
	}
	
	private ActiveRecord record;
	private String model;
	private String name;
	private String format;
	private String type;
	private Object defaultValue;
	private Map<String,Object> attrCnf;
	private String message;
	
	public Attribute(ActiveRecord record,String name) throws TypeException{
		super();
		this.record = record;
		this.model = record.getClass().getSimpleName();
		this.name = name;

		attrCnf = (Map<String, Object>) Support.map(Support.config().getModels().get(model)).gets("attributes",name);
		if(attrCnf == null)
			attrCnf = new HashMap<String,Object>();
		
		format = (String)attrCnf.get("format");
		type = Support.map(attrCnf).get("type","Object");
		defaultValue = parse(attrCnf.get("default"));
	}
	
	public String getModel(){
		return model;
	}
	
	public String getName(){
		return name;
	}
	
	public String getFormat(){
		return format;
	}
	
	public String getType(){
		return type;
	}
	
	public Object getDefaultValue(){
		return defaultValue;
	}
	
	public <T extends ActiveRecord> T getRecord(){
		return (T) record;
	}
	
	public <T extends Object> T parse(Object o) throws TypeException {
	    Map localeMap = record.getGlobal().currentLocale();
		if (o == null)
			return null;
		try {
			if(type.equals("Object")){
				return (T) o;
			}else if (type.equals("String")) {
				return (T) o;
			} else if (type.equals("Byte")) {
				if (o instanceof Byte)
					return (T) o;
				else
					return (T) (Support.object(o).blank() ? null : Byte.parseByte(o.toString().trim()));
			} else if (type.equals("Short")) {
				if (o instanceof Short)
					return (T) o;
				else
					return (T) (Support.object(o).blank() ? null : Short.parseShort(o.toString().trim()));
			} else if (type.equals("Integer")) {
				if (o instanceof Integer)
					return (T) o;
				else
					return (T) (Support.object(o).blank() ? null : Integer.parseInt(o.toString().trim().split("\\.")[0]));
			} else if (type.equals("Long")) {
				if (o instanceof Long)
					return (T) o;
				else
					return (T) (Support.object(o).blank() ? null : Long.parseLong(o.toString().trim().split("\\.")[0]));
			} else if (type.equals("Float")) {
				if (o instanceof Float)
					return (T) o;
				else
					return (T) (Support.object(o).blank() ? null : Float.parseFloat(o.toString().trim()));
			} else if (type.equals("Double")) {
				if (o instanceof Double)
					return (T) o;
				else
					return (T) (Support.object(o).blank() ? null : Double.parseDouble(o.toString().trim()));
			} else if (type.equals("Timestamp")) {
				if (o instanceof Timestamp)
					return (T) o;
				else {
					if (Support.object(o).blank())
						return null;					
					
				// 	String defFormat = (String)Support.map(localeMap).gets("formats:datetime");
					String defFormat = Support.map(record.getReaderAdapter().getDbcnf()).gets("formats","datetime");
					String f = Support.string(format).def(defFormat);
					return (T) new Timestamp(new SimpleDateFormat(f).parse(o.toString()).getTime());
				}
			} else if (type.equals("Date")) {
				if (o instanceof java.sql.Date)
					return (T) o;
				else if (o instanceof java.util.Date)
					return (T) new java.sql.Date(((java.util.Date) o).getTime());
				else {
					if (Support.object(o).blank())
						return null;
					
				// 	String defFormat = (String)Support.map(localeMap).gets("formats:date");
					String defFormat = Support.map(record.getReaderAdapter().getDbcnf()).gets("formats","date");
					String f = Support.string(format).def(defFormat);
					return (T) new Date(new SimpleDateFormat(f).parse(o.toString()).getTime());
				}
			} else if (type.equals("Time")) {
				if (o instanceof Time)
					return (T) o;
				else {
					if (Support.object(o).blank())
						return null;
					
				// 	String defFormat = (String)Support.map(localeMap).gets("formats:time");
					String defFormat = Support.map(record.getReaderAdapter().getDbcnf()).gets("formats","time");
					String f = Support.string(format).def(defFormat);
					return (T) new Time(new SimpleDateFormat(f).parse(o.toString().trim()).getTime());
				}
			} else if (type.equals("Boolean")) {
				if (o instanceof Boolean)
					return (T) o;
				else
					return (T) (Support.object(o).blank() ? null : Boolean.parseBoolean(o.toString().trim()));
			} else {
				return (T) o;
			}
		} catch (Exception e) {
			String defErr = MessageFormat.format("{0} : {1}.{2} ({3}) not available!","validates_type_of", model, name,o);
			Map<String,String> msg = Support.validateMessage(getGlobal(),"validates_type_of", model, name).getMessages();
			message = Support.string(msg.get("message")).def(defErr);
			throw new TypeException(defErr,message);
		}
	}
	
	public AbsGlobal getGlobal(){
		return record.getGlobal();
	}
	
	public List<Validate> getValidator() throws ConfigurException{
		if(attrCnf == null)
			return null;
		
		Validate validate = null;
		List<Validate> ls = new ArrayList<Validate>();
		try{		
			List<String> keys = Support.map(attrCnf).keys();
			for(Object[] valid : VALIDATES){
				String of = valid[0].toString();
				if(!keys.contains(of))
					continue;
				
				Class<Validate> ofcls = (Class<Validate>)valid[1];
				validate = (Validate) ofcls.getConstructor(Attribute.class).newInstance(this);
				ls.add(validate);
			}
			if(keys.contains("validates_uniqueness_of")){
				ls.add(new UniquenessValidate(this,record));
			}
			if(keys.contains("validates_free_of")){				
				String classify = ((Map<String,Object>)attrCnf.get("validates_free_of")).get("classify").toString();
				Class<Validate> ofcls = (Class<Validate>) Class.forName(classify);
				validate = (Validate)ofcls.getConstructor(Attribute.class).newInstance(this);
				ls.add(validate);
			}
			return ls;
		}catch(Exception e){
			throw new ConfigurException(e.getMessage(),e);
		}
	}

}
