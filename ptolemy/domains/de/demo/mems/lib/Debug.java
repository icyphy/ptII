
package ptolemy.domains.de.demo.mems.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;

/** Generic Debug class that implements a java-static log method
 *  with level control.
 */
public class Debug {
  static public int debugLevel = 0;
  static public final int ERR = 255;
  /** If level >= debugLevel, prints a log for debugging 
   *  purposes in stdout.
   *
   *  Format
   *    DEBUG_LEVEL MESSAGE
   */
  public static void log(int level, String message) {
    if (level == ERR) {
      System.out.println("*** ERR ***\t" + message);
    } else if (level >= debugLevel) {
      System.out.println(level + "\t" + message);
    }
  }    

  /** If level >= debugLevel, prints a formatted log for debugging 
   *  purposes in stdout.
   *
   *  The log has the following format:
   *    DEBUG_LEVEL DEBUG_HEADER[ACTOR_ID](t=CURRENT_TIME): MESSAGE
   */
  public static void log(int level, MEMSActor a, String message) {
    try {
    if (level == ERR) {
	System.out.println("*** ERR ***" + a.getDebugHeader() + 
			   "\tid=" + a.getID() + "\tt=" + 
			   a.getCurrentTime() + "\t" +message);
    } else if (level >= debugLevel) {
	System.out.println(level + " " + a.getDebugHeader() + 
			   "\tid=" + a.getID() + "\tt=" + 
			   a.getCurrentTime() + "\t" +message);
      }
    } catch (IllegalActionException e) {
      System.out.println("Warning!!! IllegalActionException caught in Debug.log");
    }
  }    
}
  
