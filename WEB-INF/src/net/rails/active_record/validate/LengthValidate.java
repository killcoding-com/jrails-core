package net.rails.active_record.validate;

import java.util.ArrayList;
import net.rails.active_record.Attribute;
import net.rails.support.Support;

public final class LengthValidate extends Validate {

	private int minimum = -1;
	private int maximum = -1;
	private int is = -1;
	private Integer[] within;
	private String parseValue;

	public LengthValidate(Attribute attribute) {
		super(attribute);
		init();
	}

	@Override
	public String getOf() {
		return "validates_length_of";
	}

	@SuppressWarnings("unchecked")
	private void init() {
		Object o = valiCnf.get("minimum");
		if (!Support.object(o).blank())
			minimum = (Integer) o;
		o = valiCnf.get("maximum");
		if (!Support.object(o).blank())
			maximum = (Integer) o;
		o = valiCnf.get("is");
		if (!Support.object(o).blank())
			is = (Integer) o;
		o = valiCnf.get("within");
		if (!Support.object(o).blank())
			within = (Integer[]) ((ArrayList) o).toArray(new Integer[2]);
	}

	@Override
	public <T extends Object> T pass(Object value) {
		try {
			parseValue = (String) attribute.parse(value);
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}

		if (Support.object(parseValue).blank() && isAllowBlank()) {
			return (T) parseValue;
		}
		int len = Support.object(parseValue).def("").toString().length();
		if (is != -1 && len != is) {
			error("is", value, is);
		}
		if (minimum != -1 && len < minimum) {
			error("minimum", value, minimum);
		}
		if (maximum != -1 && len > maximum) {
			error("maximum", value, maximum);
		}
		if (within != null && (len < within[0] || len > within[1])){
			error("within", value, within);
		}
		return (T) parseValue;
	}

}
