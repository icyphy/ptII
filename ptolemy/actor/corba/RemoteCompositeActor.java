/* A CompositeActor that catches CORBA exceptions.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (davisj@eecs.berkeley.edu)
*/

package ptolemy.actor.corba;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// RemoteCompositeActor
/**
A RemoteCompositeActor is a CompositeActor that catches CORBA
exceptions. The purpose of a RemoteCompositeActor (RCA) is to
facilitate CORBA remote object invocation of actors and composite
actors during the execution of a Ptolemy II model.
<P>
The remote object invocation infrastructure incorporated into
Ptolemy II allows dynamic, remote access to actors (atomic or
composite). The purpose of an RCA is to isolate the Ptolemy II
manager from CORBA-related, network errors that can occur when
invoking remote objects. This is necessary to prevent the simulation
environment from shutting down if there is a CORBA exception. To
accomplish this task, RCA overrides several methods of CompositeActor
to catch org.omg.CORBA.SystemException.
<P>
CORBA, as with most remote object invocation systems (e.g., Java
RMI and Microsoft's DCOM), uses a client side stub and a server
side skeleton. Consider a topology in which a local actor, actor A,
is connected to a remote actor, actor B. In this case, actor A
would connect to a stub representing actor B. Calls made on B's
stub would invoke the appropriate response from B's skeleton running
on the remote server.
<P>
All stubs contained in a model must be contained either directly or
indirectly by an RCA. The RCA will catch any CORBA exceptions thrown
on behalf of the stub. Note that while stubs must be contained by an
RCA, it is not necessary that an RCA contain stubs. An RCA can be
chosen to be a top level composite actor or it can be located must
lower in a model's hierarchy. Since an RCA is opaque, it must contain
a local director and hence it must self contain a model of computation.

@author John S. Davis II
@version $Id$
*/
public class RemoteCompositeActor extends CompositeActor {

    /** Construct a RemoteCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory. A RemoteCompositeActor must be opaque so you
     *  must set the director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     */
    public RemoteCompositeActor() {
        super();
    }

    /** Construct a RemoteCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. A RemoteCompositeActor must be opaque
     *  so you must set the director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     * @param workspace The workspace that will list the actor.
     */
    public RemoteCompositeActor(Workspace workspace) {
	super(workspace);
    }

    /** Create a RemoteCompositeActor with a name and a container. The
     *  container argument must not be null, or a NullPointerException
     *  will be thrown. This actor will use the workspace of the container
     *  for synchronization and version counts. If the name argument is
     *  null, then the name is set to the empty string. Increment the
     *  version of the workspace. This actor will have no local director
     *  initially, and its executive director will be simply the director
     *  of the container. RemoteCompositeActors must be opaque so a
     *  local director must set for this actor prior to execution.
     *
     * @param container The container actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the container is incompatible
     *  with this actor.
     * @exception NameDuplicationException If the name coincides with
     *  an actor already in the container.
     */
    public RemoteCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a remote composite actor with clones of the ports
     *  of the original actor, the contained actors, and the contained
     *  relations. The ports of the returned actor are not connected to
     *  anything. The connections of the relations are duplicated in the
     *  new composite, unless they cross levels, in which case an exception
     *  is thrown. The local director is cloned, if there is one.
     *  The executive director is not cloned.
     *  NOTE: This will not work if there are level-crossing transitions.
     *
     * @param ws The workspace for the cloned object.
     * @exception CloneNotSupportedException If the actor contains
     *  level crossing transitions so that its connections cannot be
     *  cloned, or if one of the attributes cannot be cloned.
     * @return A new RemoteCompositeActor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        RemoteCompositeActor newobj =
            (RemoteCompositeActor)super.clone(ws);
	if( getDirector() != getExecutiveDirector() ) {
	    if( getDirector() != null ) {
	        newobj._hasLocalDirector = true;
	    }
	}

