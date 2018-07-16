package net.rails.active_record.validate;

import net.rails.active_record.Attribute;
import net.rails.support.Support;

public final class NumberValidate extends Validate {
	
	private boolean odd = false;
	private boolean even = false;
	
	private Number greaterThan;
	private Number greaterThanOrEqualTo;
	private Number equalTo;
	private Number lessThan;
	private Number lessThanOrEqualTo;
	private Number parseValue;
	
	public NumberValidate(Attribute attribute) throws TypeException {
		super(attribute);
		init();
	}
	
	@Override
	public String getOf() {
		return "validates_number_of";
	}
	
	private void init() throws TypeException {
		Object o = valiCnf.get("odd");
		if(!Support.object(o).blank())
			odd = (Boolean)o;
		o = valiCnf.get("even");
		if(!Support.object(o).blank())
			even = (Boolean)o;
		o = valiCnf.get("greater_than");
		if(!Support.object(o).blank())
			greaterThan = (Number)attribute.parse(o);
		o = valiCnf.get("greater_than_or_equal_to");
		if(!Support.object(o).blank())
			greaterThanOrEqualTo = (Number)attribute.parse(o);
		o = valiCnf.get("equal_to");
		if(!Support.object(o).blank())
			equalTo = (Number)attribute.parse(o);
		o = valiCnf.get("less_than");
		if(!Support.object(o).blank())
			lessThan = (Number)attribute.parse(o);
		o = valiCnf.get("less_than_or_equal_to");
		if(!Support.object(o).blank())
			lessThanOrEqualTo = (Number)attribute.parse(o);
	}

	@Override
	public <T extends Object> T pass(Object value) {	
		try {
			parseValue = (Number)attribute.parse(value);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}
		if(Support.object(parseValue).blank() && isAllowBlank()){
			return null;
		}		
		
		try{
			if(greaterThan != null && parseValue.doubleValue() <= greaterThan.doubleValue()){
				error("greater_than",parseValue,greaterThan);
			}
			if(greaterThanOrEqualTo != null && parseValue.doubleValue() < greaterThanOrEqualTo.doubleValue()){
				error("greater_than_or_equal_to",parseValue,greaterThanOrEqualTo);
			}
			if(equalTo != null && parseValue.doubleValue() != equalTo.doubleValue()){
				error("equal_to",parseValue,equalTo);
			}
			if(lessThan != null && parseValue.doubleValue() > lessThan.doubleValue()){
				error("less_than",parseValue,lessThan);
			}
			if(lessThanOrEqualTo != null && parseValue.doubleValue() > lessThanOrEqualTo.doubleValue()){
				error("less_than_or_equal_to",parseValue,lessThanOrEqualTo);
			}		
			if(odd && parseValue.doubleValue() != 1 && (parseValue.doubleValue() % 2 == 0)){
				error("odd",parseValue,"odd");
			}
			if(even && parseValue.doubleValue() % 2 != 0){
				error("even",parseValue,"even");
			}
		}
		catch(Exception e){
			error("type", value, "Number");
		}
		return (T) parseValue;
	}

}
