/* An aggregation of actors.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
Review changeRequest / changeListener code.
*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import java.io.IOException;
import java.io.Writer;



//////////////////////////////////////////////////////////////////////////
//// CompositeActor
/**
A CompositeActor is an aggregation of actors.  It may have a
<i>local director</i>, which is an object of class Director that
is responsible for executing the contained actors.
At the top level of a hierarchy, a composite actor (the toplevel
CompositeActor of the topology) will normally exist with a local Director,
and no container.  A composite actor at a lower level
of the hierarchy may also have a local director.  A composite actor
with a local director is <i>opaque</i>, and serves the role of the
<i>wormhole</i> from Ptolemy 0.x. Its ports are opaque, but it can
contain actors and relations.  The toplevel composite actor is also
with a Manager object that is responsible for managing any execution within
the topology at a high level.
<p>
The <i>executive director</i> of a composite actor is the local director of
the actor's container.   The toplevel composite actor has no executive
director, and getExecutiveDirector will return null.   For transparent
composite actors, the executive director and the local director will be the
same.
<p>
The getDirector() method returns the local director if there is one.
Otherwise, it returns the <i>executive director</i> of the CompositeActor,
 if there is one.  Whatever it returns is called (simply) the
<i>director</i> of the composite (it may be local or executive).  This
Director is responsible for the execution of all the actors contained
within the composite actor.
<p>
A composite actor must have an executive director in order communicate with
the hierarchy around it.   In fact, it cannot even receive data in its
 input ports without an executive director, since the executive director
is responsible for supplying the receivers to the ports.
The toplevel composite actor has no executive director and cannot have
ports that transmit data, but it can still be executed as long as it has a
local director.  If the getDirector() method returns null, then the
composite is not executable.
<p>
When a composite actor has both a director and an executive director, then
the model of computation implemented by the director need not be the
same as the model of computation implemented by the executive director.
This is the source of the hierarchical heterogeneity in Ptolemy II.
Multiple models of computation can be cleanly nested.
<p>
The ports of a CompositeActor are constrained to be IOPorts, the
relations to be IORelations, and the actors to be instances of
ComponentEntity that implement the Actor interface.  Derived classes
may impose further constraints by overriding newPort(), _addPort(),
newRelation(), _addRelation(), and _addEntity().
<p>
The container is constrained to be an instance of CompositeActor.
Derived classes may impose further constraints by overriding setContainer().

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer
@version $Id$
@see ptolemy.actor.IOPort
@see ptolemy.actor.IORelation
@see ptolemy.kernel.ComponentEntity
@see ptolemy.actor.Director
@see ptolemy.actor.Manager
*/
public class CompositeActor extends CompositeEntity implements Actor {

    /** Construct a CompositeActor in the default workspace with no container
     *  and an empty string as its name. Add the actor to the workspace
     *  directory.
     *  You should set the director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     */
    public CompositeActor() {
        super();
    }

