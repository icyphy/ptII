import tinyvm.rcx.*;

// Test for instanceof and checkcast on null

public class Test28
{
  public static void main (String[] arg) 
  {
    Object x = null;
    if (!(x instanceof Test28))
      LCD.showNumber (1);
    if (!(x instanceof Object))
      LCD.showNumber (2);
    Test28 y = (Test28) x;
    x = new Test28();
    y = (Test28) x;
    if (x instanceof Test28)
      LCD.showNumber (3);
    try {
      x = new Object();
      y = (Test28) x;
    } catch (ClassCastException e) {
      LCD.showNumber (4);
    }
  }
}
