package josx.platform.rcx;

/**
 * Constants for Sensor methods.
 * @see josx.platform.rcx.Sensor#setTypeAndMode
 */
public interface SensorConstants
{
	public static final int SENSOR_TYPE_RAW    = 0;
	public static final int SENSOR_TYPE_TOUCH  = 1;
	public static final int SENSOR_TYPE_TEMP   = 2;
	public static final int SENSOR_TYPE_LIGHT  = 3;
	public static final int SENSOR_TYPE_ROT    = 4;
	
	public static final int SENSOR_MODE_RAW    = 0x00;
	public static final int SENSOR_MODE_BOOL   = 0x20;
	public static final int SENSOR_MODE_EDGE   = 0x40;
	public static final int SENSOR_MODE_PULSE  = 0x60;
	public static final int SENSOR_MODE_PCT    = 0x80;
	public static final int SENSOR_MODE_DEGC   = 0xa0;
	public static final int SENSOR_MODE_DEGF   = 0xc0;
	public static final int SENSOR_MODE_ANGLE  = 0xe0;
}
