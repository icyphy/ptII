/* A request to link a port to a relation.

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

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Link
/**
A request to link a port to a relation.  When this request is executed, if
the container of the port implements the Actor interface, then both
the schedule and the type resolution are invalidated, forcing them to
be recomputed when they are next needed. In addition,
if the port is an IOPort and is also an input, then this method calls its
createReceivers() method.  Notice that will result in the loss of any
data that might be present in the port.  In addition, if the port is
an output port, then createReceivers() is called on all deeply connected
input ports.  This may again result in loss of data.  Notice that this
could make this class difficult to use deterministically.

@author  Edward A. Lee
@version $Id$
@see ptolemy.kernel.Port#link
*/
public class Link extends ChangeRequest {

    /** Construct a request with the specified originator, port, and relation.
     *  @param originator The source of the change request.
     *  @param port The port to link.
     *  @param relation The relation to link.
     */
    public Link(Nameable originator, ComponentPort port,
            ComponentRelation relation) {
        super(originator, "Link port "
                + port.getFullName() + " to relation "
                + relation.getFullName());
        _port = port;
        _relation = relation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the link() method of the port.
     *  In addition, if the port is an IOPort and is also an input,
     *  then call its
     *  createReceivers() method.  Also, notify the director that both
     *  type resolution and the schedule, if there is one, are invalid.
     *  @exception ChangeFailedException If the port rejects the
     *   link, for example because it crosses hierarchy levels.
     */
    public void execute() throws ChangeFailedException {
        try {
            _port.link(_relation);
            // Create receivers, if appropriate.
            if (_port instanceof IOPort) {
                IOPort port = (IOPort)_port;
                if (port.isInput()) {
                    port.createReceivers();
                }
                if (port.isOutput()) {
                    Iterator ports = port.deepConnectedInPortList().iterator();
                    while (ports.hasNext()) {
                        IOPort farPort = (IOPort)ports.next();
                        farPort.createReceivers();
                    }
                }
            }
            ComponentEntity portContainer
                = (ComponentEntity)_port.getContainer();
            if (portContainer != null) {
                if (portContainer instanceof Actor) {
                    Director director = ((Actor)portContainer).getDirector();
                    if (director != null) {
                        director.invalidateSchedule();
                        director.invalidateResolvedTypes();
                    }
                }
            }
        } catch (IllegalActionException ex) {
            throw new ChangeFailedException(this, ex);
        }
    }

    /** Get the port.
     *  @return The port to link.
     */
    public ComponentPort getPort() {
        return _port;
    }

    /** Get the relation.
     *  @return The relation to link.
     */
    public ComponentRelation getRelation() {
        return _relation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The port.
    private ComponentPort _port;

    // The relation.
    private ComponentRelation _relation;
}
