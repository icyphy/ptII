
import josx.platform.rcx.*;

public class Test18
{
  public static void main (String[] arg)
  {
    Sensor.S3.activate();
    Sensor.S3.addSensorListener (
      new SensorListener() {
        public final void stateChanged (Sensor aSource, boolean aBooleanValue)
        {
          // ignore
        }
    
        public final void stateChanged (Sensor aSource, int aRawValue)
        {
          int pct = aSource.readPercentage();
          LCD.showNumber (pct);
          for (int k = 0; k < 10; k++) { }
        }
      }
    );
  }
}
