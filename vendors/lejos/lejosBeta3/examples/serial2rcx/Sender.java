import tinyvm.rcx.*;

class Sender
{
	public static void main(String[] args)
	{
		byte[] b = new byte[] {(byte)0xf7, (byte)0x01};
		
		while(true)
		{
			for(int i = 0;  i < 1000;  i--)
				i += 2;
			Serial.sendPacket(b, 0, 2);
		}
	}
}