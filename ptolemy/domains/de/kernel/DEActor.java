/* A base class for DE domain actors.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEActor
/** A convenience base class for actors in the DE domain.  This class
 *  provides methods that access features of the DEDirector.
 *  Actors in DE models need not derive from this class.
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see Actor
 */
public abstract class DEActor extends TypedAtomicActor implements TimedActor {

    /** Constructor an with the specified container and name.
     *  This is protected because there is no reason to create an instance
     *  of this class, but derived classes will want to invoke the
     *  constructor of the superclass.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    protected DEActor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the current time from the director.  This is a facade
     *  for the director method of the same name.
     *  @return The current time.
     *  @exception IllegalActionException If there is no director.
     */
    public double getCurrentTime() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this, "No director.");
        }
        return dir.getCurrentTime();
    }

    /** Get the start time from the director.
     *  @return The start time.
     *  @exception IllegalActionException If there is no director.
     */
    public double getStartTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir == null) {
	    throw new IllegalActionException(this, "No director.");
	}
	return dir.getStartTime();
    }

    /** Get the stop time from the director.  This is a facade for
     *  a method by the same name of DEDirector.
     *  @return The stop time.
     *  @exception IllegalActionException If there is no director,
     *   or if the director is not a DEDirector.
     */
    public double getStopTime() throws IllegalActionException {
	Director dir = getDirector();
	if (dir == null || !(dir instanceof DEDirector)) {
	    throw new IllegalActionException(this,
            "Director is not an instance of DEDirector.");
	}
	return ((DEDirector)dir).getStopTime();
    }

    /** Schedule this actor to be fired at a specified time in the future.
     *  If the time is not in the future (i.e. is less than the current
     *  time), then throw an exception.  This is a facade for the fireAt()
     *  method of the director.
     *  @param time The time at which the actor will be fired.
     *  @exception IllegalActionException If the time is in the past, or
     *   if there is no director.
     */
    public void fireAt(double time) throws IllegalActionException {
        Director dir = getDirector();
	if (dir == null) {
	    throw new IllegalActionException(this, "No director.");
	}
        dir.fireAt(this, time);
    }
}
