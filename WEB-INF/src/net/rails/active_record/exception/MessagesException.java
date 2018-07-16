package net.rails.active_record.exception;

import java.util.ArrayList;
import java.util.List;
import net.rails.support.Support;

public class MessagesException extends Exception {
	
	private final List<String> messages = new ArrayList<String>();
	
	public MessagesException(List<String> messages){
		super();
		this.messages.addAll(messages);
	}
	
	public List<String> getMessages(){
		return messages;
	}
	
	@Override
	public String getMessage(){
		if(Support.string(super.getMessage()).blank())
			return Support.array(messages).join("\n");
		else
			return super.getMessage();
	}

}
