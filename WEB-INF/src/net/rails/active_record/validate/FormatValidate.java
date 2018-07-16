package net.rails.active_record.validate;

import net.rails.active_record.Attribute;
import net.rails.support.Support;

public final class FormatValidate extends Validate {
	
	private String with;
	private String parseValue;

	public FormatValidate(Attribute attribute) {
		super(attribute);
		init();
	}
	
	@Override
	public String getOf() {
		return "validates_format_of";
	}
	
	private void init(){
		Object o = valiCnf.get("with");
		if(o != null)
			with = o.toString();

	}
	
	@Override
	public <T extends Object> T pass(Object value) {	
		try {
			parseValue = (String)attribute.parse(value);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}

		if(Support.object(parseValue).blank() && isAllowBlank()){
			return (T) parseValue;
		}
		if(!Support.object(parseValue).def("").toString().matches(with)){
			error("with",parseValue,with);
			return  (T) parseValue;
		}
		return (T) parseValue;
	}
	
}
