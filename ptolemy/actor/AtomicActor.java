/* An executable entity.

 Copyright (c) 1997- The Regents of the University of California.
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
*/

package pt.actor;

import pt.kernel.*;
import pt.kernel.util.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// AtomicActor
/**
An AtomicActor is an executable entity. The Ports of AtomicActors are
constrained to be IOPorts.
The container of an AtomicActor is constrained to be
an instance of CompositeActor.  In this base class, the actor does nothing
in the action methods (prefire, fire, ...).

@author Mudit Goel, Edward A. Lee
@version $Id$
@see pt.actors.CompositeActor
@see pt.actors.IOPort
*/
public class AtomicActor extends ComponentEntity implements Actor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The containing CompositeActor.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public AtomicActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    // FIXME: Implement clone() and _description().

    /** Return the director responsible for the execution of this actor.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        CompositeActor container = (CompositeActor)getContainer();
        if (container != null) {
            return container.getDirector();
        }
        return null;
    }

    /** This is where the actors would normally define their actions
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
    }

    /** The actors would be initialized in this method before their execution
     *  begins
     */
    public void initialize() {
    }

    /** Return an enumeration of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration inputPorts() {
        try {
            workspace().getReadAccess();
            if(_inputPortsVersion != workspace().getVersion()) {
                // Update the cache.
                LinkedList inports = new LinkedList();
                Enumeration ports = getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isInput()) {
                        inports.insertLast(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = workspace().getVersion();
            }
            return _cachedInputPorts.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the newly created port.
     *  @return The new port.
     *  @exception IllegalActionException if the argument is null.
     *  @exception NameDuplicationException if the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            IOPort port = new IOPort(this, name);
            workspace().incrVersion();
            return port;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return a new receiver of a type compatible with the director.
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this,
            "Cannot create a receiver without a director.");
        }
        return dir.newReceiver();
    }

    /** Return an enumeration of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration outputPorts() {
        try {
            workspace().getReadAccess();
            if(_outputPortsVersion != workspace().getVersion()) {
                LinkedList outports = new LinkedList();
                Enumeration ports = getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isOutput()) {
                        outports.insertLast(p);
                    }
                }
                _cachedOutputPorts = outports;
                _outputPortsVersion = workspace().getVersion();
            }
            return _cachedOutputPorts.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Do nothing.  In derived classes, this defines operations to be
     *  performed at the end of every iteration of its execution.
     */
    public void postfire() {
    }

    /** Return true.  In derived classes, this defines operations to be
     *  performed at the beginning of every iteration of its execution.
     *  @return true if the actor is ready for firing, false otherwise.
     */
    public boolean prefire() {
        return true;
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of CompositeActor. If it is, call the base class
     *  setContainer() method.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the argument is not a CompositeActor.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor)) {
            throw new IllegalActionException(container, this,
                    "AtomicActor can only be contained by instances of " +
                    "CompositeActor.");
        }
        super.setContainer(container);
    }

    /** Do nothing.  In derived classes, this defines operations to be
     *  performed at the end of a complete execution of an application.
     */
    public void wrapup() {
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Override the base class to
     *  throw an exception if the added port is not an instance of
     *  IOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set
     *  the container of the port to point to this entity.
     *  It assumes that the port is in the same workspace as this
     *  entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of IOPort.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
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

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;
}




