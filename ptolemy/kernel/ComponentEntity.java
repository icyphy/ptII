/* A ComponentEntity is a vertex in a clustered graph.

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
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// ComponentEntity
/**
A ComponentEntity is a component in a CompositeEntity.
It might itself be composite, but in this base class it is assumed to
be atomic (meaning that it cannot contain components).
<p>
Derived classes may further constrain the container to be
a subclass of CompositeEntity.  To do this, they should override
the protected method _checkContainer() to throw an exception.
<p>
A ComponentEntity can contain instances of ComponentPort.  Derived
classes may further constrain to a subclass of ComponentPort.
To do this, they should override the public method newPort() to create
a port of the appropriate subclass, and the protected method _addPort()
to throw an exception if its argument is a port that is not of the
appropriate subclass.

@author John S. Davis II, Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
*/
public class ComponentEntity extends Entity {

    /** Construct an entity in the default workspace with an empty string
     *  The object is added to the workspace directory.
     *  as its name. Increment the version number of the workspace.
     */
    public ComponentEntity() {
        super();
        _addIcon();
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public ComponentEntity(Workspace workspace) {
        super(workspace);
        _addIcon();
    }

    /** Construct an entity with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ComponentEntity(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name);
        setContainer(container);
        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the entity is atomic.
     *  An atomic entity is one that cannot have components.
     *  Instances of this base class are always atomic.
     *  Derived classes that return false are assumed to be instances of
     *  CompositeEntity or a class derived from that.
     *  @return True if the entity is atomic.
     *  @see ptolemy.kernel.CompositeEntity
     */
    public boolean isAtomic() {
        return true;
    }

    /** Return true if the entity is opaque.
     *  An opaque entity is one that either is atomic or hides
     *  its components behind opaque ports.
     *  Instances of this base class are always opaque.
     *  Derived classes may be transparent, in which case they return false
     *  to this method and to isAtomic().
     *  @return True if the entity is opaque.
     *  @see ptolemy.kernel.CompositeEntity
     */
    public boolean isOpaque() {
        return true;
    }

    /** Create a new port with the specified name.
     *  The container of the port is set to this entity.
     *  This overrides the base class to create an instance of ComponentPort.
     *  Derived classes may override this to further constrain the ports.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param name The new port name.
     *  @return The new port
     *  @exception IllegalActionException If the argument is null.
     *  @exception NameDuplicationException If this entity already has a
     *   port with the specified name.
     */
    public Port newPort(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            Port port = new ComponentPort(this, name);
            return port;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.  If this entity is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context.  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     *  @see #isClassElement()
     */
    public void setContainer(Prototype container)
            throws IllegalActionException, NameDuplicationException {

        if (container != null && _workspace != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            _checkContainer(container);
            // NOTE: The following code is quite tricky.  It is very careful
            // to leave a consistent state even in the face of unexpected
            // exceptions.  Be very careful if modifying it.
            CompositeEntity previousContainer =
                (CompositeEntity)getContainer();
            if (previousContainer == container) return;

            // Do this first, because it may throw an exception, and we have
            // not yet changed any state.
            if (container != null) {
                // checkContainer() above ensures that this cast is valid.
                ((CompositeEntity)container)._addEntity(this);
                if (previousContainer == null) {
                    _workspace.remove(this);
                }
            }
            super.setContainer(container);
            if (previousContainer != null) {
                // This is safe now because it does not throw an exception.
                previousContainer._removeEntity(this);
            }
            if (container == null) {
                Iterator ports = portList().iterator();
                while (ports.hasNext()) {
                    Port port = (Port)ports.next();
                    port.unlinkAll();
                }
            } else {
                // checkContainer() above ensures that this cast is valid.
                ((CompositeEntity)container)._finishedAddEntity(this);

                // We have successfully set a new container for this
                // object. Mark it modified to ensure MoML export.
                // EAL 12/03, 2/04
                setModifiedFromClass(true);
            }
            // Validate all deeply contained settables, since
            // they may no longer be valid in the new context.
            validateSettables();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the name of the ComponentEntity. If there is already
     *  a ComponentEntity of the container with the same name, throw an
     *  exception.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there already is an entity
     *   in the container with the same name.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        if (name == null) {
            name = new String("");
        }
        CompositeEntity container = (CompositeEntity) getContainer();
        if ((container != null)) {
            ComponentEntity another = (ComponentEntity)
                container.getEntity(name);
            if ((another != null) && (another != this)) {
                throw new NameDuplicationException(container,
                        "Name duplication: " + name);
            }
        }
        super.setName(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
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
        
    /** Check the specified container.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not an
     *   instnance of CompositeEntity.
     */
    protected void _checkContainer(Prototype container)
            throws IllegalActionException {
        if (container != null && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
            "Component entity can only be contained by a CompositeEntity");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addIcon() {
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" width=\"60\" " +
                "height=\"40\" style=\"fill:white\"/>\n" +
                "<polygon points=\"-20,-10 20,0 -20,10\" " +
                "style=\"fill:blue\"/>\n" +
                "</svg>\n");
    }
}