        return newobj;
    }

    /** Invoke the fire() method on this actor's local director. If
     *  this actor is not opaque and does not contain a local director
     *  then throw an IllegalActionException. If any CORBA
     *  SystemExceptions are thrown, then throw an
     *  IllegalActionException with a corresonding message.
     *
     * @exception IllegalActionException If there is no director, or
     *  if the director's fire() method throws it, or if the actor is
     *  not opaque, or if a CORBA SystemException has been caught.
     */
    public void fire() throws IllegalActionException {
        try {
            if( !_hasLocalDirector ) {
                throw new IllegalActionException( this, "Cannot"
                        + " invoke fire on a non-opaque actor.");
            }
            super.fire();
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    /** If this actor is opaque, create receivers, and then
     *  invoke the initialize() method of its local
     *  director. Otherwise, throw an exception.
     *  If any CORBA SystemExceptions are thrown, then throw an
     *  IllegalActionException with a corresonding message.
     *
     * @exception IllegalActionException If there is no director, or if
     *  the director's initialize() method throws it, or if this actor
     *  is not opaque, or if a CORBA SystemException has been caught.
     */
    public void initialize() throws IllegalActionException {
        try {
            if( !_hasLocalDirector ) {
                throw new IllegalActionException( this, "Cannot"
                        + " initialize a non-opaque actor.");
            }
            super.initialize();
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    /** Return true to indicate that this actor is opaque. The
     *  local director of this actor must be set, but this
     *  method will always return true to prevent external
     *  components from accessing the contents of this actor.
     */
    public boolean isOpaque() {
        return true;
    }

    /** If this actor is opaque, invoke the postfire() method of its
     *  local director and transfer output data.
     *  Specifically, transfer any data from the output ports of this composite
     *  to the ports connected on the outside. The transfer is accomplished
     *  by calling the transferOutputs() method of the executive director.
     *  If there is no executive director, then no transfer occurs.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if the execution can continue into the next iteration.
     *  @exception IllegalActionException If there is no director,
     *   or if the director's postfire() method throws it, or if this
     *   actor is not opaque, or if a CORBA SystemException has been caught.
     */
    public boolean postfire() throws IllegalActionException {
        try {
            if( !_hasLocalDirector ) {
                throw new IllegalActionException( this, "Cannot"
                        + " invoke postfire on a non-opaque actor.");
            }
            return super.postfire();
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    /** If this actor is opaque, transfer input data and invoke the prefire()
     *  method of the local director. Specifically, transfer any data from
     *  the input ports of this composite to the ports connected on the inside.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  This method returns true if the actor is
     *  ready to fire (determined by the prefire() method of the director).
     *  It is read-synchronized on the workspace.
     *
     * @exception IllegalActionException If there is no director,
     *  or if the director's prefire() method throws it, or if this actor
     *  is not opaque, or if a CORBA SystemException has been caught.
     */
    public boolean prefire() throws IllegalActionException {
        try {
            if( !_hasLocalDirector ) {
                throw new IllegalActionException( this, "Cannot"
                        + " invoke prefire on a non-opaque actor.");
            }
            return super.prefire();
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    /** Set the local director for execution of this CompositeActor.
     *  Calling this method with a non-null argument makes this entity opaque.
     *  Calling it with a null argument makes it transparent.
     *  The container of the specified director is set to this composite
     *  actor, and if there was previously a local director, its container
     *  is set to null. This method is write-synchronized on the workspace.
     *
     * @param director The Director responsible for execution.
     * @exception IllegalActionException If the director is not in
     *  the same workspace as this actor or if the director is null. It
     *  may also be thrown in derived classes if the director is not
     *  compatible.
     */
    public void setDirector(Director director) throws IllegalActionException {
    	super.setDirector(director);
        _hasLocalDirector = true;
    }

    /** If this actor is opaque, then invoke the wrapup() method of the local
     *  director. This method is read-synchronized on the workspace.
     *
     * @exception IllegalActionException If there is no director,
     *  or if the director's wrapup() method throws it, or if this
     *  actor is not opaque, or if a CORBA SystemException has been caught.
     */
    public void wrapup() throws IllegalActionException {
        try {
            if( !_hasLocalDirector ) {
                throw new IllegalActionException( this, "Cannot"
                        + " invoke wrapup on a non-opaque actor.");
            }
            super.wrapup();
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an actor to this container with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  implements the Actor interface and to invalidate the schedule
     *  and type resolution. This method does not alter the actor in
     *  any way. It is <i>not</i> synchronized on the workspace, so
     *  the caller should be.
     *
     * @param entity Actor to contain.
     * @exception IllegalActionException If the actor has no name, or the
     *  action would result in a recursive containment structure, or the
     *  argument does not implement the Actor interface, or if a CORBA
     *  SystemException is caught.
     * @exception NameDuplicationException If the name collides with a name
     *  already on the actor contents list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        try {
            super._addEntity(entity);
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    /** Add a port to this actor. This overrides the base class to
     *  throw an exception if the added port is not an instance of
     *  IOPort.  This method should not be used directly.  Call the
     *  setContainer() method of the port instead. This method does not set
     *  the container of the port to point to this actor.
     *  It assumes that the port is in the same workspace as this
     *  actor, but does not check.  The caller should check.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     * @param port The port to add to this actor.
     * @exception IllegalActionException If the port class is not
     *  acceptable to this actor, or the port has no name, or if
     *  a CORBA SystemException is caught.
     * @exception NameDuplicationException If the port name collides with a
     *  name already in the actor.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        try {
            super._addPort(port);
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    /** Add a relation to this container. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set the container of the relation to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     * @param relation Relation to contain.
     * @exception IllegalActionException If the relation has no name, or
     *  is not an instance of IORelation, or if a CORBA SystemException is
     *  caught.
     * @exception NameDuplicationException If the name collides with a name
     *  already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        try {
            super._addRelation(relation);
        } catch( org.omg.CORBA.SystemException e ) {
            throw new IllegalActionException( this, e.getMessage() );
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _hasLocalDirector = false;

}
