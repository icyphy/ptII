/* A ComponentEntity is a vertex in a clustered graph.

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

@ProposedRating Green (eal@eecs.berkeley.edu)

*/

package pt.kernel;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ComponentEntity
/** 
A ComponentEntity is a component in a CompositeEntity.
It might itself be composite, but in this base class it is assumed to
be atomic (meaning that it contains no components).
<p>
Derived classes may further constrain the container to be
a subclass of CompositeEntity.  To do this, they should override
setContainer() to throw an exception.
<p>
A ComponentEntity can contain instances of ComponentPort.  Derived
classes may further constrain to a subclass of ComponentPort.
To do this, they should override the public method newPort() to create
a port of the appropriate subclass, and the protected method _addPort()
to throw an exception if its argument is a port that is not of the
appropriate subclass.

@author John S. Davis II, Edward A. Lee
@version $Id$
*/
public class ComponentEntity extends Entity {

    /** Construct an entity in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     */
    public ComponentEntity() {
	super();
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
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
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */	
    public ComponentEntity(CompositeEntity container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name);
        container._addEntity(this);
        // "super" call above puts this on the workspace list. Remove it.
        workspace().remove(this);
        _container = container;
        workspace().incrVersion();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Clone the object and register the clone in the workspace.
     *  The result is an entity with the same ports as the original, but
     *  no connections, that is registered with the workspace.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur).
     */
    public Object clone() throws CloneNotSupportedException {
        // NOTE: It is not actually necessary to override the base class
        // method, but we do it anyway so that the exact behavior of this
        // method is documented with the class.
        return super.clone();
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
     *  This overrides the base class to create an instance of ComponentPort.
     *  Derived classes may override this to further constrain the ports.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param name The new port name.
     *  @return The new port
     *  @exception IllegalActionException If the argument is null.
     *  @exception NameDuplicationException If this entity already has a
     *   port with the specified name.
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
     *  Derived classes may override this method to constrain the container
     *  to subclasses of CompositeEntity. This method is synchronized on the
     *  workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace..
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
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
            _container = container;
            workspace().incrVersion();
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

    /** Add a port to this entity. This overrides the base class to
     *  throw an exception if the added port is not an instance of
     *  ComponentPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set
     *  the container of the port to point to this entity.
     *  It assumes that the port is in the same workspace as this
     *  entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of ComponentPort.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this entity, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a 
     *   name already in the entity.
     */	
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof ComponentPort)) {
            throw new IllegalActionException(this, port,
            "Incompatible port class for this entity.");
        }
        super._addPort(port);
    }

    /** Clear references that are not valid in a cloned object.  The clone()
     *  method makes a field-by-field copy, which results
     *  in invalid references to objects. 
     *  In this class, this method resets the private member _container.
     */
    protected void _clear() {
        super._clear();
        _container = null;
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         private variables                       ////

    // The entity that contains this entity.
    private CompositeEntity _container;
}
