package common;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private Protocol type;
	private Object data;
	
	public Message(Object m, Protocol type){
		this.type = type;
		data = m;
	}
	
	public Protocol getType() {
		return type;
	}
	
	public Object getData() {
		return data;
	}
}
