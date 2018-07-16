package net.rails.support.worker;

public final class TokenWorker implements Cloneable {

	private String family;
	private String group;
	private String name;
	private float version;
	
	public TokenWorker() {
		super();
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}
	
	@Override
	public TokenWorker clone(){
		TokenWorker o = new TokenWorker();
		o.setFamily(this.family);
		o.setGroup(this.group);
		o.setName(this.name);
		o.setVersion(this.version);
		return o;
	}

}