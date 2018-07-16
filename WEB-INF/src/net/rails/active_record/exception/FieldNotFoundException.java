package net.rails.active_record.exception;

@SuppressWarnings("serial")
public class FieldNotFoundException extends Exception {
	
	public FieldNotFoundException(String msg){
		super("Field not found : " + msg);
	}

}
