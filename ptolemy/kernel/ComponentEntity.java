/* A ComponentEntity is a vertex in a hierarchical graph.

 Copyright (c) 1997 The Regents of the University of California.
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

    /** Create an object with no name and no container */	
    public ComponentEntity() {
         super();
    }

    /** Create an object with a name and no container. 
     *  @param name
     *  @exception IllegalActionException Argument is null.
     */	
    public ComponentEntity(String name)
           throws IllegalActionException {
        super(name);
    }

    /** Create an object with a name and a container. 
     *  @param container
     *  @param name
     *  @exception IllegalActionException name argument is null.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the container's contents list.
     */	
    public ComponentEntity(CompositeEntity container, String name)
           throws IllegalActionException, NameDuplicationException {
        super(name);
        // NOTE: This unnecessarily does the check for recursive topologies.
        // Not sure how to avoid this, however.  Inconsistency is impossible
        // at this stage, so we silence the compiler by catching.
        try {
            setContainer(container);
        } catch (InvalidStateException ex) {}
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Append a port to the list of ports belonging to this entity.
     *  If the port has a container (other than this), remove it from
     *  the port list of that container.  Set the container of the port
     *  to point to this entity.  This overrides the base class to ensure
     *  that the port added is a ComponentPort.  If it is not, throw an
     *  exception.
     *  @param port
     *  @exception IllegalActionException Port is not of class ComponentPort.
     *  @exception InvalidStateException Inconsistent port-container
     *   relationship, or port has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *  on the port list.
     */	
    public void addPort(Port port) 
            throws IllegalActionException, InvalidStateException,
            NameDuplicationException {
        if (port instanceof ComponentPort) {
            super.addPort(port);
        } else {
            throw new IllegalActionException(this, port,
                   "Component entities require ComponentPorts.");
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
     *  @param name
     *  @return The new port
     *  @exception IllegalActionException Argument is null.
     *  @exception NameDuplicationException Entity already has a port with
     *  that name.
     */	
    public Port newPort(String name) 
             throws IllegalActionException, NameDuplicationException {
        Port port = new ComponentPort(this, name);
        return port;
    }

    /** Set the container.  Throw an exception if this object directly
     *  or indirectly contains the proposed container.  Unless the argument
     *  is null, add the object to the entity list of the new container.
     *  If this object was previously contained, remove it from the entity
     *  list of the old container.
     *  @param container
     *  @exception IllegalActionException Recursive containment structure.
     *  @exception InvalidStateException Inconsistent containment relationship.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the contents list of the container.
     */	
    public void setContainer(CompositeEntity container) 
            throws IllegalActionException, InvalidStateException,
            NameDuplicationException {
        if (container != null) {
            // To ensure consistency, this is handled by the container.
            container.addEntity(this);
        } else {
            if (_container != null) {
                // To ensure consistency, this is handled by the container.
                // If this throws an IllegalActionException, then the container
                // does not contain me, which is an inconsistent state.
                try {
                    ((CompositeEntity)_container).removeEntity(this);
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException(_container, this,
                          "Inconsistent containment relationship!");
                }
            } else {
                // Both the old and the new container are null.  Do nothing.
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Set the container.  This protected
     *  method should be called _only_ by CompositeEntity.addEntity() and
     *  CompositeEntity.removeEntity().
     *  This way, synchronization and consistency are ensured.
     *  Note that this method does nothing about removing this entity
     *  from the previous container's contents, nor does it check for
     *  recursive containment errors.
     *  @param container
     */	
    protected void _setContainer(CompositeEntity container) {
	_container = container;
    }
}
