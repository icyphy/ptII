/* An executable entity.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// AtomicActor
/**
An AtomicActor is an executable entity that cannot itself contain
other actors. The Ports of AtomicActors are constrained to be IOPorts.
Derived classes may further constrain the ports by overriding the public
method newPort() to create a port of the appropriate subclass, and the
protected method _addPort() to throw an exception if its argument is a
port that is not of the appropriate subclass. In this base class, the
actor does nothing in the action methods (prefire, fire, ...).

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.actor.CompositeActor
@see ptolemy.actor.IOPort
*/
public class AtomicActor extends ComponentEntity implements Actor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     */
    public AtomicActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list this actor.
     */
    public AtomicActor(Workspace workspace) {
        super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public AtomicActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        AtomicActor newObject = (AtomicActor)super.clone(workspace);
        // Reset to force reinitialization of cache.
        newObject._inputPortsVersion = -1;
        newObject._outputPortsVersion = -1;
        return newObject;
    }

    /** Create new receivers if the port is
     *  an input port and there is a director.
     *  @param port The port that has connection changes.
     */
    public void connectionsChanged(Port port) {
        if (_debugging) {
            _debug("Connections changed on port: " + port.getName());
        }
        if (port instanceof IOPort) {
            IOPort castPort = (IOPort)port;
            if (castPort.isInput() && getDirector() != null) {
                try {
                    castPort.createReceivers();
                } catch(IllegalActionException ex) {
                    // Should never happen.
                    throw new InternalErrorException(
                            "cannot create receivers.");
                }
            }
        }
    }

    /** Do nothing.  Derived classes override this method to define their
     *  their primary run-time action.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
    }

    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        Nameable container = getContainer();
        if (container instanceof Actor) {
            return ((Actor)container).getDirector();
        }
        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the IODependency object associated with this 
     *  atomic actor.
     *  @return the IODependency object.
     *  @see IODependency
     */
    public IODependency getIODependencies() {
        // If the _ioDependency object is not constructed, 
        // construct an IODependencyOfAtomicActor object.
        if (_ioDependency == null) {
            _ioDependency = new IODependencyOfAtomicActor(this);
        }
        // Note, we don't guarantee the validity of this 
        // _ioDependency in this method. Any further access of
        // the details of the _ioDependency will check the 
        // validity.
        return _ioDependency;
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    public Manager getManager() {
        try {
            _workspace.getReadAccess();
            Nameable container = getContainer();
            if (container instanceof Actor) {
                return ((Actor)container).getManager();
            }
            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Initialize this actor.  Derived classes override this method
     *  to perform actions that should occur once at the beginning of
     *  an execution, but after type resolution.  Derived classes can
     *  produce output data and schedule events.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }
    }

    /** List all the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of input IOPort objects.
     */
    public List inputPortList() {
        if (_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                // Update the cache.
                List inputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while (ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if ( p.isInput()) {
                        inputPorts.add(p);
                    }
                }
                // Create an arrayList, since the cache will not be
                // modified.  This reduces memory usage.
                _cachedInputPorts = new ArrayList(inputPorts);
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedInputPorts;
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED. If stop() is called while
     *  this is executing, then cease executing and return STOP_ITERATING.
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

    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *  Normally this method is not called directly by actor code.
     *  Instead, a change request should be queued with the director.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If this actor already has a port
     *   with the specified name.
     *  @see ptolemy.kernel.util.Workspace#getWriteAccess()
     */
    public Port newPort(String name) throws NameDuplicationException {
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

    /** Return a new receiver of a type compatible with the director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.
     *
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return director.newReceiver();
    }

    /** List the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of output IOPort objects.
     */
    public List outputPortList() {
        if (_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                List outputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while (ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if ( p.isOutput()) {
                        outputPorts.add(p);
                    }
                }
                // Create an arrayList, since the cache will not be
                // modified.  This reduces memory usage.
                _cachedOutputPorts = new ArrayList(outputPorts);
                _outputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedOutputPorts;
    }

    /** Return true, unless stop() has been called, in which case,
     *  return false.  Derived classes override this method to define
     *  operations to be performed at the end of every iteration of
     *  its execution, after one invocation of the prefire() method
     *  and any number of invocations of the fire() method.
     *  This method typically wraps up an iteration, which may
     *  involve updating local state.  In derived classes,
     *  this method returns false to indicate that this actor should not
     *  be fired again.
     *
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called postfire()");
        }
        return !_stopRequested;
    }

    /** Return true. Derived classes override this method to define
     *  operations to be performed at the beginning of every iteration
     *  of its execution, prior the invocation of the fire() method.
     *  Derived classes may also use it to check preconditions for an
     *  iteration, if there are any.
     *
     *  @return True if this actor is ready for firing, false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire(), which returns true");
        }
        return true;
    }

    /** Create receivers. Derived classes
     *  can override this method to perform additional initialization
     *  functions, but they should call this base class methods or
     *  create the receivers themselves.
     *  This method gets executed exactly once prior to any other
     *  action methods.  It cannot produce output data since type
     *  resolution is typically not yet done. It also gets invoked
     *  prior to any static scheduling that might occur in the domain,
     *  so it can change scheduling information.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void preinitialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called preinitialize()");
        }
        _stopRequested = false;
        // NOTE:  Receivers are also getting created
        // in connectionChanged().  Perhaps this is here to ensure
        // that the receivers are reset?
        _createReceivers();

    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  In this base class, this method does nothing. Subclasses should
     *  implement the details. 
     *  <p>
     *  To declare a pair, input and output, independent, use the <i>
     *  removeDependency(IOPort IOPort)</i> method. 
     *  
     *  @see ptolemy.domains.de.lib.TimedDelay
     *  @see #removeDependency(IOPort IOPort)
     */
    public void removeDependencies() {
    }

    /** Explicitly declare a pair, input and output, are independent.
     *  @param input the input port
     *  @param output the output port
     */
    public void removeDependency(IOPort input, IOPort output) {
        _ioDependency.removeDependency(input, output);
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

    /** Request that execution of the current iteration stop as soon
     *  as possible.  In this base class, we set a flag indicating that
     *  this request has been made (the protected variable _stopRequested).
     *  Most atomic actors have bounded fire() methods,
     *  so they can simply ignore this.  Atomic actors with unbounded
     *  fire() methods should react by saving their state
     *  and returning from the fire() method at the next convenient
     *  point.
     */
    public void stop() {
        _stopRequested = true;
        if (_debugging) {
            _debug("Called stop()");
        }
    }

    /** Request that execution of the current iteration complete.
     *  Most atomic actors have bounded fire() methods, so they
     *  can simply ignore this.  Atomic actors with unbounded fire()
     *  methods should override this method to save their state and
     *  return from the fire() method at the next convenient point.
     *  In this base class, do nothing.
     */
    public void stopFire() {
        if (_debugging) {
            _debug("Called stopFire()");
        }
    }

    /** Terminate execution immediately.  In this base class, call stop().
     *  Derived classes may wish to do something more aggressive, such
     *  as terminating any threads they have started.
     */
    public void terminate() {
        if (_debugging) {
            _debug("Called terminate()");
        }
        stop();
    }

    /** Do nothing.  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup()");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to throw an exception if the added port
     *  is not an instance of IOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of IOPort. This method is <i>not</i> synchronized on
     *  the workspace, so the caller should be.
     *
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this entity, or the port has no name.
     *  @exception NameDuplicationException If the port name coincides with a
     *   name already in the entity.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this entity.");
        }
        super._addPort(port);
    }

    /*  Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    protected void _createReceivers() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();
            inputPort.createReceivers();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient List _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient List _cachedOutputPorts;
    
    // Cached ioDependency object.
    private IODependencyOfAtomicActor _ioDependency;
}
