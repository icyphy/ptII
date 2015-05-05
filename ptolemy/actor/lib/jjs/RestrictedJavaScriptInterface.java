/* Restricted interface to the JavaScript actor.

   Copyright (c) 2014 The Regents of the University of California.
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


///////////////////////////////////////////////////////////////////
//// RestrictedJavaScriptInterface

/**
   Restricted interface to the {@link JavaScript} actor.
   An instance of this class provides limited access to the JavaScript actor
   specified in the constructor, given access only to methods that an untrusted
   script can safely execute.
   
   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class RestrictedJavaScriptInterface {
    
    /** Construct a restricted interface to the specified JavaScript actor.
     *  @param actor The actor.
     */ 
    public RestrictedJavaScriptInterface(JavaScript actor) {
	_actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the interval with the specified handle, if it has not already executed.
     *  @param handle The interval handle.
     *  @see #setInterval(Runnable, int)
     */
    public void clearInterval(Integer handle) {
        _actor.clearTimeout(handle);
    }

    /** Clear the timeout with the specified handle, if it has not already executed.
     *  @param handle The timeout handle.
     *  @see #setTimeout(Runnable, int)
     */
    public void clearTimeout(Integer handle) {
        _actor.clearTimeout(handle);
    }

    /** Delegate to the associated JavaScript actor to report an error.
     *  @param message The message
     */
    public void error(String message) {
	_actor.error(message);
    }

    /** Return true.
     *  A restricted JavaScript actor limits the capabilities available
     *  to the script it executes so that it can execute untrusted code. 
     *  This class is an interface to a restricted JavaScript actor,
     *  so it returns true.
     *  @return True.
     */
    public boolean isRestricted() {
        return true;
    }

    /** Delegate to the associated JavaScript actor to log a message.
     *  @param message The message
     */
    public void log(String message) {
	_actor.log(message);
    }

    /** Invoke the specified function after the specified interval and
     *  periodically after that.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param millisecond The number of milliseconds in the future to invoke it
     *   and the period thereafter.
     *  @return A unique ID for this callback
     *  @throws IllegalActionException If the director cannot respect the request.
     */
    public int setInterval(final Runnable function, int millisecond) throws IllegalActionException {
	return _actor.setInterval(function, millisecond);
    }

    /** Invoke the specified function after the specified amount of time.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param millisecond The number of milliseconds in the future to invoke it.
     *  @return A unique ID for this callback
     *  @throws IllegalActionException If the director cannot respect the request.
     */
    public int setTimeout(final Runnable function, int millisecond) throws IllegalActionException {
	return _actor.setTimeout(function, millisecond);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        Private Variables                  ////

    /** The actor to which this is providing an interface. */
    private JavaScript _actor;
}
