package test;

import net.rails.Define;
import net.rails.ext.AbsGlobal;

public class GlobalUnit extends AbsGlobal {
	
	static{
		Define.CONFIG_PATH = "/src/app/WEB-INF/config";
		Define.VIEW_PATH = "/src/app/WEB-INF/view";
	}
	
	private Object userId;
	
	public GlobalUnit(){
		super();
// 		this.options.put("key","value");
	}

	@Override
	public void setUserId(Object userId) {
		this.userId = userId;
	}
	@Override
	public void setSessionId(Object sessionId) {

	}
	@Override
	public Object getUserId() {
		return userId;
	}
	@Override
	public Object getSessionId() {
		return "MySessionId";
	}

	@Override
	public String getRealPath() {
		return String.format("%s/WEB-INF",System.getProperty("user.dir"));
	}

}
