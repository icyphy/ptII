package tinyvm.rcx;

/**
 * Implements currentTimeMillis() and
 * sleep() (as sleep wont work in the Thread
 * class).
 *
 * @author <a href="mailto:rvbijl39<at>calvin<dot>edu">Ryan VanderBijl</a> 
 */
public class Time
{
    private Time() 
    {
    }

    /**
     * Return the number of milliseconds the RCX has been running.
     * Warning: this variable will wrap around after approx 49.7 
     * days of continuous operation. However, a more important bug
     * is that you will need new batteries before then.
     * (unless you have RIS v1.0, and use the adapter).
     */
    public static final int currentTimeMillis()
    {
	return (int) System.currentTimeMillis();
    }

    /**
     * Cause the calling thread to "sleep" for at LEAST
     *  the specified number of milliseconds. Due to
     *  thread dispatching algorithm, it may sleep
     *  for longer. Native sleep will be implemented
     *  in leJOS. This is not busy waitting. (We
     *  call Thread.yield()).
     */
    public static void sleep(int milli)
    //  throws java.lang.InterruptedException
    {
	int stopAt = (int) System.currentTimeMillis() + milli;
	while ((int) System.currentTimeMillis() < stopAt)
	{
	    Thread.yield();
	}
    }

}
