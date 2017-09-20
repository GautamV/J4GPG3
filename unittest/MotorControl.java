package unittest;

import java.util.Arrays;
import java.util.Scanner;

import com.j4gpg3.control.EasyGPG3;
import com.j4gpg3.control.GoPiGo3;

public class MotorControl {
	public static void main(String[] args) throws Exception{
		GoPiGo3 go = GoPiGo3.Instance();
		EasyGPG3 easy_go = EasyGPG3.Instance();
		
		int speed = 0; 
		
		Scanner sc = new Scanner(System.in);
		while (true){
			String input = sc.nextLine();
			if (input.equals("w")){
				speed += 50;
			} else if (input.equals("s")){
				speed -= 50;
			} else if (input.equals("x")){
				speed = 0;
			}
			
			go.SetMotorDPS(go.MotorLeft + go.MotorRight, speed);
		}
	}
}
