package net.rails.active_record.validate;

import net.rails.active_record.Attribute;
import net.rails.support.Support;

public final class BooleanValidate extends Validate {
	
	private Boolean parseValue;

	public BooleanValidate(Attribute attribute) {
		super(attribute);
	}
	
	@Override
	public String getOf() {
		return "validates_boolean_of";
	}
	
	@Override
	public <T extends Object> T pass(Object value) {	
		try {
			parseValue = (Boolean)attribute.parse(value);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}
		
		if(Support.object(parseValue).blank() && isAllowBlank()){
			return (T) parseValue;
		}
		
		return (T) parseValue;
	}
	
}
