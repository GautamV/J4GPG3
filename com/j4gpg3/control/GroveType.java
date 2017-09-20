package com.j4gpg3.control;

public enum GroveType {
	CUSTOM,
	IR_DI_REMOTE,
	IR_EV3_REMOTE,
	US,
	I2C;
	
	static int number = 1;
	
	private int _id;
	
	GroveType(){
		setId();
	}
	
	private void setId(){
		_id = number++;
	}
	
	public int getId(){
		return _id;
	}
}
