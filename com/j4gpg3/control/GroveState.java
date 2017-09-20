package com.j4gpg3.control;

public enum GroveState {
	VALID_DATA,
    NOT_CONFIGURED,
    CONFIGURING,
    NO_DATA,
    I2C_ERROR;
    
    static int number = 0;
	
	private int _id;
	
	GroveState(){
		setId();
	}
	
	private void setId(){
		_id = number++;
	}
	
	public int getId(){
		return _id;
	}
}
