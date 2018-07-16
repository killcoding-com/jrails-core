package net.rails.active_record.validate;

import net.rails.active_record.Attribute;

public abstract class FreeValidate extends Validate {
	
	public FreeValidate(Attribute attribute) throws TypeException {
		super(attribute);
	}
	
	@Override
	public String getOf() {
		return "validates_free_of";
	}
	
}
