/* An aggregation of actors.

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

package pt.actors;

import pt.kernel.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CompositeActor
/** 
A CompositeActor is an aggregation of actors.  It may have a <i>director</i>,
which is an object of class Director that is responsible for executing
the contained actors.  It may also have an <i>executive Director</i>,
which is also an object of class Director.  The executive director
is responsible for executing this composite.  It must have one or the
other.  The getDirector() method will return the director, if it exists,
and otherwise return the executive director.

If there is no director, then the executive director plays the role
of director.  I.e., for the purposes of execution, the hierarchy is
flattened.  The executive director executes the composite by executing
the contained actors. If there is a director, then the executive director
executes this composite by issuing commands to the director.

If there is a director, then the composite behaves
as an atomic entity (its isAtomic() method returns <i>true</i>, and
its ports are opaque).

A top-level CompositeActor will typically have both a director and
an executive director.  The executive director might be a control panel
in the gui package, for example.  The director would be domain-specific,
and might contain for example a scheduler that sequences the invocations
of the contained actors.

A CompositeActor that is lower in the hierarchy will typically have
an executive director, but no director.  The executive director is
the director or executive director (in that order of preference) of the
container.  In this case, the CompositeActor does not itself execute,
but instead its contained actors are managed by a director higher
in the hierarchy.  Such a composite actor was called a <i>galaxy</i>
in previous versions of Ptolemy.  Note that unlike previous versions
of Ptolemy, any galaxy can be assigned a director, and thus can be
executed.

A <i>wormhole</i> is a CompositeActor that is not at the top-level and
has both a director and and executive director.  The executive director
will invoke methods of the director, via the methods of the CompositeActor.
The director will typically transfer data tokens (and maybe translate them)
from the opaque ports of the composite to the connected ports of the
contained actors, and then will invoke those actors.  The model of
computation implemented by the director need not be the same as the
model of computation implemented by the executive director.

The ports of this composite are constrained to be IOPorts, the
relations to IORelations, and the actors instances of Actor.

@author Mudit Goel, Edward A. Lee
@version $Id$
@see pt.actors.Actor
@see pt.actors.IOPort
*/
public class CompositeActor extends CompositeEntity implements Executable {

    /** Construct a top-level CompositeActor with the specified director
     *  and executive director.  At least one of these must be non-null.
     *  The object is registered in the default workspace with
     *  an empty string as its name.
     *  Increment the version number of the workspace.
     *  @param dir The director.
     *  @param execdir The executive director.
     *  @exception IllegalActionException If both arguments are null.
     */
    public CompositeActor(Director dir, Director execdir)
            throws IllegalActionException {
	super();
        if (dir == null && execdir == null) {
            throw new IllegalActionException(
                    "CompositeActor requires a director or " +
                    "executive director.");
        }
	_director = dir;
        _execdirector = execdir;
    }

    /** Create a CompositeActor with a container, a name, and the specified
     *  director (which may be null). The container must not be null, or a
     *  NullPointerException exception will be thrown (this is a runtime
     *  exception).  This entity inherits both the workspace and its
     *  executive director from the container.  If the director is non-null,
     *  then the resulting composite is called a <i>wormhole</i>.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The parent actor.
     *  @param name The name of the actor.
     *  @param director The director.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public CompositeActor(CompositeActor container, String name,
            Director director)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	_director = director;
        _execdirector = container.getDirector();
    }

    /** Construct a top-level CompositeActor in the specified
     *  workspace with the specified name, director, and executive director.
     *  At least one of these must be non-null. If the workspace
     *  argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the composite.
     *  @param dir The director.
     *  @param execdir The executive director.
     *  @exception IllegalActionException If both dir and execdir are null.
     */
    public CompositeActor(Workspace workspace, String name,
            Director dir, Director execdir)
            throws IllegalActionException {
	super(workspace);
        if (dir == null && execdir == null) {
            workspace.remove(this);
            throw new IllegalActionException(
                    "CompositeActor requires a director or " +
                    "executive director.");
        }
        setName(name);
	_director = dir;
        _execdirector = execdir;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** If there is a director, invoke its fire() method.
     *  Otherwise, invoke the fire() method of the executive director.
     *  This method is synchronized on the workspace.
     *  @exception IllegalActionException If the director throws it.
     */
    public void fire() throws IllegalActionException {
        synchronized(workspace()) {
            getDirector().fire();
        }
    }

    /** Return the director responsible for execution of the contained
     *  actors.  This will be either the local director (if it exists) or the
     *  executive director.  Note that this is never null.
     *  @return The director responsible for invocation of inside actors.
     */
    public Director getDirector() {
	return _director;
    }

    /** Return the director responsible for the execution of this composite.
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return _execdirector;            
    }

    /** If there is a director, invoke its initialize() method.
     *  This method is synchronized on the workspace.
     *  Otherwise, invoke the initialize() method of the executive director.
     */
    public void initialize() {
        synchronized(workspace()) {
            getDirector().initialize();
        }
    }

    /** Return an enumeration of the input ports.
     *  This method is synchronized on the workspace.
     *  @returns An enumeration of IOPort objects.
     */ 
    public Enumeration inputPorts() {
        synchronized(workspace()) {
            if(_inputPortsVersion != workspace().getVersion()) {
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
        }
    }

    /** Return true if this contains a director.  Otherwise, return false.
     *  This method is synchronized on the workspace.
     */	
    public boolean isAtomic() {
        synchronized(workspace()) {
            return _director != null;
        }
    }

    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this entity.
     *  This method is synchronized on the workspace, and increments
     *  its version number.
     *  @param name The name of the newly created port.
     *  @return The new port.
     *  @exception IllegalActionException if the argument is null.
     *  @exception NameDuplicationException if the entity already has a port 
     *   with the specified name.
     */	
    public Port newPort(String name) 
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            IOPort port = new IOPort(this, name);
            workspace().incrVersion();
            return port;
        }
    }

