package com.j4gpg3.control;

import java.io.IOException;

public class EasyGPG3 {
	
	private static EasyGPG3 _instance; 
	
	public static EasyGPG3 Instance() throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new EasyGPG3(GoPiGo3.Instance(8, true));
		}
		return _instance;
	}
	
	public static EasyGPG3 Instance(int addr) throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new EasyGPG3(GoPiGo3.Instance(addr, true));
		}
		return _instance;
	}
	
	public static EasyGPG3 Instance(boolean detect) throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new EasyGPG3(GoPiGo3.Instance(8, detect));
		}
		return _instance;
	}
	
	public static EasyGPG3 Instance(int addr, boolean detect) throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new EasyGPG3(GoPiGo3.Instance(addr, detect));
		}
		return _instance;
	}
	
	private EasyGPG3(GoPiGo3 gpg3) throws IOException{
		_gpg3 = gpg3;
		ResetSpeed();
	}
	
	private GoPiGo3 _gpg3;
	private final int DEFAULT_SPEED = 300;
	private int _speed;
	private int[] _leftEyeColor = {0, 255, 255};
	private int[] _rightEyeColor = {0, 255, 255};
	
	// Public API 
	
	public double Voltage() throws IOException{
		return _gpg3.GetVoltageBattery();
	}
	
	public void SetSpeed(int speed) throws IOException{
		_speed = speed;
		_gpg3.SetMotorLimits(_gpg3.MotorLeft + _gpg3.MotorRight, 0, _speed);
	}
	
	public int GetSpeed(){
		return _speed;
	}
	
	public void ResetSpeed() throws IOException{
		SetSpeed(DEFAULT_SPEED);
	}
	
	public void Stop() throws IOException{
		_gpg3.SetMotorDPS(_gpg3.MotorLeft + _gpg3.MotorRight, 0);
	}
	
	public void Backward() throws IOException{
		_gpg3.SetMotorDPS(_gpg3.MotorLeft + _gpg3.MotorRight, _speed * -1);
	}
	
	public void Right() throws IOException{
		_gpg3.SetMotorDPS(_gpg3.MotorLeft, _speed);
		_gpg3.SetMotorDPS(_gpg3.MotorRight, 0);
	}
	
	public void Left() throws IOException{
		_gpg3.SetMotorDPS(_gpg3.MotorRight, _speed);
		_gpg3.SetMotorDPS(_gpg3.MotorLeft, 0);
	}
	
	public void Forward() throws IOException{
		_gpg3.SetMotorDPS(_gpg3.MotorLeft + _gpg3.MotorRight, _speed);
	}
	
	public void DriveCm(double dist, boolean blocking) throws Exception{
		double dist_mm = dist * 10;
		double wheelTurnDegrees = ((dist_mm / _gpg3.WHEEL_CIRCUMFERENCE) * 360);
		int startPositionLeft = _gpg3.GetMotorEncoder(_gpg3.MotorLeft);
		int startPositionRight = _gpg3.GetMotorEncoder(_gpg3.MotorRight);
		
		_gpg3.SetMotorPosition(_gpg3.MotorLeft, startPositionLeft + (int)wheelTurnDegrees);
		_gpg3.SetMotorPosition(_gpg3.MotorRight, startPositionRight + (int)wheelTurnDegrees);
		
		if (blocking){
			while (!TargetReached(startPositionLeft + (int)wheelTurnDegrees, startPositionRight + (int)wheelTurnDegrees)){
				Thread.sleep(100);
			}
		}
	}
	
	public void DriveInches(double dist, boolean blocking) throws Exception{
		DriveCm(dist * 2.54, blocking);
	}
	
	public void DriveDegrees(int degrees, boolean blocking) throws Exception {
		int startPositionLeft = _gpg3.GetMotorEncoder(_gpg3.MotorLeft);
		int startPositionRight = _gpg3.GetMotorEncoder(_gpg3.MotorRight);
		
		_gpg3.SetMotorPosition(_gpg3.MotorLeft, startPositionLeft + degrees);
		_gpg3.SetMotorPosition(_gpg3.MotorRight, startPositionRight + degrees);
		
		if (blocking){
			while (!TargetReached(startPositionLeft + degrees, startPositionRight + degrees)){
				Thread.sleep(100);
			}
		}
	}
	
	private boolean TargetReached(int leftTargetDegrees, int rightTargetDegrees) throws Exception{
		int tolerance = 5;
        int min_left_target = leftTargetDegrees - tolerance;
        int max_left_target = leftTargetDegrees + tolerance;
        int min_right_target = rightTargetDegrees - tolerance;
        int max_right_target = rightTargetDegrees + tolerance;
        
        int current_left_position = _gpg3.GetMotorEncoder(_gpg3.MotorLeft);
        int current_right_position = _gpg3.GetMotorEncoder(_gpg3.MotorRight);
        
        if (current_left_position > min_left_target &&
        current_left_position < max_left_target && 
        current_right_position > min_right_target && 
        current_right_position < max_right_target) {
        	return true;
        } else {
        	return false;
        }
	}
	
	public void ResetEncoders() throws Exception{
		_gpg3.SetMotorPower(_gpg3.MotorLeft + _gpg3.MotorRight, 0);
		_gpg3.OffsetMotorEncoder(_gpg3.MotorLeft, _gpg3.GetMotorEncoder(_gpg3.MotorLeft));
		_gpg3.OffsetMotorEncoder(_gpg3.MotorRight, _gpg3.GetMotorEncoder(_gpg3.MotorRight));
	}
	
	public int[] ReadEncoders() throws Exception{
		return new int[]{_gpg3.GetMotorEncoder(_gpg3.MotorLeft), _gpg3.GetMotorEncoder(_gpg3.MotorRight)};
	}
	
	public void TurnDegrees(double degrees, boolean blocking) throws Exception{
		double wheelTravelDistance = ((_gpg3.WHEEL_BASE_CIRCUMFERENCE * degrees) / 360);
		double wheelTurnDegrees = ((wheelTravelDistance / _gpg3.WHEEL_CIRCUMFERENCE) * 360);
		int startPositionLeft = _gpg3.GetMotorEncoder(_gpg3.MotorLeft);
		int startPositionRight = _gpg3.GetMotorEncoder(_gpg3.MotorRight);
		
		_gpg3.SetMotorPosition(_gpg3.MotorLeft, startPositionLeft + (int)wheelTurnDegrees);
		_gpg3.SetMotorPosition(_gpg3.MotorRight, startPositionRight - (int)wheelTurnDegrees);
		
		if (blocking){
			while (!TargetReached(startPositionLeft + (int)wheelTurnDegrees, startPositionRight - (int)wheelTurnDegrees)){
				Thread.sleep(100);
			}
		}
	}
	
	public void BlinkerOn(String id) throws IOException{
		if (id.equals("left")){
			_gpg3.SetLed(_gpg3.LedBlinkerLeft, 255, 0, 0);
		} else if (id.equals("right")){
			_gpg3.SetLed(_gpg3.LedBlinkerRight, 255, 0, 0);
		}
	}
	
	public void BlinkerOff(String id) throws IOException{
		if (id.equals("left")){
			_gpg3.SetLed(_gpg3.LedBlinkerLeft, 0, 0, 0);
		} else if (id.equals("right")){
			_gpg3.SetLed(_gpg3.LedBlinkerRight, 0, 0, 0);
		}
	}
	
	public void LEDOn(String id) throws IOException{
		BlinkerOn(id);
	}
	
	public void LEDOff(String id) throws IOException{
		BlinkerOff(id);
	}
	
	public void SetLeftEyeColor(int[] color) throws Exception{
		if (color.length != 3){
			throw new Exception("Eye color not valid");
		}
		_leftEyeColor = color;
	}
	
	public void SetRightEyeColor(int[] color) throws Exception{
		if (color.length != 3){
			throw new Exception("Eye color not valid");
		}
		_rightEyeColor = color;
	}
	
	public void SetEyeColor(int[] color) throws Exception{
		SetLeftEyeColor(color);
		SetRightEyeColor(color);
	}
	
	public void OpenLeftEye() throws IOException{
		_gpg3.SetLed(_gpg3.LedEyeLeft, _leftEyeColor[0], _leftEyeColor[1], _leftEyeColor[2]);
	}
	
	public void OpenRightEye() throws IOException{
		_gpg3.SetLed(_gpg3.LedEyeRight, _rightEyeColor[0], _rightEyeColor[1], _rightEyeColor[2]);
	}
	
	public void OpenEyes() throws IOException{
		OpenLeftEye();
		OpenRightEye();
	}
	
	public void CloseLeftEye() throws IOException{
		_gpg3.SetLed(_gpg3.LedEyeLeft, 0, 0, 0);
	}
	
	public void CloseRightEye() throws IOException{
		_gpg3.SetLed(_gpg3.LedEyeRight, 0, 0, 0);
	}
	
	public void CloseEyes() throws IOException{
		CloseLeftEye();
		CloseRightEye();
	}
}
