package net.rails.active_record.validate;

import net.rails.active_record.Attribute;
import net.rails.support.Support;

public final class PresenceValidate extends Validate {	
	
	private Object parseValue;

	public PresenceValidate(Attribute attribute) {
		super(attribute);
	}
	
	@Override
	public String getOf() {
		return "validates_presence_of";
	}
	
	@Override
	public <T extends Object> T pass(Object value) {
		try {
			parseValue = attribute.parse(value);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}			
		if(attrCnf.containsKey(getOf()) && Support.object(parseValue).blank()){
			error("message",parseValue,null);
			return (T) parseValue;
		}		
		return (T) parseValue;
	}
	
}