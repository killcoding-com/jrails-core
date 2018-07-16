package net.rails.active_record.validate.exception;

@SuppressWarnings("serial")
public class ConfigurException extends Exception {
	
	public ConfigurException(String message){
		super(message);
	}
	
	public ConfigurException(String message,Throwable cause){
		super(message,cause);
	}
	

}
