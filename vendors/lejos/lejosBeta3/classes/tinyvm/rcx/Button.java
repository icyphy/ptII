package tinyvm.rcx;

/**
 * Abstraction for an RCX button.
 * Example:<p>
 * <code><pre>
 *    Button.RUN.waitForPressAndRelease();
 *    Sound.playTone (1000, 1);
 * </pre></code>
 */
public class Button
{
  /**
   * The View button.
   */
  public static final Button VIEW = new Button (0x02);
  /**
   * The Prgm button.
   */
  public static final Button PRGM = new Button (0x04);
  /**
   * The Run button.
   */
  public static final Button RUN = new Button (0x01);

  private int iCode;

  private Button (int aCode)
  {
    iCode = aCode;
  }

  /**
   * @return <code>true</code> if button is pressed, <code>false</code> otherwise.
   */
  public final boolean isPressed()
  {
    return (readButtons() & iCode) != 0;
  }

  /**
   * Loops until the button is released.
   */
  public final void waitForPressAndRelease()
  {
    while (!isPressed()) {}
    while (isPressed()) {}
  }

  /**
   * <i>Low-level API</i> that reads status of buttons.
   * @return An integer with possibly some bits set: 0x02 (view button pressed)
   * 0x04 (prgm button pressed), 0x01 (run button pressed). If all buttons 
   * are released, this method returns 0.
   */
  public static int readButtons()
  {
    synchronized (Native.MEMORY_MONITOR)
    {
      int pAddr = Native.iAuxDataAddr;
      Native.callRom2 ((short) 0x1fb6, (short) 0x3000, (short) pAddr);
      return Native.readMemoryShort (pAddr);
    }
  }
}

