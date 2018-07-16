package net.rails.active_record.validate;

public class TypeException extends Exception {
	
	private String showMsg;
	
	public TypeException(String message){
		super(message);
	}
	
	public TypeException(String message,String showMsg){
		super(message);
		this.showMsg = showMsg;
	}
	
	public String getShowMsg(){
		return showMsg;
	}

}
