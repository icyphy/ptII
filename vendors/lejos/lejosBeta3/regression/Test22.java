import josx.platform.rcx.*;

public class Test22
implements SensorConstants
{
  public static void main (String[] arg)
  {
    Sensor.S2.setTypeAndMode (SENSOR_TYPE_LIGHT, SENSOR_MODE_PCT);
    Sensor.S2.activate();
    Sensor.S2.addSensorListener (new SensorListener() 
    {
      public void stateChanged (Sensor src, int oldValue, int newValue) 
      {
        LCD.showNumber (newValue);
	if (src.readBooleanValue())
          Sound.beep();
	try {
          Thread.sleep (10);
	} catch (InterruptedException e) {
	}
      }
    });
  }
}
