package josx.platform.rcx;

/**
 * Only the most basic APIs from Sound.
 */
public class MinSound
{
  private MinSound()
  {
  }

  /**
   * @deprecated
   */
  public static void playTone (int aFrequency, int aDuration)
  {
    Native.callRom3 ((short) 0x327c, (short) 0x1773, (short) aFrequency, 
                  (short) aDuration);
  }
}
