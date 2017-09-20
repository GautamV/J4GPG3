package com.j4gpg3.control;

import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.spi.*;

public class GoPiGo3 {

	public final String FirmwareVersionRequired = "0.3.x";

	public final double WHEEL_BASE_WIDTH         = 117; // distance (mm) from left wheel to right wheel. This works with the initial GPG3 prototype. Will need to be adjusted.
	public final double WHEEL_DIAMETER           = 66.5; // wheel diameter (mm)
	public final double WHEEL_BASE_CIRCUMFERENCE = WHEEL_BASE_WIDTH * Math.PI; // The circumference of the circle the wheels will trace while turning (mm)
	public final double WHEEL_CIRCUMFERENCE      = WHEEL_DIAMETER   * Math.PI; // The circumference of the wheels (mm)

	public final double MOTOR_GEAR_RATIO           = 120; // Motor gear ratio, 220 for Nicole's prototype
	public final double ENCODER_TICKS_PER_ROTATION = 6; // Encoder ticks per motor rotation (number of magnet positions) # 16 for early prototypes
	public final double MOTOR_TICKS_PER_DEGREE = ((MOTOR_GEAR_RATIO * ENCODER_TICKS_PER_ROTATION) / 360.0); // encoder ticks per output shaft rotation degree

	public final int GROVE_I2C_LENGTH_LIMIT = 16;

	public final int LedEyeLeft = 0x02;
	public final int LedEyeRight = 0x01;
	public final int LedBlinkerLeft = 0x04;
	public final int LedBlinkerRight = 0x08;

	public final int LedWifi = 0x80;

	public final int Servo1 = 0x01;
	public final int Servo2 = 0x02;

	public final int MotorLeft = 0x01;
	public final int MotorRight = 0x02;

	public final int MotorFloat = -128;

	public final int GROVE_1_1 = 0x01;
	public final int GROVE_1_2 = 0x02;
	public final int GROVE_2_1 = 0x04;
	public final int GROVE_2_2 = 0x08;

	public final int GROVE_1 = GROVE_1_1 + GROVE_1_2;
	public final int GROVE_2 = GROVE_2_1 + GROVE_2_2;

	public final int[] GroveType = {0, 0};
	public final int[] GroveI2CInBytes = {0, 0};

	public final int GROVE_INPUT_DIGITAL = 0;
	public final int GROVE_OUTPUT_DIGITAL = 1;
	public final int GROVE_INPUT_DIGITAL_PULLUP = 2;
	public final int GROVE_INPUT_DIGITAL_PULLDOWN = 3;
	public final int GROVE_INPUT_ANALOG = 4;
	public final int GROVE_OUTPUT_PWM = 5;
	public final int GROVE_INPUT_ANALOG_PULLUP = 6;
	public final int GROVE_INPUT_ANALOG_PULLDOWN = 7;

	public final int GROVE_LOW  = 0;
	public final int GROVE_HIGH = 1;

	public int SPIAddress;
	public SpiDevice spi;

	private static GoPiGo3 _instance; 

