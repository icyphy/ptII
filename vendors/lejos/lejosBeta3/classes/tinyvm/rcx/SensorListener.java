package tinyvm.rcx;

/**
 * Listener of sensor events.
 * @see tinyvm.rcx.Sensor#addSensorListener
 */
public interface SensorListener
{
  /**
   * Called when the boolean state of the sensor changes.
   */
  public void stateChanged (Sensor aSource, boolean aBooleanValue);  

  /**
   * Called when the raw value of the sensor changes.
   */
  public void stateChanged (Sensor aSource, int aRawValue);
}





