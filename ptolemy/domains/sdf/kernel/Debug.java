/*
@Copyright (c) 1998 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
*/

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

