/* A base class for DE domain actors.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEActor
/** A base class for DE domain actor. An actor in DE simulation does not
 *  necessarily derive from this class. This class provides methods to access
 *  time (since DE is a timed domain) and methods for an actor to reschedule
 *  itself in the future. Actors specializing in DE domain should derive from
 *  this class and thus can be written more succintly.
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see Actor
 */
public class DEActor extends TypedAtomicActor {

    /** Constructor a DEActor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEActor(TypedCompositeActor container, String name)
	 throws NameDuplicationException, IllegalActionException  {
      super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the current time from the director.
     *  @return The current time.
     */
    public double getCurrentTime() throws IllegalActionException {
        DEDirector dir = (DEDirector) getDirector();
        if (dir==null) {
            throw new IllegalActionException("No director available");
        }
        return dir.getCurrentTime();
    }

    /** Get the start time from the director.
     *  @return The start time.
     */
    public double getStartTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir==null) {
	    throw new IllegalActionException("No director available");
	}
	return dir.getStartTime();
    }

    /** Get the stop time from the director.
     *  @return The stop time.
     */
    public double getStopTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir==null) {
	    throw new IllegalActionException("No director available");
	}
	return dir.getStopTime();
    }

    /** Schedule this actor to be fired at a specified delay relative
     *  to the current time. If the delay specified is less than zero, then
     *  IllegalActionException will be thrown.
     *
     *  @param delay The delay, relative to the current time.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void fireAfterDelay(double delay) throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	// FIXME: the depth is equal to zero ???
        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.

        dir.fireAfterDelay(this, delay);
    }

    /** Schedule this actor to be fired at a specified time in the future.
     *  If the time is not in the future (i.e. less than the current time), 
     *  then throw an exception.
     *  @param timeStamp The time stamp at which the actor will be fired.
     *  @exception IllegalActionException If the time stamp is in the past.
     */
    public void fireAt(double timeStamp) throws IllegalActionException {
        DEDirector dir = (DEDirector)getDirector();
        dir.fireAt(this, timeStamp);
    }

    /** Empty all input ports.
     *
     *  @exception IllegalActionException Not thrown.
     */
    /*
    public void wrapup() throws IllegalActionException {
	super.wrapup();
	Enumeration inputs = inputPorts();
	while (inputs.hasMoreElements()) {
	    IOPort p = (IOPort)inputs.nextElement();
	    int width = p.getWidth();
	    for (int i = 0; i<width; i++) {
		while (p.hasToken(i)) {
		    try {
			p.get(i);
		    } catch (NoSuchItemException e) {
			e.printStackTrace();
			throw new InternalErrorException(e.getMessage());
		    }
		}
	    }

	}
    }
    */

  ///////////////////////////////////////////////////////////////////
  ////                         private variables                 ////

  // Private variables should not have doc comments, they should
  // have regular C++ comments.

}

