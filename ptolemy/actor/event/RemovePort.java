/* A request to remove a port.

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
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.CompositeActor;

import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// RemovePort
/**
A request to remove a port.  The execute() method of this request
unlinks the port from all relations and sets its container to null.

@author  Edward A. Lee
@version $Id$
@see ptolemy.kernel.Port
*/
public class RemovePort extends ChangeRequest {

    /** Construct a request with the specified originator and
     *  port to be removed.
     *  @param originator The source of the change request.
     *  @param port The port to remove.
     */
    public RemovePort(Nameable originator, Port port) {
        super(originator, "Remove " + port.getFullName());
        _port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the unlinkAll() method of the
     *  port, then setting its container
     *  to null.  If the port is contained by an instance of
     *  Actor, then this method also notifies its director that the
     *  schedule and type resolution may be invalid.
     *  @exception ChangeFailedException If removing or unlinking the
     *   port fails with an exception.
     */
    public void execute() throws ChangeFailedException {
        try {
            _port.unlinkAll();
            Nameable container = _port.getContainer();
            if (container instanceof Actor) {
                Director director = ((Actor)container).getDirector();
		if(director != null) {
		    director.invalidateSchedule();
		    director.invalidateResolvedTypes();
		}
            }
            _port.setContainer(null);
        } catch (KernelException ex) {
            throw new ChangeFailedException(this, ex);
        }
    }

    /** Get the port.
     *  @return The port to be removed.
     */
    public Port getPort() {
        return _port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The port to remove.
    private Port _port;
}
