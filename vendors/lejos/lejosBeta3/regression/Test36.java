
import josx.platform.rcx.*;
import java.util.Random;

public class Test36
{   
   static Thread2 t2 = new Thread2();
   static boolean b;
   static int k;

   static int abs (int n)
   {
     return n;
   }
   
   public static void main (String[] args)
   throws Exception
   {
     t2.fCurrentSpeed = 1;
     int runForTime = 0;
     if (runForTime==0)
       t2.fPlanSpeed = 2;
     else if (runForTime > 0)
       t2.fPlanSpeed = 2;
     else
       t2.fPlanSpeed = 2;
     t2.fRunning = true;
     t2.fRunUntil = abs(t2.fPlanSpeed) + (Thread2.getValue() / 100);
     k = 4;
     int sum = t2.fCurrentSpeed + t2.fPlanSpeed + t2.fRunUntil + k;
     LCD.showNumber (sum);
     Thread.sleep (500);
   }

    static class Thread1 extends Thread
    {    
      public void run()
      {
      }
    }
    
    static class Thread2 extends Thread1
    {
      private int fCurrentSpeed=0;
      private int fPlanSpeed=0;
      private int fRunUntil=0;
      private boolean fRunning=false;
      private boolean fFalse=false;
      
      static int getValue()
      {
	return 100;
      }
      
      //public void run()
      //{
      //}
    }
}
