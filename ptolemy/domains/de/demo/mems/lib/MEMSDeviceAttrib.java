package ptolemy.domains.de.demo.mems.lib;

/* this object is immutable */
public class MEMSDeviceAttrib {

  /* MEMS Device clock period for processing messages */
  private final double _clock = 1.0;

  /* probe processing time values */
  private final double _probeTemperatureProcTime = 2.0;

  /* defines the radius of the signal reception range */
  private double _range;

  /* the size of circular buffer that holds the id of seen messages */
  private final int _seenMsgBufSize = 256;

  /* the threshold temperature of which, when exceeded, 
     causes the MEMSDevice to trigger an alarm message */
  private final double _thermoAlarmThreshold = 72.0;

  /* message processing time values */
  private final double _thermoAlarmProcTime = 3.0;

  /* FIXME: might want to add bandwidth parameters (also in 
     MEMSEnvirMsg to calc xfertime) */

  /** Constructs a MEMSDeviceAttrib that describes the physical 
   *  characteristics of a particular MEMSDevice.
   */
  public MEMSDeviceAttrib(double range) {
    _range = range;
  }

  /* arranged in alphabetical order */
  public double getClockPeriod() { return _clock; }
  public double getProbeTempProcTime() { return _probeTemperatureProcTime; }
  public double getRange() { return _range; }
  public int    getSeenMsgBufSize() { return _seenMsgBufSize; }
  public double getThermoAlarmThreshold() { return _thermoAlarmThreshold; }
  public double getThermoAlarmProcTime()  { return _thermoAlarmProcTime; }

}