    /** Construct a CompositeActor in the specified workspace with no container
     *  and an empty string as a name. You can then change the name with
     *  setName(). If the workspace argument is null, then use the default
     *  workspace.
     *  You should set the director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CompositeActor(Workspace workspace) {
	super(workspace);
    }

    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  You should set the director before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a composite actor with clones of the ports of the
     *  original actor, the contained actors, and the contained relations.
     *  The ports of the returned actor are not connected to anything.
     *  The connections of the relations are duplicated in the new composite,
     *  unless they cross levels, in which case an exception is thrown.
     *  The local director is cloned, if there is one.
     *  The executive director is not cloned.
     *  NOTE: This will not work if there are level-crossing transitions.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the actor contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new CompositeActor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        CompositeActor newobj = (CompositeActor)super.clone(ws);
        if (_director != null) {
            newobj._director = (Director)_director.clone();
	    newobj._director._makeDirectorOf(newobj);
        } else {
            newobj._director = null;
        }
        newobj._inputPortsVersion = -1;
        newobj._outputPortsVersion = -1;
        return newobj;
    }

    /** If this actor is opaque, invoke the fire() method of its local
     *  director. Otherwise, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    public void fire() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }
            // Use the local director to transfer inputs.
            Iterator inports = inputPortList().iterator();
            while(inports.hasNext()) {
                IOPort p = (IOPort)inports.next();
                _director.transferInputs(p);
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().fire();
            // Use the executive director to transfer outputs.
            Director edir = getExecutiveDirector();
            if (edir != null) {
                Iterator outports = outputPortList().iterator();
                while(outports.hasNext()) {
                    IOPort p = (IOPort)outports.next();
                    edir.transferOutputs(p);
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the attribute with the given name. The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is contained by a (deeply) contained attribute, port,
     *  relation, entity, or director.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    public Attribute getAttribute(String name) {
        try {
            _workspace.getReadAccess();
            // Check attributes and ports first.
            Attribute result = super.getAttribute(name);
            if (result == null) {
                // Check director.
                String[] subnames = _splitName(name);
                if (_director != null &&
                        _director.getName().equals(subnames[0])) {
                    // Director name matches.
                    if (subnames[1] != null) {
                        result = _director.getAttribute(subnames[1]);
                    }
                }
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the director responsible for execution of the contained
     *  actors.  This will be either the local director (if it exists) or the
     *  executive director (obtained using getExecutiveDirector()).
     *  This method is read-synchronized on the workspace.
     *
     *  @return The director responsible for invocation of inside actors.
     */
    public Director getDirector() {
        try {
            _workspace.getReadAccess();
            if (_director != null) return _director;
            return getExecutiveDirector();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the executive director of this CompositeActor.
     *  The container (if any) is queried for its (local) director.
     *  If it has none, or there
     *  is no container, then return null. This method is read-synchronized
     *  on the workspace.
     *
     *  @return The executive director of this composite actor.
     */
    public Director getExecutiveDirector() {
        try {
            _workspace.getReadAccess();
            CompositeActor container = (CompositeActor)getContainer();
            if (container != null) return container.getDirector();
            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the manager responsible for execution of this composite actor.
     *  If this is the toplevel composite actor, then return what was
     *  set with setManager().
     *  For others, recursively call on the container, until the
     *  toplevel composite actor is reached.
     *  This method is read-synchronized on the workspace.
     *  @see #setManager(Manager)
     *
     *  @return The Manager of the topology that contains the composite actor.
     */
    public Manager getManager() {
        try {
            _workspace.getReadAccess();
            if (_manager != null) return _manager;
            CompositeActor container = (CompositeActor)getContainer();
            if (container != null) return container.getManager();
            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this actor is opaque, invoke the initialize() method of its local
     *  director. Otherwise, throw an exception.
     *  Before that, if this is not a top-level composite actor,
     *  perform domain-specific initialization by calling the
     *  initialize(Actor) method of the executive director.
     *  This method is read-synchronized on the workspace, so the
     *  initialize() method of the director need not be (assuming it is only
     *  called from here).
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's initialize() method throws it, or if the actor is not
     *   opaque.
     */
    public void initialize() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }
            Director executive = getExecutiveDirector();
            if (executive != null) {
                executive.initialize(this);
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().initialize();
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the input ports of this actor.
     *  Note that this method returns the ports directly
     *  contained by this actor, whether they are transparent or not.
     *  This method is read-synchronized on the workspace.
     *  @return A list of IOPort objects.
     */
    public List inputPortList() {
        try {
            _workspace.getReadAccess();
            if(_inputPortsVersion != _workspace.getVersion()) {
                // Update the cache.
                List inports = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if( p.isInput()) {
                        inports.add(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = _workspace.getVersion();
            }

            return _cachedInputPorts;

        } finally {
            _workspace.doneReading();
        }
    }

    /** Return an enumeration of the input ports of this actor.
     *  Note that this method returns the ports directly
     *  contained by this actor, whether they are transparent or not.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use inputPortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration inputPorts() {
        return Collections.enumeration(inputPortList());
    }

    /** Return true if this actor contains a local director.
     *  Otherwise, return false.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     */
    public boolean isOpaque() {
        return _director != null;
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalant to invoking prefire(), fire(), and 
     *  postfire(), in that order. In an iteration, if prefire() 
     *  returns true, then fire() will be called once, followed by 
     *  postfire(). Otherwise, if prefire() returns false, fire() 
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.
     *  <p>
     *  This base class method actually invokes prefire(), fire(), 
     *  and postfire(), as described above, but a derived class
     *  may override the method to execute more efficient code.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
	int n = 0;
	while (n++ < count) {
	    if (prefire()) {
		fire();
		if(!postfire()) return Executable.STOP_ITERATING;
	    } else {
                return Executable.NOT_READY;
	    }
	}
	return Executable.COMPLETED;
    }

    /** Return a new receiver of a type compatible with the local director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @exception IllegalActionException If there is no local director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newInsideReceiver() throws IllegalActionException {
        if (_director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return _director.newReceiver();
    }

    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name)
            throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            IOPort port = new IOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "CompositeActor.newPort: Internal error: " +
                    ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a new receiver of a type compatible with the executive director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @exception IllegalActionException If there is no executive director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director dir = getExecutiveDirector();
        if (dir == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without an executive director.");
        }
        return dir.newReceiver();
    }

    /** Create a new IORelation with the specified name, add it to the
     *  relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of IORelation.
     *  This method is write-synchronized on the workspace.
     *
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already on the container's contents list.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            IORelation rel = new IORelation(this, name);
            return rel;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return an enumeration of the output ports.
     *  Note that this method returns the ports directly
     *  contained by this actor, whether they are transparent or not.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public List outputPortList() {
        try {
            _workspace.getReadAccess();
            if(_outputPortsVersion != _workspace.getVersion()) {
                _cachedOutputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if( p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }
                _outputPortsVersion = _workspace.getVersion();
            }
            return _cachedOutputPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return an enumeration of the output ports.
     *  Note that this method returns the ports directly
     *  contained by this actor, whether they are transparent or not.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use outputPortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration outputPorts() {
        return Collections.enumeration(outputPortList());
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
     *   actor is not opaque.
     */
    public boolean postfire() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot invoke postfire a non-opaque actor.");
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            return getDirector().postfire();
        } finally {
            _workspace.doneReading();
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
     *  @exception IllegalActionException If there is no director,
     *   or if the director's prefire() method throws it, or if this actor
     *   is not opaque.
     */
    public boolean prefire()
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot invoke prefire a non-opaque actor.");
            }

            return getDirector().prefire();
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this actor is opaque, create receivers, and then
     *  invoke the preinitialize() method of its local
     *  director. Otherwise, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  preinitialize() method of the director need not be, assuming
     *  it is only called from here.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    public void preinitialize() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            _createReceivers();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot preinitialize a non-opaque actor.");
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().preinitialize();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Queue a change request.  Defer the change request to the container
     *  of this entity.  If the entity has no container, then defer to its
     *  Manager.  If the entity has no manager then execute the request 
     *  immediately.
     *  @param change The requested change.
     *  @exception ChangeFailedException If the change request fails.
     */
    public void requestChange(ChangeRequest change) 
	throws ChangeFailedException {
	CompositeEntity container = (CompositeEntity) getContainer();
	if(container == null) {
	    Manager manager = getManager();
	    if(manager == null) {
		change.execute();
		notifyChangeListeners(change);
	    } else {
		manager.requestChange(change);
	    }
	} else {
	    container.requestChange(change);
	}
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of CompositeActor. If it is, call the base class
     *  setContainer() method.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the argument is not a CompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "CompositeActor can only be contained by instances of " +
                    "CompositeActor.");
        }
        super.setContainer(container);
    }

    /** Set the local director for execution of this CompositeActor.
     *  Calling this method with a non-null argument makes this entity opaque.
     *  Calling it with a null argument makes it transparent.
     *  The container of the specified director is set to this composite
     *  actor, and if there was previously a local director, its container
     *  is set to null. This method is write-synchronized on the workspace.
     *
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException If the director is not in
     *  the same workspace as this actor. It may also be thrown in derived
     *  classes if the director is not compatible.
     */
    public void setDirector(Director director) throws IllegalActionException {
        if (director != null && _workspace != director.workspace()) {
            throw new IllegalActionException(this, director,
                    "Cannot set director because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            // If there was a previous director, we need to reset it.
            if (_director != null) _director._makeDirectorOf(null);
            if (director != null) {
                director._makeDirectorOf(this);
            }
            _director = director;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the Manager for execution of this CompositeActor.
     *  This can only be done for a composite actor that has no container.
     *  For others, the Manager is inherited from the container.
     *  This method is write-synchronized on the workspace.
     *
     *  @param manager The Manager
     *  @exception IllegalActionException If this actor already has a
     *  container, or the manager is not in the same workspace as this
     *  actor.
     *  @see #getManager()
     */
    public void setManager(Manager manager)
            throws IllegalActionException {
        if (manager != null && _workspace != manager.workspace()) {
            throw new IllegalActionException(this, manager,
                    "Cannot set manager because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            if (getContainer() != null && manager != null) {
                throw new IllegalActionException(this, manager,
                        "Cannot set the Manager of an actor "
                        + "with a container.");
            }
            // If there was a previous manager, we need to reset it.
            if (_manager != null) _manager._makeManagerOf(null);
            if (manager != null) {
                manager._makeManagerOf(this);
            }
            _manager = manager;
            return;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Request that execution of the current iteration stop.
     *  If this actor is opaque, then invoke the stopFire()
     *  method of the local director. Otherwise, do nothing.
     *  This method is read-synchronized on the workspace.
     */
    public void stopFire() {
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
		return;
            }
            getDirector().stopFire();
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this is an opaque CompositeActor, then look to our director
     *  for help.   If we are transparent, then we really shouldn't have been
     *  called, so just ignore.
     */
    public void terminate() {
        if(!isOpaque()) return;
        getDirector().terminate();
    }

    /** If this actor is opaque, then invoke the wrapup() method of the local
     *  director. This method is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's wrapup() method throws it, or if this
     *   actor is not opaque.
     */
    public void wrapup() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Missing director.");
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().wrapup();
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include the
     *  director and executive director.
     */
    public static final int DIRECTOR = 512;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an actor to this container with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  implements the Actor interface, to invalidate the schedule
     *  and type resolution, and to request initialization with the director.
     *  This method does not alter the actor in any way.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity Actor to contain.
     *  @exception IllegalActionException If the actor has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument does not implement the Actor interface.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the actor contents list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof Actor)) {
            throw new IllegalActionException(this, entity,
                    "CompositeActor can only contain entities that " +
                    " implement the Actor interface.");
        }
        super._addEntity(entity);
        Director director = getDirector();
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
            director.requestInitialization((Actor)entity);
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
     *  @param port The port to add to this actor.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this actor, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "CompositeActor can only contain instances of IOPort.");
        }
        super._addPort(port);
    }

    /** Add a relation to this container. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set the container of the relation to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Relation to contain.
     *  @exception IllegalActionException If the relation has no name, or is
     *   not an instance of IORelation.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "CompositeActor can only contain instances of IORelation.");
        }
        super._addRelation(relation);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class and this class.  Lines are indented
     *  according to to the level argument using the protected method
     *  _getIndentPrefix(). Zero, one or two brackets can be specified
     *  to surround the returned description.  If one is specified it is
     *  the leading bracket. This is used by derived classes that will
     *  append to the description. Those derived classes are responsible
     *  for the closing bracket. An argument other than 0, 1, or 2 is
     *  taken to be equivalent to 0.
     *  This method is <i>not</i> read-synchronized on the workspace,
     *  so the caller should be.
     *
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & DIRECTOR) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "director {\n";
                Director dir = getDirector();
                if (dir != null) {
                    result += dir._description(detail, indent+1, 2);
                    result += "\n";
                }
                result += _getIndentPrefix(indent) + "} executivedirector {\n";
                dir = getExecutiveDirector();
                if (dir != null) {
                    result += dir._description(detail, indent+1, 2);
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class is the director, attributes, ports, contained relations,
     *  and contained entities, plus all links.  The links are written
     *  in an order that respects the ordering in ports, but not necessarily
     *  the ordering in relations.  This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        if (_director != null) {
            _director.exportMoML(output, depth);
        }
        super._exportMoMLContents(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            IOPort oneport = (IOPort)ports.next();
            oneport.createReceivers();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Director _director;
    private Manager _manager;

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient List _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient List _cachedOutputPorts;
}