    /** Create a new IORelation with the specified name, add it to the
     *  relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of IORelation.
     *  This method is synchronized on the workspace, and increments
     *  its version number.
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already on the container's contents list.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            IORelation rel = new IORelation(this, name);
            workspace().incrVersion();
            return rel;
        }
    }

    /** get an enumeration of the output ports
     *  This method is synchronized on the workspace.
     *  @returns An enumeration of IOPort objects.
     */
    public Enumeration outputPorts() {
        synchronized(workspace()) {
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
        }
    }

    /** If there is a director, invoke its postfire() method, and then
     *  transfer any data to the output ports of this composite from the
     *  ports connected on the inside.
     *  Otherwise, invoke the postfire() method of the executive director.
     *  This method is synchronized on the workspace.
     */
    public void postfire() {
        synchronized(workspace()) {
            getDirector().postfire();
            // FIXME: Transfer output data.
        }
    }

    /** If there is a director, transfer any data from the ports of the
     *  composite to the ports connected to them on the inside, and
     *  then invoke the prefire() method of the director.
     *  Otherwise, invoke the prefire() method of the executive director.
     *  This method is synchronized on the workspace.
     *  @return true if the star is ready for firing, false otherwise.
     */
    public boolean prefire() {
        synchronized(workspace()) {
            if (_director != null) {
                // FIXME: transfer input data.
                return getDirector().prefire();
            } else {
                return _execdirector.prefire();
            }
        }
    }
  
    /** Set the director for execution of this CompositeActor.
     *  This method increments the workspace version.
     *  This method is synchronized on the workspace.
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException If the argument is null and
     *   there is no executive director.
     */
    public void setDirector(Director director) throws IllegalActionException {
        synchronized(workspace()) {
            if (director == null && _execdirector == null) {
                throw new IllegalActionException(
                        "CompositeActor cannot have both a null director " +
                        "and executive director.");
            }
            _director = director;
            workspace().incrVersion();
            return;
        }
    }

    /** Set the executive director for execution of this CompositeActor.
     *  This method increments the workspace version.
     *  This method is synchronized on the workspace.
     *  @param execdir The executive director.
     *  @exception IllegalActionException If the argument is null and
     *   there is no director.
     */
    public void setExecutiveDirector(Director execdir)
            throws IllegalActionException {
        synchronized(workspace()) {
            if (execdir == null && _director == null) {
                throw new IllegalActionException(
                        "CompositeActor cannot have both a null director " +
                        "and executive director.");
            }
            _execdirector = execdir;
            workspace().incrVersion();
            return;
        }
    }

    /** If there is a director, invoke its wrapup() method.
     *  Otherwise, invoke the wrapup() method of the executive director.
     *  This method is synchronized on the workspace.
     */
    public void wrapup() {
        synchronized(workspace()) {
            getDirector().wrapup();
        }
    }
        
    ///////////////////////////////////////////////////////////////////////
    ////                      protected methods                        ////

    /** Add an actor to this container with minimal error checking.
     *  This overrides the base-class method to register the actor with
     *  the director. This method does not alter the entity in any way.
     *  This method is sychronized on the workspace.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException Argument has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *  on the entity contents list.
     */	
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            super._addEntity(entity);
            // getDirector().registerNewActor((Actor)entity);
            // FIXME -- is this right?  NO -- definitely wrong!
            // The entity might be a CompositeActor, which is not an actor!
        }
    }

    /** Remove the specified entity with minimal error checking.
     *  This overrides the base class method to unregister the actor
     *  with the director, if it is registered.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This method does not alter the entity in any way.
     *  This method is sychronized on the workspace.
     *  @param entity Entity to be removed.
     */	
    protected void _removeEntity(ComponentEntity entity) {
        synchronized(workspace()) {
            super._removeEntity(entity);
            // FIXME - getDirector().unregisterActor(entity);
        }
    }
    /////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Director _director, _execdirector;

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;
}
