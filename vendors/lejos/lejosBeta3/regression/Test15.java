
// Sensor test

import tinyvm.rcx.*;

public class Test15
{ 
  public static void main (String[] argv)
  {
    for (int i = 0; i < 200; i++)
    {
      int pValue = Sensor.readSensorValue ((short) 0x1001, (byte) 3, (byte) 0x80);
      LCD.showNumber (pValue);
      LCD.showProgramNumber (i % 10);
      for (int k = 0; k < 500; k++) { }
    }
  }
}

