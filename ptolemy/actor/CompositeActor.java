/* An aggregation of actors.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
setDirector throws NameDuplicationException
fire: call transferOutputs on local, not executive director.
preinitialize: validate attributes of this composite and
    the attributes of its ports.
setDirector invalidatesSchedule of executiveDirector.
moved invalidation code from _addEntity to _finishedAddEntity
initialize now clears receivers.. This helps SampleDelay inside a modal models with reset transition work better.
*/

package ptolemy.actor;

//import ptolemy.kernel.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import ptolemy.actor.parameters.ParameterPort;

//////////////////////////////////////////////////////////////////////////
//// CompositeActor
/**
A CompositeActor is an aggregation of actors.  It may have a
<i>local director</i>, which is an attribute of class Director that
is responsible for executing the contained actors.
At the top level of a hierarchy, a composite actor (the toplevel
CompositeActor of the topology) will normally exist with a local Director,
and no container.  A composite actor at a lower level
of the hierarchy may also have a local director.  A composite actor
with a local director is <i>opaque</i>, and serves the role of the
<i>wormhole</i> from Ptolemy Classic. Its ports are opaque, but it can
contain actors and relations.  The toplevel composite actor is also
associated with a Manager object that is responsible for managing
any execution within the topology at a high level.
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
A composite actor must have an executive director in order to communicate with
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

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.actor.IOPort
@see ptolemy.actor.IORelation
@see ptolemy.kernel.ComponentEntity
@see ptolemy.actor.Director
@see ptolemy.actor.Manager
*/
public class CompositeActor extends CompositeEntity 
    implements Actor, HasFunctionDependencies {

    /** Construct a CompositeActor in the default workspace with no container
     *  and an empty string as its name. Add the actor to the workspace
     *  directory.
     *  You should set a director before attempting to execute it.
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
     *  You should set a director before attempting to execute it.
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
     *  You should set a director before attempting to execute it.
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
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the actor contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new CompositeActor.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        CompositeActor newObject = (CompositeActor)super.clone(workspace);
        newObject._inputPortsVersion = -1;
        newObject._outputPortsVersion = -1;
        return newObject;
    }

    /** Invalidate the schedule and type resolution and create
     *  new receivers if the specified port is an opaque
     *  output port.  Also, notify the containers of any ports
     *  deeply connected on the inside by calling their connectionsChanged()
     *  methods, since their width may have changed.
     *  @param port The port that has connection changes.
     */
    public void connectionsChanged(Port port) {
        if (_debugging) {
            _debug("Connections changed on port: " + port.getName());
        }
        super.connectionsChanged(port);
        if (port instanceof ComponentPort) {
            // NOTE: deepInsidePortList() is not the right thing here
            // since it will return the same port if it is opaque.
            Iterator insidePorts
                = ((ComponentPort)port).insidePortList().iterator();
            try {
                _inConnectionsChanged = true;
                while (insidePorts.hasNext()) {
                    ComponentPort insidePort
                        = (ComponentPort)insidePorts.next();
                    Entity portContainer = (Entity)insidePort.getContainer();
                    // Avoid an infinite loop where notifications are traded.
                    if (!(portContainer instanceof CompositeActor)
                            || !((CompositeActor)portContainer)
                            ._inConnectionsChanged) {
                        portContainer.connectionsChanged(insidePort);
                    }
                }
            } finally {
                _inConnectionsChanged = false;
            }
        }
        if (port instanceof IOPort) {
            IOPort castPort = (IOPort) port;
            if (castPort.isOpaque()) {
                if (castPort.isOutput() && getDirector() != null) {
                    // Note that even if castPort is opaque, we still have to
                    // check for director above.
                    try {
                        castPort.createReceivers();
                    } catch(IllegalActionException ex) {
                        // Should never happen.
                        throw new InternalErrorException(
                                this, ex, "Cannot create receivers");
                    }
                }
                if (castPort.isInput() && getExecutiveDirector() != null) {
                    try {
                        castPort.createReceivers();
                    } catch(IllegalActionException ex) {
                        // Should never happen.
                        throw new InternalErrorException(
                                this, ex, "Cannot create receivers");
                    }
                }
                // Invalidate the local director schedule and types
                if (getDirector() != null) {
                    getDirector().invalidateSchedule();
                    getDirector().invalidateResolvedTypes();
                }
            }
        }
    }

    /** If this actor is opaque, transfer any data from the input ports
     *  of this composite to the ports connected on the inside, and then
     *  invoke the fire() method of its local director.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  If the actor is not opaque, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).  After the fire() method of the director returns,
     *  send any output data created by calling the local director's
     *  transferOutputs method.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }
          
            // This HAS to be split because in some domains (e.g. SDF)
            // the behavior of the schedule might depend on rate variables
            // set from ParameterPorts.

            // Use the local director to first read from port parameters.
            for(Iterator inputPorts = inputPortList().iterator();
                inputPorts.hasNext() && !_stopRequested;) {
                IOPort p = (IOPort)inputPorts.next();
                if(p instanceof ParameterPort) {
                    ((ParameterPort)p).getParameter().update();
                    //Used to be: _director.transferInputs(p);
                }
            }
            // Use the local director to transfer inputs from
            // everything that is not a port parameter.
            for(Iterator inputPorts = inputPortList().iterator();
                inputPorts.hasNext() && !_stopRequested;) {
                IOPort p = (IOPort)inputPorts.next();
                if(!(p instanceof ParameterPort)) {
                    _director.transferInputs(p);
                }
            }
            if (_stopRequested) return;
            _director.fire();
            if (_stopRequested) return;
            // Use the local director to transfer outputs.
            Iterator outports = outputPortList().iterator();
            while (outports.hasNext() && !_stopRequested) {
                IOPort p = (IOPort)outports.next();
                _director.transferOutputs(p);
            }
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
            Nameable container = getContainer();
            if (container instanceof Actor) {
                return ((Actor)container).getDirector();
            }
            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the FunctionDependency object associated with this 
     *  composite actor.
     *  @return the FunctionDependency object.
     *  @see FunctionDependency
     */
    public FunctionDependency getFunctionDependencies() {
        // If the _functionDependency object is not constructed, 
        // construct a FunctionDependencyOfAtomicActor object.
        if (_functionDependency == null) {
            _functionDependency = new FunctionDependencyOfCompositeActor(this);
        }
        // Note, we don't guarantee the validity of this 
        // _functionDependency in this method. Any further access of
        // the _functionDependency will check the validity.
        return _functionDependency;
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
            Nameable container = getContainer();
            if (container instanceof Actor) {
                return ((Actor)container).getManager();
            }
            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Initialize this actor.  If this actor is opaque, invoke the
     *  initialize() method of its local director. Otherwise, throw an
     *  exception.  This method is read-synchronized on the workspace,
     *  so the initialize() method of the director need not be
     *  (assuming it is only called from here).
     *
     *  @exception IllegalActionException If there is no director, or
     *  if the director's initialize() method throws it, or if the
     *  actor is not opaque.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot initialize a non-opaque actor.");
            }

            // Clear all of the contained actor's input ports.
            for(Iterator actors = entityList(Actor.class).iterator();
                actors.hasNext();) {
                Entity actor = (Entity)actors.next();
                Iterator ports = actor.portList().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    if(port.isInput()) {
                        // Clear all receivers.
                        Receiver[][] receivers = port.getReceivers();
                        for(int i = 0; i < receivers.length; i++) {
                            Receiver[] receivers2 = receivers[i];
                            for(int j = 0; j < receivers2.length; j++) {
                                receivers2[j].clear();
                            }
                        } 
                    }
                }
            }

            // Clear all of the output ports.
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort)ports.next();
                if(port.isOutput()) {
                    // Clear all insideReceivers.
                    Receiver[][] receivers = port.getInsideReceivers();
                    for(int i = 0; i < receivers.length; i++) {
                        Receiver[] receivers2 = receivers[i];
                        for(int j = 0; j < receivers2.length; j++) {
                            receivers2[j].clear();
                        }
                    } 
                }
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
            if (_inputPortsVersion != _workspace.getVersion()) {
                // Update the cache.
                List inputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while (ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if ( p.isInput()) {
                        inputPorts.add(p);
                    }
                }
                _cachedInputPorts = inputPorts;
                _inputPortsVersion = _workspace.getVersion();
            }

            return _cachedInputPorts;

        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if this actor contains a local director.
     *  Otherwise, return false.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     */
    public boolean isOpaque() {
        return _director != null;
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.  If stop() is called during
     *  this iteration, then cease iterating and return STOP_ITERATING.
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
        if (_debugging) {
            _debug("Called iterate(" + count + ")");
        }
        int n = 0;
        while (n++ < count && !_stopRequested) {
            if (prefire()) {
                fire();
                if (!postfire()) return Executable.STOP_ITERATING;
            } else {
                return Executable.NOT_READY;
            }
        }
        if (_stopRequested) {
            return Executable.STOP_ITERATING;
        } else {
            return Executable.COMPLETED;
        }
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
            throw new InternalErrorException(this, ex, null);
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
        Director director = getExecutiveDirector();
        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without an executive director.");
        }
        return director.newReceiver();
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
            IORelation relation = new IORelation(this, name);
            return relation;
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
            if (_outputPortsVersion != _workspace.getVersion()) {
                _cachedOutputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while (ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if ( p.isOutput()) {
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

    /** Create Receivers and invoke the
     *  preinitialize() method of its local director. If this actor is
     *  not opaque, throw an exception.  This method also resets
     *  the protected variable _stopRequested
     *  to false, so if a derived class overrides this method, then it
     *  should also do that.  This method is
     *  read-synchronized on the workspace, so the preinitialize()
     *  method of the director need not be, assuming it is only called
     *  from here.
     * 
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    public void preinitialize() throws IllegalActionException {
        _stopRequested = false;
        // because function dependency is not persistent,
        // it gets reset each time the preinitialize method is called.
        _functionDependency = null;
        if (_debugging) {
            _debug("Called preinitialize()");
        }
        try {
            _workspace.getReadAccess();
            _createReceivers();
            
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot preinitialize a non-opaque actor.");
            }
            if (_director == null) {
                throw new InternalErrorException(
                        "Actor says it is opaque, but it has no director: "
                        + getFullName());
            }

            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().preinitialize();

        } finally {
            _workspace.doneReading();
        }
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
        if (_debugging) {
            _debug("Called postfire()");
        }
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot postfire a non-opaque actor.");
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            boolean result = getDirector().postfire();
            if (_debugging) {
                _debug("Postfire returns (from director) " + result);
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this actor is opaque, invoke the prefire() method of the local
     *  director. This method returns true if the actor is ready to fire
     *  (determined by the prefire() method of the director).
     *  It is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's prefire() method throws it, or if this actor
     *   is not opaque.
     */
    public boolean prefire()
            throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire()");
        }
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot invoke prefire on a non-opaque actor.");
            }
            boolean result = getDirector().prefire();
            if (_debugging) {
                _debug("Prefire returns (from director) " + result);
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Queue a change request.  If there is a manager, then first call
     *  stopFire() before deferring to the base class.
     *  @param change The requested change.
     */
    public void requestChange(ChangeRequest change) {
        Manager manager = getManager();
        if (manager != null) {
            stopFire();
        }
        super.requestChange(change);
    }

    /** Override the base class to invalidate the schedule and
     *  resolved types of the director.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        // Invalidate the schedule and type resolution of the old director.
        Director oldDirector = getDirector();
        if (oldDirector != null) {
            oldDirector.invalidateSchedule();
            oldDirector.invalidateResolvedTypes();
        }

        super.setContainer(container);

        Director director = getDirector();
        // Invalidate the schedule and type resolution of the new director.
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
    }

    /** Set the local director for execution of this CompositeActor.
     *  Calling this method with a non-null argument makes this entity opaque.
     *  Calling it with a null argument makes it transparent.
     *  The container of the specified director is set to this composite
     *  actor, and if there was previously a local director, its container
     *  is set to null. This method is write-synchronized on the workspace.
     *  NOTE: Calling this method is almost equivalent to calling setContainer()
     *  on the director with this composite as an argument. The difference
     *  is that if you call this method with a null argument, it effectively
     *  removes the director from its role as director, but without
     *  removing it from its container.
     *
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException If the director is not in
     *  the same workspace as this actor. It may also be thrown in derived
     *  classes if the director is not compatible.
     *  @exception NameDuplicationException If an attribute already exists
     *  in this container with the same name as the given director.
     */
    public void setDirector(Director director)
            throws IllegalActionException, NameDuplicationException {
        if (director != null) {
            director.setContainer(this);
        } else {
            _setDirector(null);
        }
    }

    /** Set the Manager for execution of this CompositeActor.
     *  This can only be done for a composite actor that has no container.
     *  For others, the Manager is inherited from the container.
     *  This method is write-synchronized on the workspace.
     *
     *  @param manager The Manager
     *  @exception IllegalActionException If this actor has a
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
            if (_manager != null) {
                // Remove this from the list of debug listeners of the old
                // manager.
                _manager.removeDebugListener(this);
                // Notify the old manager that it is no longer the manager
                // of anything.
                _manager._makeManagerOf(null);
            }
            if (manager != null) {
                // Add this to the list of debug listeners of the new manager.
                // This composite actor will relay debug messages from
                // the manager.
                manager.addDebugListener(this);
                manager._makeManagerOf(this);
            }
            _manager = manager;
            return;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Request that execution stop as soon as possible.
     *  This sets a flag indicating that this request has been made
     *  (the protected variable _stopRequested).
     *  If this actor is opaque, then invoke the
     *  stop() method of the local director.
     *  This method is read-synchronized on the workspace.
     */
    public void stop() {
        if (_debugging) {
            _debug("Called stop()");
        }
        try {
            _workspace.getReadAccess();
            _stopRequested = true;
            if (!isOpaque()) {
                return;
            }
            Director director = getDirector();
            if (director != null) {
                director.stop();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Request that execution of the current iteration complete.
     *  If this actor is opaque, then invoke the stopFire()
     *  method of the local director, if there is one.
     *  Otherwise, do nothing.
     *  This method is read-synchronized on the workspace.
     */
    public void stopFire() {
        if (_debugging) {
            _debug("Called stopFire()");
        }
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                return;
            }
            Director director = getDirector();
            if (director != null) {
                director.stopFire();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this is an opaque CompositeActor, then look to our director
     *  for help.   If we are transparent, then we really shouldn't have been
     *  called, so just ignore.
     */
    public void terminate() {
        if (_debugging) {
            _debug("Called terminate()");
        }
        if (!isOpaque()) return;
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
        if (_debugging) {
            _debug("Called wrapup()");
        }
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Missing director.");
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            // However, there may not be a director (e.g. DifferentialSystem
            // actor in CT).
            Director director = getDirector();
            if (director != null) {
                director.wrapup();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an actor to this container with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  implements the Actor interface.
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

    /** Notify this actor that the given entity has been added inside it.
     *  This overrides the base-class method to invalidate the schedule
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
    protected void _finishedAddEntity(ComponentEntity entity) {
        Director director = getDirector();
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
            director.requestInitialization((Actor)entity);
        }
    }

    /** Set the local director for execution of this CompositeActor.
     *  This should not be called be directly.  Instead, call setContainer()
     *  on the director.  This method removes any previous director
     *  from this container, and caches a local reference to the director
     *  so that this composite does not need to search its attributes each
     *  time the director is accessed.
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException If removing the old director
     *   causes this to be thrown. Should not be thrown.
     *  @exception NameDuplicationException If removing the old director
     *   causes this to be thrown. Should not be thrown.
     */
    protected void _setDirector(Director director)
            throws IllegalActionException, NameDuplicationException {

        Director oldDirector = getDirector();
        if (oldDirector != null) {
            oldDirector.invalidateSchedule();
            oldDirector.invalidateResolvedTypes();
        }

        _director = director;

        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        } else {
            // When deleting, the executive director also needs to be
            // notified that its schedule must be recomputed.
            Director executiveDirector = getExecutiveDirector();
            if (executiveDirector != null) {
                executiveDirector.invalidateSchedule();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            IOPort onePort = (IOPort)ports.next();
            onePort.createReceivers();
        }
    }

    // The director for this composite actor.
    private Director _director;

    // Indicator that we are in the connectionsChanged method.
    private boolean _inConnectionsChanged = false;
    
    // The manager for this composite actor.
    private Manager _manager;

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient List _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient List _cachedOutputPorts;
    
    // Cached FunctionDependency object.
    private FunctionDependencyOfCompositeActor _functionDependency;
}
