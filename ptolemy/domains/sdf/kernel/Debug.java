
/* This is an static class that allows debug print statements to be 
   inserted into code and easily ignored when not needed. 
@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

/** I wrote this class in an effort to provide a nice way to deal with 
    inserting debugging print statements into code.   It is often a nicer
    way to trace code than by stepping through with jdb and this mechanism 
    allows the more useful of these trace statements to remain in code with 
    little overhead.

@author Steve Neuendorffer
@version $Id$
*/

import collections.HashedSet;
import java.util.Enumeration;
import ptolemy.kernel.util.InvalidStateException;

public class Debug {

    /** Throw an exception if expr is not true. 
     *
     *  @param expr an expression to be evaluted
     *  @exception InvalidStateException if expression evaluates to false.
     */
    public static void assert(boolean expr) {
        if(!expr) {
            InvalidStateException exception = 
                new InvalidStateException("Assertion Failed");
            println(exception.toString());
        }
    }

    /** Publish an event string to all debug listeners.
     *  
     *  @param eventstring String to be issued
     */
    public static void print(String eventstring) {
        Enumeration listeners = debuglisteners.elements();
        while(listeners.hasMoreElements()) {
            DebugListener listener = (DebugListener) listeners.nextElement();
            listener.tell(eventstring);
        }
    }


    /** Publish an event string to all debug listeners, terminated with EOLN.
     *  
     *  @param eventstring String to be issued
     */
    public static void println(String eventstring) {
        print(eventstring + "\n");
    }

    /** Register a new debug listener.
     *  
     *  @param DebugListener to be registered.
     */
    public static void register(DebugListener d) {
        debuglisteners.include(d);
    }

    /** Remove a debug listener from the list of registered listeners.
     *  
     *  @param DebugListener to be unregistered.
     */
    public static void unregister(DebugListener d) {
        debuglisteners.exclude(d);
    }

    // A HashedSet of all our debuglisteners
    static HashedSet debuglisteners;
    
    static { 
        debuglisteners = new HashedSet();
    }
}

