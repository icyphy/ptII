package josx.platform.rcx;

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

  /**
   * Array containing VIEW, PRGM and RUN, in that order.
   */
  public static final Button[] BUTTONS = { Button.VIEW, Button.PRGM, Button.RUN };
  
  private static final ButtonListenerThread LISTENER_THREAD = new ButtonListenerThread();

  private int iCode;
  private ButtonListener[] iListeners = new ButtonListener[4];
  private int iNumListeners;
  
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
    while (!isPressed())
      Thread.yield();
    while (isPressed())
      Thread.yield();
  }

  /**
   * Adds a listener of button events. Each button can serve at most
   * 4 listeners.
   */
  public synchronized void addButtonListener (ButtonListener aListener)
  {
    if (!LISTENER_THREAD.isAlive())
    {
      // Hack: Force initialization of Native
      Native.getDataAddress (null);
      // Initialize each button
      VIEW.iNumListeners = 0;
      RUN.iNumListeners = 0;
      PRGM.iNumListeners = 0;      
      // Start thread
      LISTENER_THREAD.start();
    }
    iListeners[iNumListeners++] = aListener;
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

  static class ButtonListenerThread extends Thread
  {    
    static boolean[] WAS_PRESSED = new boolean[3];
    
    public void run()
    {
      Button[] pButtons = BUTTONS;
      boolean[] pWasPressedArray = WAS_PRESSED;
      for (;;)
      {
        for (int i = 0; i < 3; i++)
        {
	  Button pButton = pButtons[i];
          boolean pPressed = pButton.isPressed();
	  if (pPressed != pWasPressedArray[i])
	  {
  	    synchronized (pButton)
	    {
              int pNumListeners = pButton.iNumListeners;
  	      ButtonListener[] pListeners = pButton.iListeners;
	      if (pPressed)
	      {
	        for (int j = 0; j < pNumListeners; j++)
	        {
	          pListeners[j].buttonPressed (pButton);
		  Thread.yield();
	        }
	      }
	      else
	      {
	        for (int j = 0; j < pNumListeners; j++)
	        {
	          pListeners[j].buttonReleased (pButton);
		  Thread.yield();
	        } 		    
	      }
	    }
	    pWasPressedArray[i] = pPressed;
  	  }
	  Thread.yield();
        }
      }	    
    }
  }
  
}

