
import tinyvm.rcx.*;

public class Test30
{
  static double operate (double a, double b)
  {
    return b / a;	  
  }
  
  public static void main (String[] arg)
  throws Exception
  {
    float f1 = (float) 1.53;
    float f2 = 3.45f;
    float f3 = (float) (operate(f1, f2) * 100);
    LCD.showNumber ((short) f3);
    Thread.sleep (3000);
    double d1 = 1.5e+3;
    double d2 = 3.4e+3;
    double d3 = (d1 - d2) / 10;
    LCD.showNumber ((short) (-d3));
    Thread.sleep (3000);
    if (f1 < f2)
      LCD.showNumber (1);
    else
      LCD.showNumber (2);	    
    Thread.sleep (3000);
    if (f2 >= f3)
      LCD.showNumber (3);
    else
      LCD.showNumber (4);	    
    Thread.sleep (3000);
    if (d3 < 0)
      LCD.showNumber (5);
    else
      LCD.showNumber (6);	    
    Thread.sleep (3000);
    if (d3 != -1.9e+2)
      LCD.showNumber (7);
    else
      LCD.showNumber (8);	    
    Thread.sleep (3000);
  }
}
