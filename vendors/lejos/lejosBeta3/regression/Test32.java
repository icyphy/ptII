
import josx.platform.rcx.*;

public class Test32
{
  public static void main (String[] arg)
  throws Exception
  {
    new Thread()
    {
      public void run()
      {
	for (;;)
	{
          try {
            Thread.sleep (10);
	  } catch (Exception e) {
	  }
          LCD.showNumber ((int) System.currentTimeMillis() / 100);
	}
      }	 
    }.start();
    
    for (;;)
    {
      Sound.beep();
      // Busy-loop to check performance
      for (int k = 0; k < 5000; k++) { }
    }
  }	
}
