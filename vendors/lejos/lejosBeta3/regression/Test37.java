
import josx.platform.rcx.*;
import java.util.Random;

public class Test37
{
   public static void traceNumber (int n, int p)
   throws Exception
   {
     LCD.showNumber (n);
     LCD.showProgramNumber (p);
     Thread.sleep (500);
   }
      
   public static void main (String[] args)
   throws Exception
   {
     traceNumber ((int) (Math.sin (Math.PI / 6.0) * 100.0), 1);
     traceNumber ((int) (Math.cos (1.56) * 1000.0), 2);
     traceNumber ((int) (Math.tan (Math.PI / 6.0) * 100.0), 3);
     traceNumber ((int) (Math.atan (1.0) * 100.0), 3);
     traceNumber ((int) (Math.sqrt (2.0) *  100), 4);
     //if (Math.isNaN (Math.sqrt (-1.0)))
       //traceNumber (999, 5);
   }
}
