/* A request to remove an entity.

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
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// RemoveActor
/**
A request to remove an entity.  The execute() method disconnects it from
the topology and sets its container to null.  In addition, the wrapup() method
will be called on the entity if it is an actor and createReceivers() 
will be called on all remote input IOports that were
connected to the actor.

@author  Edward A. Lee, Steve Neuendorffer
@version $Id$
@see ptolemy.actor.Actor
*/
public class RemoveActor extends ChangeRequest {

    /** Construct a request with the specified originator and
     *  actor to be removed. The actor must also implement the
     *  Nameable interface or a ClassCastException will occur.
     *  @param originator The source of the change request.
     *  @param actor The actor to remove.
     */
    public RemoveActor(Nameable originator, ComponentEntity entity) {
        super(originator, "Remove " + entity.getFullName());
	_entity = entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the wrapup() method of the
     *  actor, then disconnecting all its ports and setting its container
     *  to null.  This method also notifies the director that the
     *  schedule and type resolution may be invalid.
     *  @exception ChangeFailedException If the wrapup() method throws an
     *   exception, or if the actor is not an instance of ComponentEntity.
     */
    public void execute() throws ChangeFailedException {
        try {
	    if(_entity instanceof Actor) {
		Actor actor = ((Actor)_entity);
		Manager manager = actor.getManager();
		// FIXME: This is not enough..  we need to figure out if the
		// model is running or not.
		if(manager != null) {
		    ((Actor)_entity).wrapup();
		}
	    }
            Iterator ports = _entity.portList().iterator();
            List farPortList = new LinkedList();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof IOPort) {
                    farPortList.addAll(
			((IOPort)port).deepConnectedInPortList());
                }
                port.unlinkAll();
            }
	    if(_entity instanceof Actor) {
		Director director = ((Actor)_entity).getDirector();
		if(director != null) {
		    director.invalidateSchedule();
		    director.invalidateResolvedTypes();
		    
		    Iterator farPorts = farPortList.iterator();
		    while (farPorts.hasNext()) {
			Port port = (Port)farPorts.next();
			if (port instanceof IOPort) {
			    ((IOPort)port).createReceivers();
			}
		    }
		}
	    }
            _entity.setContainer(null);
        } catch (KernelException ex) {
            throw new ChangeFailedException(this, ex);
        }
    }

    /** Get the actor.
     *  @return The actor to be removed.
     */
    public ComponentEntity getEntity() {
        return _entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor to remove.
    private ComponentEntity _entity;
}
