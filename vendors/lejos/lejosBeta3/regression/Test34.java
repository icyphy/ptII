import josx.platform.rcx.*;

// ButtonListener test

public class Test34
{
  static MyListener LISTENER = new MyListener();
  
  public static void main (String[] arg)
  {
    Button.VIEW.addButtonListener (LISTENER);
    Button.RUN.addButtonListener (LISTENER);
    Button.PRGM.addButtonListener (LISTENER);
  }

  static class MyListener implements ButtonListener
  {
    int ctr = 0;
    
    public void buttonPressed (Button b)
    {
      LCD.showNumber (ctr++);    
    }
    
    public void buttonReleased (Button b)
    {
      if (b == Button.VIEW)
        Sound.beep();
      else if (b == Button.RUN)
	Sound.twoBeeps();
      else
	Sound.beepSequence();
    }
  }
}
