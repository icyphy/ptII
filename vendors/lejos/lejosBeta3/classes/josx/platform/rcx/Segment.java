package josx.platform.rcx;

/**
 * LCD segment constants.
 */
public interface Segment
{
  public static final int STANDING = 0x3006;
  public static final int WALKING = 0x3007;
  public static final int SENSOR_1_VIEW = 0x3008;
  public static final int SENSOR_1_ACTIVE = 0x3009;
  public static final int SENSOR_2_VIEW = 0x300a;
  public static final int SENSOR_2_ACTIVE = 0x300b;
  public static final int SENSOR_3_VIEW = 0x300c;
  public static final int SENSOR_3_ACTIVE = 0x300d;
  public static final int MOTOR_A_VIEW = 0x300e;
  public static final int MOTOR_A_REV = 0x300f;
  public static final int MOTOR_A_FWD = 0x3010;
  public static final int MOTOR_B_VIEW = 0x3011;
  public static final int MOTOR_B_REV = 0x3012;
  public static final int MOTOR_B_FWD = 0x3013;
  public static final int MOTOR_C_VIEW = 0x3014;
  public static final int MOTOR_C_REV = 0x3015;
  public static final int MOTOR_C_FWD = 0x3016;
  public static final int DATALOG = 0x3018;
  public static final int DOWNLOAD = 0x3019;
  public static final int UPLOAD = 0x301a;
  public static final int BATTERY = 0x301b;
  public static final int RANGE_SHORT = 0x301c;
  public static final int RANGE_LONG = 0x301d;
  public static final int ALL = 0x3020;
}
