package com.j4gpg3.control;

public enum SPIMessageType {
	NONE,
    GET_MANUFACTURER,
    GET_NAME,
    GET_HARDWARE_VERSION,
    GET_FIRMWARE_VERSION,
    GET_ID,
    SET_LED,
    GET_VOLTAGE_5V,
    GET_VOLTAGE_VCC,
    SET_SERVO,
    SET_MOTOR_PWM,
    SET_MOTOR_POSITION,
    SET_MOTOR_POSITION_KP,
    SET_MOTOR_POSITION_KD,
    SET_MOTOR_DPS,
    SET_MOTOR_LIMITS,
    OFFSET_MOTOR_ENCODER,
    GET_MOTOR_ENCODER_LEFT,
    GET_MOTOR_ENCODER_RIGHT,
    GET_MOTOR_STATUS_LEFT,
    GET_MOTOR_STATUS_RIGHT,
    SET_GROVE_TYPE,
    SET_GROVE_MODE,
    SET_GROVE_STATE,
    SET_GROVE_PWM_DUTY,
    SET_GROVE_PWM_FREQUENCY,
    GET_GROVE_VALUE_1,
    GET_GROVE_VALUE_2,
    GET_GROVE_STATE_1_1,
    GET_GROVE_STATE_1_2,
    GET_GROVE_STATE_2_1,
    GET_GROVE_STATE_2_2,
    GET_GROVE_VOLTAGE_1_1,
    GET_GROVE_VOLTAGE_1_2,
    GET_GROVE_VOLTAGE_2_1,
    GET_GROVE_VOLTAGE_2_2,
    GET_GROVE_ANALOG_1_1,
    GET_GROVE_ANALOG_1_2,
    GET_GROVE_ANALOG_2_1,
    GET_GROVE_ANALOG_2_2,
    START_GROVE_I2C_1,
    START_GROVE_I2C_2;
	
	static int number = 0;
	
	private int _id;
	
	SPIMessageType(){
		setId();
	}
	
	private void setId(){
		_id = number++;
	}
	
	public int getId(){
		return _id;
	}
}