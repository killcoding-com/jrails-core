package net.rails.support.worker;

import java.util.Collection;

public final class ObjectWorker<O> {

	private O source;
	
	public ObjectWorker(O source) {
		super();
		this.source = source;
	}
	
	public boolean nil(){
		return source == null;
	}
	
	public boolean blank(){
		return nil() || source.toString().trim().equals("") || (source instanceof Collection && ((Collection)source).size() == 0) || (source instanceof Object[] && ((Object[])source).length == 0);
	}
	
	public O def(O defaultValue){
		if(blank())
			return defaultValue;
		else
			return source;
	}
	
	public O getSource(){
		return source;
	}

}
