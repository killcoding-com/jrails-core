package net.rails.active_record.validate;

import java.sql.Time;
import java.util.ArrayList;

import net.rails.active_record.Attribute;
import net.rails.support.Support;

public final class TimeValidate extends Validate {

	private Time is;
	private Time minimum;
	private Time maximum;
	private Time[] within;
	private Time parseValue;
	private ArrayList<String> withinArr;

	public TimeValidate(Attribute attribute) throws TypeException {
		super(attribute);
		init();
	}

	@Override
	public String getOf() {
		return "validates_time_of";
	}

	@SuppressWarnings("unchecked")
	private void init() throws TypeException {
		Object o = valiCnf.get("within");
		if (!Support.object(o).blank()) {
			withinArr = (ArrayList<String>) o;
			within = new Time[] { (Time) attribute.parse(withinArr.get(0)),
					(Time) attribute.parse(withinArr.get(1)) };
		}
		o = valiCnf.get("is");
		if (!Support.object(o).blank()) {
			is = (Time) attribute.parse(o.toString());
		}
		o = valiCnf.get("minimum");
		if (!Support.object(o).blank())
			minimum = (Time) attribute.parse(o.toString());
		o = valiCnf.get("maximum");
		if (!Support.object(o).blank())
			maximum = (Time) attribute.parse(o.toString());

	}

	@Override
	public <T extends Object> T pass(Object value) {
		Long time = 0L;
		try {
			parseValue = (Time)attribute.parse(value);
			time = parseValue.getTime();
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
			typeError();
			return null;
		}
		if (Support.object(parseValue).blank() && isAllowBlank()) {
			return null;
		}

		if (is != null && time != is.getTime()) {
			error("is", parseValue, valiCnf.get("is"));
		}
		if (minimum != null && time < minimum.getTime()) {
			error("minimum", parseValue, valiCnf.get("minimum"));
		}
		if (maximum != null && time > maximum.getTime()) {
			error("maximum",parseValue, valiCnf.get("maximum"));
		}
		if (within != null && (time < within[0].getTime() || time > within[1].getTime())) {
			error("within", parseValue, withinArr);
		}
		return (T) parseValue;
	}

}
