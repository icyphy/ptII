/* A request to connect two ports.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.actor.event;

import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// Connect
/**
A request to connect two ports.  When this request is executed, if
the container of either port implements the Actor interface, then both
the schedule and the type resolution are invalidated, forcing them to
be recomputed when they are next required. In addition,
if either port is an IOPort and is also an input, then this method calls its
createReceivers() method.  Notice that will result in the loss of any
data that might be present in the port.  This means that this type
of mutation could be tricky to use if determinism is important, since
it may be difficult to understand under what circumstances data will
be lost.

@author  Edward A. Lee
@version $Id$
@see ptolemy.kernel.CompositeEntity#connect
*/
public class Connect extends ChangeRequest {

    /** Construct a request with the specified originator and ports.
     *  @param originator The source of the change request.
     *  @param port1 The first port.
     *  @param port2 The second port.
     */
    public Connect(Nameable originator, ComponentPort port1,
            ComponentPort port2) {
        super(originator, "Connect port "
                + port1.getFullName() + " to " + port2.getFullName());
        _firstPort = port1;
        _secondPort = port2;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the connect() method of the
     *  container of the entity containing the first port.  In addition,
     *  if either port is an IOPort and is also an input, then call its
     *  createReceivers() method.  Also, notify the director that both
     *  type resolution and the schedule, if there is one, are invalid.
     *  @exception ChangeFailedException If the container rejects the
     *   connection, for example because it crosses hierarchy levels,
     *   or if the first port has no container.
     */
    public void execute() throws ChangeFailedException {
        try {
            ComponentEntity firstContainer
                = (ComponentEntity)_firstPort.getContainer();
            if (firstContainer == null) {
                throw new ChangeFailedException(this, "Cannot connect. "
                        + "First port has no container: "
                        + _firstPort.getFullName());
            }
            CompositeEntity container
                = (CompositeEntity)firstContainer.getContainer();
            if (container == null) {
                throw new ChangeFailedException(this, "Cannot connect. "
                        + "First port's container has no container: "
                        + _firstPort.getFullName());
            }
            container.connect(_firstPort, _secondPort);

            // Create receivers, if appropriate.
            if (_firstPort instanceof IOPort) {
                IOPort port = (IOPort)_firstPort;
                if (port.isInput()) {
                    port.createReceivers();
                }
            }
            if (_secondPort instanceof IOPort) {
                IOPort port = (IOPort)_secondPort;
                if (port.isInput()) {
                    port.createReceivers();
                }
            }

            // Invalidate schedule and type resolution if appropriate.
            Nameable secondContainer = _secondPort.getContainer();
            if (firstContainer instanceof Actor) {
                Director director = ((Actor)firstContainer).getDirector();
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            } else if (secondContainer instanceof Actor) {
                Director director = ((Actor)secondContainer).getDirector();
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        } catch (IllegalActionException ex) {
            throw new ChangeFailedException(this, ex);
        }
    }

    /** Get the first port.
     *  @return The first port to connect.
     */
    public ComponentPort getFirstPort() {
        return _firstPort;
    }

    /** Get the second port.
     *  @return The second port to connect.
     */
    public ComponentPort getSecondPort() {
        return _secondPort;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The first port.
    private ComponentPort _firstPort;

    // The second port.
    private ComponentPort _secondPort;
}
