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
/**
A base class for DE domain actor. This class provides a higher level of
abstraction for actors to communicate with directors. 
FIXME: actor can access time only in the relative sense, shouldn't we also
provide ways for actors to access time in the absolute sense.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEActor extends AtomicActor {

    /** Constructor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The initial output event value.
     *  @param step The step size by which to increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEActor(CompositeActor container, String name)
	 throws NameDuplicationException, IllegalActionException  {
      super(container, name);
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /*
     */
    public double getCurrentTime() throws IllegalActionException {
        DEDirector dir = (DEDirector) getDirector();
        if (dir==null) {
            throw new IllegalActionException("No director available");
        }
        return dir.getCurrentTime();
    }
    
    /*
     */
    public double getStartTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir==null) {
	    throw new IllegalActionException("No director available");
	}
	return dir.getStartTime();
    }
    
    /*
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
     *  
     *  FIXME: Change the name, maybe refireAfterDelay()
     *
     *  @param delay The delay, relative to the current time.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void refireAtTime(double delay) throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	// FIXME: the depth is equal to zero ???
        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.
        long maxdepth = -1;
        Enumeration iports = this.inputPorts();
        while (iports.hasMoreElements()) {
            IOPort p = (IOPort) iports.nextElement();
            Receiver[][] r = p.getReceivers();
            if (r == null) continue;
            DEReceiver rr = (DEReceiver) r[0][0];
            if (rr._depth > maxdepth) {
                maxdepth = rr._depth;
            }
        }
        dir.enqueueEvent(this, delay, maxdepth+1);
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

