import tinyvm.rcx.*;

public class Test23
{
  public static void main (String[] arg)
  {
    LCD.clear();
    LCD.refresh();
    int pCount = 0;
    byte[] pPacket = new byte[10];
    for (;;)
    {
      if (Serial.isPacketAvailable())
      {
        Serial.readPacket (pPacket);
        LCD.showNumber (pPacket[0]);
        LCD.showProgramNumber ((pPacket[2] << 8) + pPacket[1]);
        for (int k = 0; k < 10000; k++) { }
        pCount++;
        if (pCount == 4)
          return;
      }
    }
  }
}
