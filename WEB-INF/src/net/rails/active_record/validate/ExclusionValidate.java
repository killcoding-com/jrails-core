package net.rails.active_record.validate;

import java.util.ArrayList;

import net.rails.active_record.Attribute;
import net.rails.support.Support;

@SuppressWarnings("unchecked")
public final class ExclusionValidate extends Validate {

	private ArrayList<Object> in;;
	private Object parseValue;

	public ExclusionValidate(Attribute attribute) {
		super(attribute);
		init();
	}

	@Override
	public String getOf() {
		return "validates_exclusion_of";
	}

	private void init() {
			Object o = valiCnf.get("in");
			if (o != null)
				in = (ArrayList) o;
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
		
		if (Support.object(parseValue).blank() && isAllowBlank()) {
			return (T) parseValue;
		}
		
		if(parseValue instanceof String){
			if (in.contains(parseValue.toString().toLowerCase())) {
				error("in", parseValue, in);
				return (T) parseValue;
			}
		}else{
			if (in.contains(parseValue)) {
				error("in", parseValue, in);
				return (T) parseValue;
			}
		}
		
		return (T) parseValue;

	}

}
