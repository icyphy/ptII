/* A ComponentEntity is a vertex in a hierarchical graph.

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

package pt.kernel;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ComponentEntity
/** 
A ComponentEntity is a component in a CompositeEntity.
It might itself be composite, but in this base class it is assumed to
be atomic.  I.e., it contains no components.

@author John S. Davis II, Edward A. Lee
@version $Id$
*/
public class ComponentEntity extends Entity {

    /** Construct an entity in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public ComponentEntity() {
	super();
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public ComponentEntity(Workspace workspace) {
	super(workspace, "");
    }

    /** Construct an entity with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the entity.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */	
    public ComponentEntity(CompositeEntity container, String name) 
            throws NameDuplicationException {
        super(container.workspace(), name);
        try {
            container._addEntity(this);
        } catch (IllegalActionException ex) {
            // Ignore -- always has a name.
        }
        // "super" call above puts this on the workspace list.
        workspace().remove(this);
        _setContainer(container);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Append a port to the list of ports belonging to this entity.
     *  If the port has a container (other than this), remove it from
     *  the port list of that container.  Set the container of the port
     *  to point to this entity.  This overrides the base class to ensure
     *  that the port added is a ComponentPort.  If it is not, throw an
     *  exception.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param port
     *  @exception IllegalActionException Port is not of class ComponentPort.
     *  @exception NameDuplicationException Name collides with a name already
     *  on the port list.
     */	
    public void addPort(Port port) 
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof ComponentPort)) {
            throw new IllegalActionException(this, port,
                    "Component entities require ComponentPorts.");
        }
        synchronized(workspace()) {
            super.addPort(port);
        }
    }

    /** Get the container entity.
     *  This method is synchronized on the workspace.
     *  @return An instance of CompositeEntity.
     */
    public Nameable getContainer() {
        synchronized(workspace()) {
            return _container;
        }
    }

    /** An atomic entity cannot contain components.
     *  Instances of this base class are atomic, so return true.
     *  Derived classes that return false are expected to be instances of
     *  CompositeEntity.
     *  @return False if this is not a CompositeEntity.
     *  @see pt.kernel.CompositeEntity
     */	
    public boolean isAtomic() {
	return true;
    }

    /** Create a new port with the specified name.
     *  The container of the port is set to this entity.
     *  This overrides the base class to create a ComponentPort rather
     *  than a simple Port.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param name The new port name.
     *  @return The new port
     *  @exception IllegalActionException Argument is null.
     *  @exception NameDuplicationException Entity already has a port with
     *  that name.
     */	
    public Port newPort(String name) 
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            Port port = new ComponentPort(this, name);
            return port;
        }
    }

    /** Specify the container entity, adding the entity to the list 
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the list of objects in the workspace. If the argument is null, then
     *  unlink the ports of the entity from any relations, remove it from
     *  its container, and add it to the list of objects in the workspace.
     *  If the entity is already contained by the container, do nothing.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException Recursive containment structure, or
     *   this entity and container are not in the same workspace..
     *  @exception NameDuplicationException Name collides with a name already
     *   on the contents list of the container.
     */	
    public void setContainer(CompositeEntity container) 
            throws IllegalActionException, NameDuplicationException {
        if (container != null && workspace() != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
        synchronized(workspace()) {
            CompositeEntity prevcontainer = (CompositeEntity)getContainer();
            if (prevcontainer == container) return;

            // Do this first, because it may throw an exception.
            if (container != null) {
                container._addEntity(this);
                if (prevcontainer == null) {
                    workspace().remove(this);
                }
            }
            _setContainer(container);
            if (container == null) {
                // Ignore exceptions, which mean the object is already
                // on the workspace list.
                try {
                    workspace().add(this);
                } catch (IllegalActionException ex) {}
            }

            if (prevcontainer != null) {
                prevcontainer._removeEntity(this);
            }
            if (container == null) {
                Enumeration ports = getPorts();
                while (ports.hasMoreElements()) {
                    Port port = (Port)ports.nextElement();
                    port.unlinkAll();
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Set the container without any effort to maintain consistency
     *  (i.e. nothing is done to ensure that the container includes the
     *  entity in its list of entities, nor that the entity is removed from
     *  the entity list of the previous container).
     *  If the previous container is null and the
     *  new one non-null, remove the entity from the list of objects in the
     *  workspace.  If the new container is null, then add the entity to
     *  the list of objects in the workspace.
     *  This method is synchronized on the
     *  workspace, and increments its version number.
     *  It assumes the workspace of the container is the same as that
     *  of this entity, but this is not checked.  The caller should check.
     *  Use the public version to to ensure consistency.
     *  @param container
     */	
    protected void _setContainer(CompositeEntity container) {
        synchronized(workspace()) {
            _container = container;
            workspace().incrVersion();
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         private variables                       ////

    // The entity that contains this entity.
    private CompositeEntity _container;
}
