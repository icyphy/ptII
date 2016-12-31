/* Interface for classes that can orchestrate the execution of accessors.

   Copyright (c) 2014-2016 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.jjs;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// AccessorOrchestrator

/** Interface for classes that can orchestrate the execution of
 *  accessors. An implementation of this interface accepts requests
 *  to invoke callback functions (via the {@link #invokeCallback(Runnable)}
 *  method). It promises that all such callback functions are mutually
 *  atomic.
 *  
 *  This interface extends Nameable so that error reporting is more complete.
 *  
 *  FIXME: More
 *  
 *  @author Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (bilung)
 */
public interface AccessorOrchestrator extends Nameable {
    
    /** Clear the interval with the specified handle, if it
     *  has not already executed.
     *  @param timer The timeout handle.
     *  @throws IllegalActionException If the handle is invalid.
     *  @see #setTimeout(Runnable, int)
     *  @see #setInterval(Runnable, int)
     */
    public void clearInterval(Object timer) throws IllegalActionException;

    /** Clear the timeout with the specified handle, if it
     *  has not already executed.
     *  @param timer The timeout handle.
     *  @throws IllegalActionException If the handle is invalid.
     *  @see #setTimeout(Runnable, int)
     *  @see #setInterval(Runnable, int)
     */
    public void clearTimeout(Object timer) throws IllegalActionException;

    /** Invoke the specified function as soon as possible, but after any currently
     *  executing or other pending callbacks have completed.
     *  @param function The function to invoke.
     *  @exception IllegalActionException If the request cannot be honored.
     */
    public void invokeCallback(final Runnable function) throws IllegalActionException;

    /** Report an error. */
    public void error(String message);

    /** Print a message. */
    public void log(String message);
    
    /** Invoke the specified function after the specified amount of time and again
     *  at multiples of that time.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to first invoke it.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     *  @see #clearTimeout(Object)
     */
    public Object setInterval(Runnable function, int milliseconds) throws IllegalActionException;
    
    /** Invoke the specified function after the specified amount of time.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to invoke it.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     *  @see #clearTimeout(Object)
     */
    public Object setTimeout(Runnable function, int milliseconds) throws IllegalActionException;

    /** Cancel all pending callbacks and stop responding to future callbacks. */
    public void wrapup() throws IllegalActionException;
}
