package app.global;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.rails.ext.AbsGlobal;
import net.rails.support.worker.UserAgentWorker;
import net.rails.web.Controller;
import net.rails.web.Route;

public class Global extends AbsGlobal {
	
	private HttpSession session;
	private FilterConfig config;
	private HttpServletRequest request;
	private Route route;
	private UserAgentWorker ua;
	
	public static class Classify {
		
		public static String Root = "Root";
		public static String TryOutCustomer = "TryOutCustomer"; 
		public static String Customer = "Customer"; 
		public static String VipCustomer = "VipCustomer"; 
		public static String Agency = "Agency";
		
	}
	
	public Global(Controller controller){
		super();
		this.config = controller.getConfig();
		this.request = controller.getRequest();
		this.session = request.getSession();		
		this.route = controller.getRoute();
		this.ua = controller.getUserAgent();
		this.options.put("protocol", request.getScheme());
		this.options.put("domain",request.getServerName());
		this.options.put("port", request.getServerPort());
		this.options.put("path", request.getContextPath());
		this.options.put("controller",route.getController());
		this.options.put("action",route.getAction());
		this.options.put("_ARGS",controller.getParams().get("_ARGS"));
		String domainRoot = null;
		String domain = this.options.get("domain").toString();
		String protocol = this.options.get("protocol").toString().toLowerCase();
		int port = (Integer)this.options.get("port");
		String path = this.options.get("path").toString();		
		if(protocol.equals("http")){
			if(port == 80){
				domainRoot = "http://" + domain;
			}else{
				domainRoot = "http://" + domain + ":" + port;
			}
		}else if(protocol.equals("https")){
			if(port == 443){
				domainRoot = "https://" + domain;
			}else{
				domainRoot = "https://" + domain + ":" + port;
			}
		}else{
			domainRoot =  protocol + "://" + domain + ":" + port;
		}
		this.options.put("domainRoot", domainRoot);	
		this.options.put("domainUrl", domainRoot + path);		
		this.options.put("os-family", ua.os().getFamily());
		this.options.put("os-name", ua.os().getName());
		this.options.put("os-version", ua.os().getVersion());
		this.options.put("browser-family",ua.browser().getFamily());
		this.options.put("browser-name",ua.browser().getName());
		this.options.put("browser-engine",ua.engine().getName());
		this.options.put("browser-version",ua.engine().getVersion());		
	}

	@Override
	public void setUserId(Object userId) {
		session.setAttribute("i", userId);
	}
	@Override
	public void setSessionId(Object sessionId) {
		session.setAttribute("sid", sessionId);
	}
	@Override
	public Object getUserId() {
		return session.getAttribute("i");
	}
	@Override
	public Object getSessionId() {
		return session.getId();
	}

	@Override
	public String getRealPath() {
		return config.getServletContext().getRealPath("/");
	}

}
