/* For debugging purpose.

 Copyright (c) 1997-2000 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Green (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.demo.mems.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Debug
/**

Debug class.

@author Allen Miu, Lukito Muliadi
@version $Id$
*/

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
  
