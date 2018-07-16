package net.rails.cache;

public abstract class AbsCacheApi {

	public AbsCacheApi() {
		super();
	}
	
	abstract void set(String name,Object value,int live);
	abstract Object get(String name);
	abstract String[] getNames();
	abstract boolean included(String name);
	abstract void remove(String name);
	abstract void removeAll();

}
