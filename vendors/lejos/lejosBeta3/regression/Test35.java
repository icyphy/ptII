
import josx.platform.rcx.*;
import java.util.Random;

public class Test35
{
    static Random RAND = new Random (System.currentTimeMillis());
    
    public static void main (String[] args)
    throws Exception
    {
      int r = RAND.nextInt();
      if (r < 0)
	r = -r;
      LCD.showNumber (r % 100);
      Thread.sleep (500);      
    }
}