	public static GoPiGo3 Instance() throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new GoPiGo3(8, true);
		}
		return _instance;
	}
	
	public static GoPiGo3 Instance(int addr) throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new GoPiGo3(addr, true);
		}
		return _instance;
	}
	
	public static GoPiGo3 Instance(boolean detect) throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new GoPiGo3(8, detect);
		}
		return _instance;
	}
	
	public static GoPiGo3 Instance(int addr, boolean detect) throws IOException, FirmwareVersionException{
		if (_instance == null) {
			_instance = new GoPiGo3(addr, detect);
		}
		return _instance;
	}

	private GoPiGo3(int addr, boolean detect) throws IOException, FirmwareVersionException {
		SPIAddress = addr;
		spi = SpiFactory.getInstance(SpiChannel.CS1, // Channel 1
				500000, // 500 kHz
				SpiMode.MODE_0); // Mode 0
		if (detect) {
			String manufacturer;
			String board;
			String vfw;
			try{
				manufacturer = GetManufacturer();
				board = GetBoard();
				vfw = GetVersionFirmware();
			} catch (IOException e){
				throw new IOException("No SPI response. GoPiGo3 with address " + addr + " not connected.");
			}
			if (!manufacturer.equals("Dexter Industries") || !board.equals("GoPiGo3")){
				throw new IOException("GoPiGo3 with address " + addr + " not connected.");
			}
//			if (!vfw.split(".")[0].equals(FirmwareVersionRequired.split(".")[0]) ||
//					!vfw.split(".")[1].equals(FirmwareVersionRequired.split(".")[1])){
//				throw new FirmwareVersionException("GoPiGo3 firmware needs to be version " + FirmwareVersionRequired + " but is currently version " + vfw);
//			}
		}
	}

	public byte[] SPITransferArray(byte[] data) throws IOException{
		return spi.write(data);
	}

	public int SPIRead8(SPIMessageType type) throws IOException{
		byte[] out = {(byte) SPIAddress, (byte) type.getId(), 0, 0, 0};
		//System.out.println("out = " + Arrays.toString(out));
		byte[] reply = SPITransferArray(out);
		//System.out.println("reply = " + Arrays.toString(reply));
		if (reply[3] == -91) {
			return (int) reply[4];
		}
		throw new IOException("SPI response was not as expected");
	}

	public int SPIRead16(SPIMessageType type) throws IOException{
		byte[] out = {(byte) SPIAddress, (byte) type.getId(), 0, 0, 0, 0};
		//System.out.println("out = " + Arrays.toString(out));
		byte[] reply = SPITransferArray(out);
		//System.out.println("reply = " + Arrays.toString(reply));
		if (reply[3] == -91) {
			//System.out.println("returning real reply");
			return (int) (reply[4] << 8 | reply[5]); //TODO what??
		}
		throw new IOException("SPI response was not as expected");
	}

	public int SPIRead32(SPIMessageType type) throws IOException{
		byte[] out = {(byte) SPIAddress, (byte) type.getId(), 0, 0, 0, 0, 0, 0};
		//System.out.println("out = " + Arrays.toString(out));
		byte[] reply = SPITransferArray(out);
		//System.out.println("reply = " + Arrays.toString(reply));
		if (reply[3] == -91) {
			return (int) (reply[4] << 24 | reply[5] << 16 | reply[6] << 8 | reply[7]); //TODO what??
		}
		throw new IOException("SPI response was not as expected");
	}

	public void SPIWrite32(SPIMessageType type, int value) throws IOException{
		byte[] out = {(byte) SPIAddress, (byte) type.getId(), (byte)((value >> 24) & 0xFF), (byte)((value >> 16) & 0xFF), (byte)((value >> 8) & 0xFF), (byte)(value & 0xFF)};
		SPITransferArray(out);
	}

	public String GetManufacturer() throws IOException{
		byte[] out = {(byte) SPIAddress, (byte) SPIMessageType.GET_MANUFACTURER.getId(), 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		byte[] reply = SPITransferArray(out);
		if (reply[3] == -91) {
			String name = "";
			for (int i = 4; i < 24; i++){
				if (reply[i] != 0) {
					name += (char)(reply[i]);
				} else {
					break;
				}
			}
			return name;
		}
		return "";
	}

	public String GetBoard() throws IOException{
		byte[] out = {(byte) SPIAddress, (byte) SPIMessageType.GET_NAME.getId(), 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		byte[] reply = SPITransferArray(out);
		if (reply[3] == -91) {
			String name = "";
			for (int i = 4; i < 24; i++){
				if (reply[i] != 0) {
					name += (char)(reply[i]);
				} else {
					break;
				}
			}
			return name;
		}
		return "";
	}

	public String GetVersionHardware() throws IOException{
		int version = SPIRead32(SPIMessageType.GET_HARDWARE_VERSION);
		return (version / 1000000) + "." + ((version / 1000) % 1000) + "." + (version % 1000);
	}

	public String GetVersionFirmware() throws IOException {
		int version = SPIRead32(SPIMessageType.GET_FIRMWARE_VERSION);
		return (version / 1000000) + "." + ((version / 1000) % 1000) + "." + (version % 1000);
	}

	public String GetId() throws IOException{
		byte[] out = {(byte)SPIAddress, (byte)SPIMessageType.GET_ID.getId(),
	                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		byte[] reply = SPITransferArray(out);
		if (reply[3] == -91){
			String s = "";
			for (int i = 4; i < 20; i++){
				s += String.format("%02d", reply[i]);
			}
			return s;
		}
		throw new IOException("No SPI Response");
	}

	public void SetLed(int led, int red, int green, int blue) throws IOException{
		if (led < 0 || led > 255) return;

		if (red > 255) red = 255;
		if (green > 255) green = 255;
		if (blue > 255) blue = 255;

		if (red < 0) red = 0;
		if (green < 0) green = 0;
		if (blue < 0) blue = 0;

		byte[] out = {(byte) SPIAddress, (byte)SPIMessageType.SET_LED.getId(), (byte)led, (byte)red, (byte)green, (byte)blue};
		SPITransferArray(out);
	}

	public double GetVoltage5V() throws IOException{
		int val = SPIRead16(SPIMessageType.GET_VOLTAGE_5V);
		return ((double) val) / 1000.0;
	}

	public double GetVoltageBattery() throws IOException{
		int val = SPIRead16(SPIMessageType.GET_VOLTAGE_VCC);
		return ((double) val) / 1000.0;
	}

	public void SetServo(int servo, int us) throws IOException{
		byte[] out = {(byte) SPIAddress, (byte) SPIMessageType.SET_SERVO.getId(), (byte) servo, (byte)((us >> 8) & 0xFF), (byte)(us & 0xFF)};
		SPITransferArray(out);
	}

	public void SetMotorPower(int port, int power) throws IOException{
		if (power > 127) power = 127; 
		if (power < -128) power = -128;
		byte[] out = {(byte) SPIAddress, (byte) SPIMessageType.SET_MOTOR_PWM.getId(), (byte) port, (byte) power};
		SPITransferArray(out);
	}

	public void SetMotorPosition(int port, int position) throws IOException{
		int position_raw = (int)(position * MOTOR_TICKS_PER_DEGREE);
		byte[] out = {(byte) SPIAddress, (byte) SPIMessageType.SET_MOTOR_POSITION.getId(), (byte) port, (byte) ((position_raw >> 24) & 0xFF), 
				(byte) ((position_raw >> 16) & 0xFF), (byte) ((position_raw >> 8) & 0xFF), (byte) (position_raw & 0xFF)};
		SPITransferArray(out);
	}

	public void SetMotorDPS(int port, int dps) throws IOException{
		dps = (int) (dps * MOTOR_TICKS_PER_DEGREE);
		byte[] out = {(byte) SPIAddress, (byte) SPIMessageType.SET_MOTOR_DPS.getId(), (byte) port, 
				(byte)((dps >> 8) & 0xFF), (byte)(dps & 0xFF)};
		//System.out.println("Motor DPS out = " + Arrays.toString(out));
		byte[] reply = SPITransferArray(out);
		//System.out.println("Motor DPS reply = " + Arrays.toString(reply));
	}

	public void SetMotorLimits(int port, int power, int dps) throws IOException{
		dps = (int) (dps * MOTOR_TICKS_PER_DEGREE);
		byte[] out = {(byte) SPIAddress, (byte) SPIMessageType.SET_MOTOR_LIMITS.getId(), (byte) port, (byte) power,
				(byte)((dps >> 8) & 0xFF), (byte)(dps & 0xFF)};
		SPITransferArray(out);
	}

	public int[] GetMotorStatus(int port) throws Exception{
		SPIMessageType type;
		if (port == MotorLeft){
			type = SPIMessageType.GET_MOTOR_STATUS_LEFT;
		} else if (port == MotorRight){
			type = SPIMessageType.GET_MOTOR_STATUS_RIGHT;
		} else {
			throw new Exception();
		}
		byte[] out = {(byte) SPIAddress, (byte) type.getId(), 0,0,0,0,0,0,0,0,0,0};
		byte[] reply = SPITransferArray(out);
		if (reply[3] == -91){
			int power = (int)reply[5];
			if ((power & 0x80) != 0) {
				power = power - 0x100;
			}
			int encoder = (int)((reply[6] << 24) | (reply[7] << 16) | (reply[8] << 8) | reply[9]);
			if ((encoder & 0x80000000) != 0){
				encoder = (int)(encoder - 0x100000000L);
			}
			int dps = (int)((reply[10] << 8) | reply[11]);
			if ((dps & 0x8000) != 0){
				dps = dps - 0x10000;
			}
			int[] ret = {reply[4], power, (int) (encoder / MOTOR_TICKS_PER_DEGREE), (int) (dps / MOTOR_TICKS_PER_DEGREE)};
			return ret;
		}
		//TODO: throw error
		return null;
	}

	public int GetMotorEncoder(int port) throws Exception{
		SPIMessageType type;
		if (port == MotorLeft){
			type = SPIMessageType.GET_MOTOR_ENCODER_LEFT;
		} else if (port == MotorRight){
			type = SPIMessageType.GET_MOTOR_ENCODER_RIGHT;
		} else {
			throw new Exception();
		}

		int encoder = SPIRead32(type);

		if ((encoder & 0x80000000) != 0){
			encoder = (int)(encoder - 0x100000000L);
		}
		return (int)(encoder / MOTOR_TICKS_PER_DEGREE);
	}

	public void OffsetMotorEncoder(int port, int offset) throws IOException{
		offset = (int)(offset * MOTOR_TICKS_PER_DEGREE);
		byte[] out = {(byte)SPIAddress, (byte)SPIMessageType.OFFSET_MOTOR_ENCODER.getId(), (byte)port,
				(byte) ((offset >> 24) & 0xFF), (byte) ((offset >> 16) & 0xFF), (byte) ((offset >> 8) & 0xFF), (byte) (offset & 0xFF)};
		SPITransferArray(out);
	}

	public void SetGroveType(int port, int type) throws IOException{
		for (int p = 0; p < 2; p++){
			if (((port >> (p * 2)) & 3) == 3){
				GroveType[p] = type;
			}
		}
		byte[] out = {(byte)SPIAddress, (byte)SPIMessageType.SET_GROVE_TYPE.getId(), (byte)port, (byte)type};
		SPITransferArray(out);
	}

	public void SetGroveMode(int pin, int mode) throws IOException{
		byte[] out = {(byte)SPIAddress, (byte)SPIMessageType.SET_GROVE_MODE.getId(), (byte)pin, (byte)mode};
		SPITransferArray(out);
	}

	public void SetGroveState(int pin, int state) throws IOException{
		byte[] out = {(byte)SPIAddress, (byte)SPIMessageType.SET_GROVE_STATE.getId(), (byte)pin, (byte)state};
		SPITransferArray(out);
	}

	public void SetGrovePWMDuty(int pin, int duty) throws IOException{
		if (duty < 0) duty = 0;
		if (duty > 100) duty = 100;
		int duty_value = duty * 10;
		byte[] out = {(byte)SPIAddress, (byte)SPIMessageType.SET_GROVE_PWM_DUTY.getId(), (byte)pin,
				(byte) ((duty_value >> 8) & 0xFF), (byte) (duty_value & 0xFF)};
		SPITransferArray(out);
	}

	public void SetGrovePWMFrequency(int port) throws IOException{
		SetGrovePWMFrequency(port, 24000);
	}

	public void SetGrovePWMFrequency(int port, int freq) throws IOException{
		if (freq < 3) freq = 3;
		if (freq > 48000) freq = 48000;

		byte[] out = {(byte)SPIAddress, (byte)SPIMessageType.SET_GROVE_PWM_FREQUENCY.getId(), (byte)port,
				(byte) ((freq >> 8) & 0xFF), (byte) (freq & 0xFF)};
		SPITransferArray(out);
	}

	public void GroveI2CTransfer(int port, int addr, byte[] out) throws Exception{
		GroveI2CTransfer(port, addr, out, 0);
	}

	public byte[] GroveI2CTransfer(int port, int addr, byte[] out, int inBytes) throws Exception{

		// start an I2C transaction as soon as the bus is available
		long timeout = System.currentTimeMillis() + 5; // timeout after 5ms of failed attempted starts
		boolean Continue = false;
		while (!Continue){
			try{
				GroveI2CStart(port, addr, out, inBytes);
				Continue = true;
			} catch (Exception e){ 
				if (System.currentTimeMillis() > timeout)
					throw new Exception("grove_i2c_transfer error: Timeout trying to start transaction");
			}
		}
		long DelayTime = 0;
		if (out.length != 0)
			DelayTime += 1 + out.length;
		if (inBytes != 0)
			DelayTime += 1 + inBytes;

		DelayTime *= (0.000115); // each I2C byte takes about 115uS at full speed (about 100kbps)
		// No point trying to read the values before they are ready.

		Thread.sleep(DelayTime); // delay for as long as it will take to do the I2C transaction.

		timeout = System.currentTimeMillis() + 5; // timeout after 5ms of failed attempted reads
		while (true){
			try{
				// read the results as soon as they are available
				byte[] values = GetGroveValue(port);
				return values;
			} catch (Exception e){
				if (System.currentTimeMillis() > timeout)
					throw new Exception("grove_i2c_transfer error: Timeout waiting for data");
			}
		}

	}

	public void GroveI2CStart(int port, int addr, byte[] out, int inBytes) throws Exception{
		SPIMessageType type;
		int port_index;
		if (port == GROVE_1){
			type = SPIMessageType.START_GROVE_I2C_1;
			port_index = 0;
		} else if (port == GROVE_2){
			type = SPIMessageType.START_GROVE_I2C_2;
			port_index = 1;
		} else {
			throw new Exception("Port not supported. Must get one at a time.");
		}

		int address = ((int)addr & 0x7F) << 1;

		if (inBytes > GROVE_I2C_LENGTH_LIMIT)
			throw new Exception("Read length error. Up to " + GROVE_I2C_LENGTH_LIMIT + " bytes can be read in a single transaction.");

		int outBytes = out.length;
		if (outBytes > GROVE_I2C_LENGTH_LIMIT)
			throw new Exception("Write length error. Up to " + GROVE_I2C_LENGTH_LIMIT + " bytes can be written in a single transaction.");

		byte[] outArr = {(byte)SPIAddress, (byte)type.getId(), (byte)address, (byte)inBytes, (byte)outBytes};
		outArr = Func.concatenate(outArr, out);

		byte[] reply = SPITransferArray(outArr);

		GroveI2CInBytes[port_index] = inBytes;

		if(reply[3] != -91)
			throw new Exception("start_grove_i2c error: No SPI response");

		if(reply[4] != 0)
			throw new Exception("start_grove_i2c error: Not ready to start I2C transaction");
	}

	public byte[] GetGroveValue(int port) throws Exception{
		SPIMessageType type;
		int port_index;
		if (port == GROVE_1){
			type = SPIMessageType.GET_GROVE_VALUE_1;
			port_index = 0;
		} else if (port == GROVE_2){
			type = SPIMessageType.GET_GROVE_VALUE_2;
			port_index = 1;
		} else {
			throw new Exception("Port not supported. Must get one at a time.");
		}

		if (GroveType[port_index] == com.j4gpg3.control.GroveType.IR_DI_REMOTE.getId()){
			byte[] outArray = {(byte)SPIAddress, (byte)type.getId(), 0, 0, 0, 0, 0};
			byte[] reply = SPITransferArray(outArray);

			if(reply[3] == -91){
				if(reply[4] == GroveType[port_index] && reply[5] == 0){
					return new byte[]{reply[6]};
				} else {
					throw new Exception("get_grove_value error: Invalid value");
				}
			} else {
				throw new Exception("get_grove_value error: No SPI response");
			}

		} else if (GroveType[port_index] == com.j4gpg3.control.GroveType.IR_EV3_REMOTE.getId()){
			byte[] outArray = {(byte)SPIAddress, (byte)type.getId(), 0, 0, 0, 0, 0, 0, 0, 0};
			byte[] reply = SPITransferArray(outArray);
			if (reply[3] == -91){
				if (reply[4] == GroveType[port_index] && reply[5] == 0){
					return new byte[] {reply[6], reply[7], reply[8], reply[9]};
				} else {
					throw new Exception("get_grove_value error: Invalid value");
				}
			} else {
				throw new Exception("get_grove_value error: No SPI response");
			}

		} else if (GroveType[port_index] == com.j4gpg3.control.GroveType.US.getId()){
			byte[] outArray = {(byte)SPIAddress, (byte)type.getId(), 0, 0, 0, 0, 0, 0};
			byte[] reply = SPITransferArray(outArray);
			if (reply[3] == -91){
				if (reply[4] == GroveType[port_index] && reply[5] == 0){
					int value = (((reply[6] << 8) & 0xFF00) | (reply[7] & 0xFF));
					if (value == 0) {
						throw new Exception("get_grove_value error: Sensor not responding");
					} else if (value == 1){
						throw new Exception("get_grove_value error: Object not detected within range");
					} else {
						return new byte[] {(byte)value};
					}
				} else {
					throw new Exception("get_grove_value error: Invalid value");
				}
			} else {
				throw new Exception("get_grove_value error: No SPI response");
			}

		} else if (GroveType[port_index] == com.j4gpg3.control.GroveType.I2C.getId()){
			byte[] outArray = {(byte)SPIAddress, (byte)type.getId(), 0, 0, 0, 0};
			outArray = Func.concatenate(outArray, new byte[GroveI2CInBytes[port_index]]);
			byte[] reply = SPITransferArray(outArray);
			if (reply[3] == -91){
				if (reply[4] == GroveType[port_index]){
					if(reply[5] == GroveState.VALID_DATA.getId()){  // no error
						return Arrays.copyOfRange(reply, 6, reply.length);
					} else if (reply[5] == GroveState.I2C_ERROR.getId()){ //I2C bus error
						throw new Exception("get_grove_value error: I2C bus error");
					} else {
						throw new Exception("get_grove_value error: Invalid value");
					}
				} else {
					throw new Exception("get_grove_value error: Grove type mismatch");
				}
			} else {
				throw new Exception("get_grove_value error: No SPI response");
			}
		}
		int value = SPIRead8(type);
		return new byte[]{(byte)value};
	}
	
	public byte GetGroveState(int pin) throws Exception{
		SPIMessageType type;
	
        if (pin == GROVE_1_1){
            type = SPIMessageType.GET_GROVE_STATE_1_1;
        } else if (pin == GROVE_1_2){
            type = SPIMessageType.GET_GROVE_STATE_1_2;
        } else if (pin == GROVE_2_1){
            type = SPIMessageType.GET_GROVE_STATE_2_1;
        } else if (pin == GROVE_2_2){
            type = SPIMessageType.GET_GROVE_STATE_2_2;
        } else {
            throw new Exception("Pin(s) unsupported. Must get one at a time.");
        }

        byte[] outArray = {(byte)SPIAddress,(byte)type.getId(), 0, 0, 0, 0};
        byte[] reply = SPITransferArray(outArray);
        if (reply[3] == -91){
            if (reply[4] == GroveState.VALID_DATA.getId()){ // no error
                return reply[5];
            } else {
                throw new Exception("get_grove_state error: Invalid value");
            }
        } else {
        	throw new Exception("get_grove_state error: No SPI response");
        }
	}
	
	public double GetGroveVoltage(int pin) throws Exception{
		SPIMessageType type;
		
		if (pin == GROVE_1_1){
			type = SPIMessageType.GET_GROVE_VOLTAGE_1_1;
		} else if (pin == GROVE_1_2){
			type = SPIMessageType.GET_GROVE_VOLTAGE_1_2;
		} else if (pin == GROVE_2_1){
			type = SPIMessageType.GET_GROVE_VOLTAGE_2_1;
		} else if (pin == GROVE_2_2){
			type = SPIMessageType.GET_GROVE_VOLTAGE_2_2;
		} else {
			throw new Exception("Pin(s) unsupported. Must get one at a time.");
		}
		
		byte[] outArray = {(byte)SPIAddress,(byte)type.getId(), 0, 0, 0, 0, 0};
		byte[] reply = SPITransferArray(outArray);
		
		if (reply[3] == -91){
            if (reply[4] == GroveState.VALID_DATA.getId()){ // no error
                return ((((reply[5] << 8) & 0xFF00) | (reply[6] & 0xFF)) / 1000.0);
            } else {
                throw new Exception("get_grove_voltage error: Invalid value");
            }
        } else {
        	throw new Exception("get_grove_voltage error: No SPI response");
        }
	}
	
	public double GetGroveAnalog(int pin) throws Exception{
		SPIMessageType type;
		
		if (pin == GROVE_1_1){
			type = SPIMessageType.GET_GROVE_ANALOG_1_1;
		} else if (pin == GROVE_1_2){
			type = SPIMessageType.GET_GROVE_ANALOG_1_2;
		} else if (pin == GROVE_2_1){
			type = SPIMessageType.GET_GROVE_ANALOG_2_1;
		} else if (pin == GROVE_2_2){
			type = SPIMessageType.GET_GROVE_ANALOG_2_2;
		} else {
			throw new Exception("Pin(s) unsupported. Must get one at a time.");
		}
		
		byte[] outArray = {(byte)SPIAddress,(byte)type.getId(), 0, 0, 0, 0, 0};
		byte[] reply = SPITransferArray(outArray);
		
		if (reply[3] == -91){
            if (reply[4] == GroveState.VALID_DATA.getId()){ // no error
                return (((reply[5] << 8) & 0xFF00) | (reply[6] & 0xFF));
            } else {
                throw new Exception("get_grove_analog error: Invalid value");
            }
        } else {
        	throw new Exception("get_grove_analog error: No SPI response");
        }
	}
	
	public void ResetAll() throws IOException{
      
        // reset all sensors
        SetGroveType(GROVE_1 + GROVE_2, com.j4gpg3.control.GroveType.CUSTOM.getId());
        SetGroveMode(GROVE_1 + GROVE_2, GROVE_INPUT_DIGITAL);

        // Turn off the motors
        SetMotorPower(MotorLeft + MotorRight, MotorFloat);

        // Reset the motor limits
        SetMotorLimits(MotorLeft + MotorRight, 0, 0);

        // Turn off the servos
        SetServo(Servo1 + Servo2, 0);

        // Turn off the LEDs
        SetLed(LedEyeLeft + LedEyeRight + LedBlinkerLeft + LedBlinkerRight, 0, 0, 0);
	}	
}
