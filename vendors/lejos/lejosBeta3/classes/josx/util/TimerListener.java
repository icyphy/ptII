package josx.util;

/**
 * Listener used with Timer.
 * 
 * @see josx.util.Timer
 * @author <a href="mailto:rvbijl39<at>calvin<dot>edu">Ryan VanderBijl</a> 
 */

public interface TimerListener
{
   /**
    * Called every time the Timer fires.
    */
   public void timedOut();
}
