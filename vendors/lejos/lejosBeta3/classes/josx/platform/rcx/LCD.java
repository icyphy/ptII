package josx.platform.rcx;

/**
 * LCD routines.
 */
public class LCD
{
  private LCD()
  {
  }

  /**
   * Shows an unsigned number on the LCD.
   * No need to refresh.
   *
   * @param aValue An unsigned number in [0, 9999].
   */
  public static void showNumber (int aValue)
  {
    setNumber (0x301f, aValue, 0x3002);
    refresh();
  }

  /**
   * Shows an digit in the Program section of the LCD.
   * No need to refresh.
   *
   * @param aValue A number in [0, 9].
   */
  public static void showProgramNumber (int aValue)
  {
    setNumber (0x3017, aValue, 0);
    refresh();
  }

  /**
   * Sets a number to be displayed in the LCD.
   * It does not show up until refresh() is called.
   * @param aCode SIGNED=0x3001, PROGRAM=0x3017, UNSIGNED=0x301f
   * @param aValue The value to be displayed.
   * @param aPoint 0x3002 - 0x3005, indicating decimal point location.
   */
  public static void setNumber (int aCode, int aValue, int aPoint)
  {
    Native.callRom3 ((short) 0x1ff2, (short) aCode, (short) aValue, (short) aPoint);
  } 

  /**
   * Refreshes LCD. Has to be called for certain LCD methods
   * to take effect.
   */
  public static void refresh()
  {
    Native.callRom0 ((short) 0x27c8);
  }

  /**
   * Sets an LCD segment. Requires refresh.
   * @param aCode One of the following numbers:<p>
   * STANDING         0x3006<br>
   * WALKING          0x3007<br>
   * SENSOR_0_VIEW    0x3008<br>
   * SENSOR_0_ACTIVE  0x3009<br>
   * SENSOR_1_VIEW    0x300a<br>
   * SENSOR_1_ACTIVE  0x300b<br>
   * SENSOR_2_VIEW    0x300c<br>
   * SENSOR_2_ACTIVE  0x300d<br>
   * MOTOR_0_VIEW     0x300e<br>
   * MOTOR_0_REV      0x300f<br>
   * MOTOR_0_FWD      0x3010<br>
   * MOTOR_1_VIEW     0x3011<br>
   * MOTOR_1_REV      0x3012<br>
   * MOTOR_1_FWD      0x3013<br>
   * MOTOR_2_VIEW     0x3014<br>
   * MOTOR_2_REV      0x3015<br>
   * MOTOR_2_FWD      0x3016<br>
   * DATALOG          0x3018<br>
   * DOWNLOAD         0x3019<br>
   * UPLOAD           0x301a<br>
   * BATTERY          0x301b<br>
   * RANGE_SHORT      0x301c<br>
   * RANGE_LONG       0x301d<br>
   * ALL              0x3020<br>
   *
   * @see josx.platform.rcx.LCD#clearSegment
   */
  public static void setSegment (int aCode)
  {
    Native.callRom1 ((short) 0x1b62, (short) aCode);
  }

  /**
   * Clears an LCD segment. Requires refresh.
   * @see josx.platform.rcx.LCD#setSegment
   */
  public static void clearSegment (int aCode)
  {
    Native.callRom1 ((short) 0x1e4a, (short) aCode);
  }

  /**
   * Clears the display. Requires refresh.
   */
  public static void clear()
  {
    Native.callRom0 ((short) 0x27ac);
  }
}

