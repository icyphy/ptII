
// Simple anonymous threads

import tinyvm.rcx.*;

public class Test12
{ 
  public int iCounter;
  public Object MONITOR = new Object();

  public void plainMethod()
  {
    int p = iCounter;
    for (int k = 0; k < 10; k++) { }
    iCounter = p + 1;
  }

  public synchronized void syncMethod()
  {
    int p = iCounter;
    iCounter = p + 1;
  }    

  public void syncMethod2()
  {
    synchronized (MONITOR)
    {
      int p = iCounter;
      iCounter = p + 1;
    }
  }    

  public static void main (String[] argv)
  {
    final Test12 pObj1 = new Test12();
    final Test12 pObj2 = new Test12();
    final Test12 pObj3 = new Test12();

    new Thread()
    {
      public void run()
      {
        for (int i = 0; i < 20; i++)
	{
          pObj1.plainMethod();
          pObj2.syncMethod();
          pObj3.syncMethod2();
	}
      }
    }.start();
    for (int i = 0; i < 20; i++)
    {
      pObj1.plainMethod();
      pObj2.syncMethod();
      pObj3.syncMethod2();
    }
    for (int i = 0; i < 1000; i++) {}
    LCD.showNumber (pObj1.iCounter);
    for (int i = 0; i < 50000; i++) {}
    LCD.showNumber (pObj2.iCounter);
    for (int i = 0; i < 50000; i++) {}
    LCD.showNumber (pObj3.iCounter);
    for (int i = 0; i < 50000; i++) {}    
  }
}
