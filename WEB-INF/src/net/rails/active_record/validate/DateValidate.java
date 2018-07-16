package net.rails.active_record.validate;

import java.util.ArrayList;
import java.sql.Date;

import net.rails.active_record.Attribute;
import net.rails.support.Support;

public final class DateValidate extends Validate {
	
	private Date is;
	private Date minimum;
	private Date maximum;
	private Date[] within;
	private Date parseValue;	
	private ArrayList<String> withinArr;

	public DateValidate(Attribute attribute) throws TypeException {
		super(attribute);
		init();
	}
	
	@Override
	public String getOf() {
		return "validates_date_of";
	}
	
	private void init() throws TypeException {		
			Object o = valiCnf.get("within");
			if(!Support.object(o).blank()){
				withinArr = (ArrayList<String>)o;
				within = new Date[]{(Date)attribute.parse(withinArr.get(0)),(Date)attribute.parse(withinArr.get(1))};
			}
			o = valiCnf.get("is");
			if(!Support.object(o).blank())
				is = (Date)attribute.parse(o);
			o = valiCnf.get("minimum");
			if(!Support.object(o).blank())
				minimum = (Date)attribute.parse(o);
			o = valiCnf.get("maximum");
			if(!Support.object(o).blank())
				maximum = (Date)attribute.parse(o);
	}
	
	@Override
	public <T extends Object> T pass(Object value) {		
		Long time = 0L;
		try{
			parseValue = (Date)attribute.parse(value);
			time = parseValue.getTime();
		}catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}
		
		if(Support.object(parseValue).blank() && isAllowBlank()){
			return null;
		}
		
		if(is != null && !time.equals(is.getTime())){
			error("is",parseValue,valiCnf.get("is"));
		}
		if(minimum != null && time < minimum.getTime()){
			error("minimum",parseValue,valiCnf.get("minimum"));
		}
		if(maximum != null && time > maximum.getTime()){
			error("maximum",parseValue,valiCnf.get("maximum"));
		}
		if(within != null && (time < within[0].getTime() || parseValue.getTime() > within[1].getTime())){
			error("within",parseValue,withinArr);
		}
		return (T) parseValue;
	}
	
}
