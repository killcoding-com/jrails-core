package net.rails.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.rails.ext.IndexMap;
import net.rails.support.Support;
import net.rails.support.worker.AbsConfigWorker;

@SuppressWarnings("unchecked")
public class Route {

	public static Map<String, Object> ROUTE;
	private static AbsConfigWorker CONFIGUR;	
	private Map<String, Object> params;	
	private String controller;
	private String action;	
	private boolean active = true;	
	
	public final static String ROUTE_PARAMS = "ROUTE_PARAMS";

	static {
		CONFIGUR = Support.config().getConfig();
		ROUTE = CONFIGUR.get("route");
		if(ROUTE == null)
			ROUTE = new IndexMap<String,Object>();
	}

	public Route(HttpServletRequest request,String path) {
		super();
		Map<String, Object> pathcnf = ((Map<String, Object>) ROUTE.get(path));
		if(pathcnf == null)
			pathcnf = new IndexMap<String,Object>();
		
		controller = (String)pathcnf.get("controller");
		if(path.equals("root") && controller == null)
			controller = Support.map(pathcnf).get("controller","Root");
		
		action = Support.map(pathcnf).get("action","index");
		params = (Map<String, Object>) pathcnf.get("params");		
		putRouteParams(request,params);
	}
	
	public Route(String controller,String action) {
		super();
		this.controller = controller;
		this.action = action;
	}
	
	public void putRouteParams(HttpServletRequest request,Map<String,Object> routeParams){
		params = routeParams;
		if(params != null)
			request.setAttribute(ROUTE_PARAMS,params);
	}

	public String getController() {
		return controller;
	}

	public String getAction() {
		return action;
	}
	
	public Map<String,Object> getParams(){
		return params;
	}
	
	public void setActive(boolean active){
		this.active = active;
	}

	public boolean isActive(){
		return active;
	}
	
}
