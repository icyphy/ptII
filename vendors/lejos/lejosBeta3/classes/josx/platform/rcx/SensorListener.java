package josx.platform.rcx;

/**
 * Listener of sensor events.
 * @see josx.platform.rcx.Sensor#addSensorListener
 */
public interface SensorListener
{
  /**
   * Called when the canonical value of the sensor changes.
   * @param aSource The sensor that generated the event.
   * @param aOldValue The old sensor value.
   * @param aNewValue The new sensor value.
   */
  public void stateChanged (Sensor aSource, int aOldValue, int aNewValue);
}





