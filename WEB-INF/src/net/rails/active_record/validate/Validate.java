package net.rails.active_record.validate;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.active_record.Attribute;
import net.rails.active_record.validate.exception.ValidateException;
import net.rails.ext.AbsGlobal;
import net.rails.support.Support;

@SuppressWarnings("unchecked")
public abstract class Validate {

	private String message;
	private String errMsg;
	
	protected AbsGlobal g;
	protected Attribute attribute;
	protected String model;
	protected String name;	
	protected String on;

	protected Map<String, Object> attrCnf;
	protected Map<String, Object> valiCnf;
//	protected Map<String, Object> locale;

	protected boolean allowBlank = false;
	protected Logger log;

	public abstract String getOf();
	public abstract <T extends Object> T pass(Object value);

	public Validate(Attribute attribute) {
		super();
		log = LoggerFactory.getLogger(this.getClass());
		this.g = attribute.getGlobal();
		this.model = attribute.getModel();
		this.name = attribute.getName();
		this.attribute = attribute;
//		locale = (Map<String, Object>) g.locale();
		init();
	}

	private void init() {
		Map<String,Object> modCnf = Support.config().getModels().get(model);
		attrCnf = (Map<String, Object>) Support.map(modCnf).gets("attributes",name);
		if (attrCnf == null)
			attrCnf = new HashMap<String, Object>();

		valiCnf = (Map<String, Object>) attrCnf.get(getOf());
		if (valiCnf == null)
			valiCnf = new HashMap<String, Object>();
		
		Object allow = valiCnf.get("allow_null");
		
		allow = valiCnf.get("allow_blank");
		if (allow != null)
			allowBlank = (Boolean) allow;
		
		on = Support.object(valiCnf.get("on")).def("save").toString();

	}

	protected Object getCnfValue(String key) {
		if (valiCnf == null)
			return null;

		return valiCnf.get(key);
	}

	protected void error(String msgKey, Object value, Object limit) {
		message = Support.validateMessage(g, getOf(),model,name).getMessages().get(msgKey);
		errMsg = MessageFormat.format("{0} : {1}.{2} ({3}) not available!",getOf(), model, name,value);
		message = Support.string(message).def(errMsg);
	}
	
	protected void error(String msgKey) {
		message = Support.validateMessage(g, getOf(),model,name).getMessages().get(msgKey);
		errMsg = MessageFormat.format("{0} : {1}.{2} ({3}) not available!",getOf(), model, name,attribute.getRecord().get(name));
		message = Support.string(message).def(errMsg);
	}
	
	protected void typeError() {
		message = Support.validateMessage(g,"validates_type_of",model,name).getMessages().get("message");
		errMsg = MessageFormat.format("{0} : {1}.{2} ({3}) not available!",getOf(), model, name,attribute.getRecord().get(name));
		message = Support.string(message).def(errMsg);
	}
	
	protected void error(StringBuffer msg) {
		message = msg.toString();
		errMsg = MessageFormat.format("{0} : {1}.{2} ({3}) not available!",getOf(), model, name,attribute.getRecord().get(name));
		message = Support.string(message).def(errMsg);
	}

	public String getOn() {
		return on;
	}

	public Map<String, Object> getAttrCnf() {
		return attrCnf;
	}

	public Map<String, Object> getValiCnf() {
		return valiCnf;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public boolean isAllowBlank() {
		return allowBlank;
	}

	public String getMessage() {
		return message;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public <T extends Object> T passes(Object value) throws ValidateException {
		T o = pass(value);
		if (getErrMsg() == null)
			return o;
		else
			throw new ValidateException(getErrMsg());
	}

}
