package josx.platform.rcx;

/**
 * Only the most basic APIs from LCD.
 */
public class MinLCD
{
  private MinLCD()
  {
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
}

