import josx.platform.rcx.*;

/**
 * This program listens for messages from your PC.
 * It accepts a few standard opcodes, such as
 * Set Motor On/Off (0x21). You should be able to
 * control it using the Lego Mindstorms GUI.
 */
public class Receiver
{
  public static void main (String[] arg)
  {
    LCD.showNumber (1111);
    int pCount = 0;
    byte[] pPacket = new byte[10];
    for (;;)
    {
      if (Serial.isPacketAvailable())
      {
        Serial.readPacket (pPacket);
        int pOpCode = pPacket[0] & 0xF7;
	if (pOpCode == 0x10)
	{
            // Alive?
            pPacket[0] = (byte) ~(pPacket[0] & 0xFF);
            Serial.sendPacket (pPacket, 0, 1);
	}
        else if (pOpCode == 0x15)
	{
            // Get versions
            pPacket[0] = (byte) ~(pPacket[0] & 0xFF);
            pPacket[1] = 0x00;
            pPacket[2] = 0x03;
            pPacket[3] = 0x00;
            pPacket[4] = 0x01;
            pPacket[5] = 0x00;
            pPacket[6] = 0x03;
            pPacket[7] = 0x00;
            pPacket[8] = 0x09;
            Serial.sendPacket (pPacket, 0, 9);
        }
  	else if (pOpCode == 0x30)
	{
	    // Get battery power
            pPacket[0] = (byte) ~(pPacket[0] & 0xFF);
	    // Fake (constant) battery power
            pPacket[1] = 0x20;
            pPacket[2] = 0x40;
            Serial.sendPacket (pPacket, 0, 3);
	}
  	else if (pOpCode == 0x65)
	{
            // Delete firmware
            pPacket[0] = (byte) ~(pPacket[0] & 0xFF);
            Serial.sendPacket (pPacket, 0, 1);
            // Exit program
            return;
	}
        else if (pOpCode == 0x21)
	{
	    // Set Motor On/Off
            int pFlags = pPacket[1] & 0xFF;
            if ((pFlags & 0x01) != 0)
	    {
              if ((pFlags & 0x040) != 0) 
                Motor.A.stop();
              if ((pFlags & 0x080) != 0) 
                Motor.A.forward();
	    }
            if ((pFlags & 0x02) != 0)
	    {
              if ((pFlags & 0x040) != 0) 
                Motor.B.stop();
              if ((pFlags & 0x080) != 0) 
                Motor.B.forward();
	    }
            if ((pFlags & 0x04) != 0)
	    {
              if ((pFlags & 0x040) != 0) 
                Motor.C.stop();
              if ((pFlags & 0x080) != 0) 
                Motor.C.forward();
	    }
            pPacket[0] = (byte) ~(pPacket[0] & 0xFF);
            Serial.sendPacket (pPacket, 0, 1);
	}
  	else
	{
            LCD.showNumber (pPacket[0]);
  	    pPacket[0] = (byte) ~(pPacket[0] & 0xFF);
            Serial.sendPacket (pPacket, 0, 1);
            Sound.beep();
	}
      }
    }
  }
}

