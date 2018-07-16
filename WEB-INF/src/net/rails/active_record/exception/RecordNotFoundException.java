package net.rails.active_record.exception;

@SuppressWarnings("serial")
public class RecordNotFoundException extends Exception {
	
	public RecordNotFoundException(String msg){
		super("ActiveRecord not found : " + msg);
	}

}
