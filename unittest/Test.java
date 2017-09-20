package unittest;

import java.util.Arrays;

import com.j4gpg3.control.EasyGPG3;
import com.j4gpg3.control.GoPiGo3;

public class Test {
	public static void main(String[] args) throws Exception{
		GoPiGo3 go = GoPiGo3.Instance();
		EasyGPG3 easy_go = EasyGPG3.Instance();
		
//		System.out.println("Before moving, motors are at: " + Arrays.toString(easy_go.ReadEncoders()));
//		easy_go.TurnDegrees(90, false);
//		Thread.sleep(2000);
//		System.out.println("After moving, motors are at: " + Arrays.toString(easy_go.ReadEncoders()));
//		
//		System.out.println("5V Voltage is " + go.GetVoltage5V());
//		System.out.println("Full Voltage is " + go.GetVoltageBattery());
//		System.out.println("Manufacturer is " + go.GetManufacturer());
//		System.out.println("Board is " + go.GetBoard());
//		System.out.println("Hardware Version is " + go.GetVersionHardware());
//		System.out.println("FirmwareVersion is " + go.GetVersionFirmware());
//		System.out.println("ID is " + go.GetId());
		
		System.out.println("Hardware is " + go.GetVersionHardware());
	}
}
