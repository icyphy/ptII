
import josx.platform.rcx.*;
import java.util.Random;

public class Test38
{
    public static void main (String[] args) 
    {
      Sensor.S1.activate();
      int t1 = (int) System.currentTimeMillis();
      for (int i = 0; i < 2600; i++)
      {
	Sensor.readSensorValue (0, 0);
	//System.currentTimeMillis();
      }
      int t2 = (int) System.currentTimeMillis();
      LCD.showNumber (t2 - t1);
      Button.VIEW.waitForPressAndRelease();
    }
}
