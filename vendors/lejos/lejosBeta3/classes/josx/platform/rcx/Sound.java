package josx.platform.rcx;

/**
 * RCX sound routines.
 */
public class Sound
{
  private Sound()
  {
  }

  /**
   * Play a system sound.
   * <TABLE BORDER=1>
   * <TR><TH>aCode</TH><TH>Resulting Sound</TH></TR>
   * <TR><TD>0</TD><TD>short beep</TD></TR>
   * <TR><TD>1</TD><TD>double beep</TD></TR>
   * <TR><TD>2</TD><TD>descending arpeggio</TD></TR>
   * <TR><TD>3</TD><TD>ascending  arpeggio</TD></TR>
   * <TR><TD>4</TD><TD>long, low beep</TD></TR>
   * <TR><TD>5</TD><TD>quick ascending arpeggio</TD></TR>
   * </TABLE>
   */
  public static void systemSound (boolean aQueued, int aCode)
  {
    Native.callRom2 ((short) 0x299a, (short) (aQueued ? 0x4004 : 0x4003), (short) aCode);
  }

  /**
   * Beeps once.
   * @param aQueued Whether the sound is queued.
   */
  public static void beep()
  {
    systemSound (true, 0);
  }

  /**
   * Beeps twice.
   * @param aQueued Whether the sound is queued.
   */
  public static void twoBeeps()
  {
    systemSound (true, 1);
  }

  /**
   * Downward tones.
   * @param aQueued Whether the sound is queued.
   */
  public static void beepSequence()
  {
    systemSound (true, 2);
  }

  /**
   * Low buzz.
   * @param aQueued Whether the sound is queued.
   */
  public static void buzz()
  {
    systemSound (true, 4);
  }

  /**
   * Plays a tone, given its frequency and duration.
   */
  public static void playTone (int aFrequency, int aDuration)
  {
    Native.callRom3 ((short) 0x327c, (short) 0x1773, (short) aFrequency, 
                  (short) aDuration);
  }
}
