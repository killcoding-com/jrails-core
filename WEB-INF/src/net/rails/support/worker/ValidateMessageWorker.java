package net.rails.support.worker;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import net.rails.ext.AbsGlobal;
import net.rails.ext.IndexMap;
import net.rails.support.Support;

public class ValidateMessageWorker {

	private Map<String, String> messages = new IndexMap<String,String>();
	
	@SuppressWarnings("unchecked")
	public ValidateMessageWorker(AbsGlobal g,String of,String model,String attr) {
		super();
		String vk = of.split("_")[1];
		Map<String,Object> modCnf = Support.config().getModels().get(model);
		Map<String, Object> attrCnf = (Map<String, Object>) Support.map(modCnf).gets("attributes",attr);
		Map<String, String> errMsg = (Map<String, String>) g.locale("errors",model,attr,vk);
		if(errMsg == null)
			errMsg = (Map<String, String>) g.locale("errors","ALL","ALL",vk);
		
		if(errMsg != null){
			List<String> msgKeys = Support.map(errMsg).keys();
			Map<String,Object> attrValidate = (Map<String, Object>) attrCnf.get(of);
			attrValidate = Support.map(attrValidate).def("{}");
			for(String mk : msgKeys){
				String pattern = errMsg.get(mk).toString();
				Object limit = null;
				if(attrValidate != null)
					limit = attrValidate.get(mk);
				
				String msg = MessageFormat.format(pattern,g.m(model),g.a(model,attr),"",limit);
				messages.put(mk, msg);					
			}
		}
	}
	
	public Map<String, String> getMessages(){
		return messages;
	}	

}
