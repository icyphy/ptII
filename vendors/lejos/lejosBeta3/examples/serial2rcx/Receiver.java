import tinyvm.rcx.*;

class Receiver
{
	public static void main(String[] args)
	{
		byte[] b = new byte[8];
		int count = 0;
		
		while(true)
		{
			if(Serial.isPacketAvailable())
			{
				Serial.readPacket(b);
				Sound.beep();
				count++;
				LCD.showNumber(count);
			}
		}
	}
}