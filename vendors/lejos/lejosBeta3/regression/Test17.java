
// Sensor test

import tinyvm.rcx.*;

public class Test17
{ 
  public static void main (String[] argv)
  {
    int[] pValues = new int[10];
    for (int i = 0; i < 50; i++)
    {
      Sensor.S2.passivate();
      for (int k = 0; k < 1000; k++) { }
      Sensor.S2.activate();
      for (int j = 0; j < 10; j++)
        pValues[j] = Sensor.S2.readPercentage();
      for (int j = 0; j < 10; j++)
      {
        LCD.showNumber (pValues[j]);
        LCD.showProgramNumber (j % 10);
        for (int k = 0; k < 10000; k++);
      }
    }
    Sensor.S2.passivate();
  }
}
