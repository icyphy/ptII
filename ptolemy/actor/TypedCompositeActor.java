/* An aggregation of typed actors.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.Typeable;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor
/**
A TypedCompositeActor is an aggregation of typed actors.
<p>
When exporting MoML, instances of this class identify their class name
as TypedCompositeActor. If a derived class does not change this, then it
too will be identified as a TypedCompositeActor. To change this in a
derived class, put the following line in the constructor
<pre>
getMoMLInfo().className = "<i>full class name</i>";
</pre>
If you do this, you will probably also want to override _exportMoMLContents()
to not generate a description of the contents of the composite, since
they will be already defined in the Java class.
<p>
The ports of a TypedCompositeActor are constrained to be TypedIOPorts,
the relations to be TypedIORelations, and the actors to be instances of
ComponentEntity that implement the TypedActor interface.  Derived classes
may impose further constraints by overriding newPort(), _addPort(),
newRelation(), _addRelation(), and _addEntity(). Also, derived classes may
constrain the container by overriding _checkContainer().
<P>

@author Yuhong Xiong
@version $Id$
@see ptolemy.actor.TypedIOPort
@see ptolemy.actor.TypedIORelation
@see ptolemy.actor.TypedActor
@see ptolemy.kernel.ComponentEntity
@see ptolemy.actor.CompositeActor
*/
public class TypedCompositeActor extends CompositeActor implements TypedActor {

    // all the constructors are wrappers of the super class constructors.

    /** Construct a TypedCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     */
    public TypedCompositeActor() {
        super();
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is TypedCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as TypedCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be TypedCompositeActor.
        getMoMLInfo().className = "ptolemy.actor.TypedCompositeActor";
    }

    /** Construct a TypedCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public TypedCompositeActor(Workspace workspace) {
	super(workspace);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is TypedCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as TypedCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be TypedCompositeActor.
        getMoMLInfo().className = "ptolemy.actor.TypedCompositeActor";
    }

    /** Construct a TypedCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TypedCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is TypedCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as TypedCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be TypedCompositeActor.
        getMoMLInfo().className = "ptolemy.actor.TypedCompositeActor";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If this composite actor is opaque, perform static type checking.
     *  Specifically, this method scans all the connections within this
     *  composite between opaque TypedIOPorts, if the ports on both ends
     *  of the connection have declared types, the types of both ports
     *  are examined to see if the type of the port at the source end
     *  of the connection is less than or equal to the type at the
     *  destination port. If not, the two ports have a type conflict.
     *  If the type of the ports on one or both ends of a connection is
     *  not declared, the connection is skipped by this method and left
     *  to the type resolution mechanism.
     *  This method returns a List of TypedIOPorts that have
     *  type conflicts. If no type conflict is detected, an empty
     *  list is returned.
     *  If this TypedCompositeActor contains other opaque
     *  TypedCompositeActors, the checkType() methods of the contained
     *  TypedCompositeActors are called to check types further down the
     *  hierarchy.
     *  @return A list of TypedIOPort where type conflicts occur.
     *  @exception IllegalActionException If this composite actor is not
     *   opaque.
     */
    public List checkTypes() throws IllegalActionException {
	try {
	    workspace().getReadAccess();

	    if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot check types on a non-opaque actor.");
            }

	    List result = new LinkedList();

	    Iterator entities = deepEntityList().iterator();
	    while (entities.hasNext()) {
	        // check types on contained actors
	        TypedActor actor = (TypedActor)entities.next();
		if (actor instanceof TypedCompositeActor) {
		    result.addAll(((TypedCompositeActor)actor).checkTypes());
		}

	        // type check from all the ports on the contained actor
	        // to the ports that the actor can send data to.
		Iterator ports = ((Entity)actor).portList().iterator();
		while (ports.hasNext()) {
		    TypedIOPort sourcePort = (TypedIOPort)ports.next();
		    Receiver[][] receivers = sourcePort.getRemoteReceivers();

		    List destinationPorts = _receiverToPort(receivers);
		    result.addAll(_checkTypesFromTo(sourcePort,
                            destinationPorts));
		}
	    }

	    // also need to check connection from the input ports on
	    // this composite actor to input ports of contained actors.
	    Iterator boundaryPorts = portList().iterator();
	    while (boundaryPorts.hasNext()) {
		TypedIOPort sourcePort = (TypedIOPort)boundaryPorts.next();
		Receiver[][] receivers = sourcePort.deepGetReceivers();
		List destinationPorts = _receiverToPort(receivers);
	    	result.addAll(_checkTypesFromTo(sourcePort, destinationPorts));
	    }

	    return result;
	} finally {
	    workspace().doneReading();
	}
    }

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return A new TypedIOPort.
     *  @exception NameDuplicationException If this actor already has a
     *   port with the specified name.
     */
    public Port newPort(String name)
            throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedCompositeActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }

    /** Create a new TypedIORelation with the specified name, add it
     *  to the relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of TypedIORelation.
     *  This method is write-synchronized on the workspace.
     *
     *  @return A new TypedIORelation.
     *  @exception NameDuplicationException If name collides with a name
     *   already on the container's contents list.
     */
    public ComponentRelation newRelation(String name)
            throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIORelation relation = new TypedIORelation(this, name);
            return relation;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedCompositeActor.newRelation: Internal error: "
                    + ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return the type constraints of this typed composite actor, if it
     *  is opaque.
     *  The constraints have the form of a list of inequalities.
     *  The constraints come from three sources, the topology, the
     *  contained actors and the contained Typeables. To generate the
     *  constraints based on the topology, this method scans all the
     *  connections within this composite between opaque TypedIOPorts.
     *  If the type of the ports on one or both ends of a connection is
     *  not declared, a type constraint is formed that requires the
     *  type of the port at the source end of the connection to be less
     *  than or equal to the type at the destination port.
     *  To collect the type constraints from the contained actors,
     *  This method recursively calls the typeConstraintList() method of the
     *  deeply contained actors and combine all the constraints together.
     *  The type constraints from contained Typeables (ports and
     *  parameters) are collected by calling the typeConstraintList() method
     *  of all the contained Typeables.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *  @return a list of Inequality.
     *  @exception IllegalActionException If this composite actor is not
     *   opaque.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() throws IllegalActionException {
	try {
	    workspace().getReadAccess();

	    if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot check types on a non-opaque actor.");
            }

	    List result = new LinkedList();
	    Iterator entities = deepEntityList().iterator();
	    while (entities.hasNext()) {
	        // collect type constraints from contained actors
	        TypedActor actor = (TypedActor)entities.next();
	        result.addAll(actor.typeConstraintList());

	        // collect constraints on all the ports in the contained
		// actor to the ports that the actor can send data to.
		Iterator ports = ((Entity)actor).portList().iterator();
		while (ports.hasNext()) {
		    TypedIOPort sourcePort = (TypedIOPort)ports.next();
                    Receiver[][] receivers = sourcePort.getRemoteReceivers();

                    List destinationPorts = _receiverToPort(receivers);
                    result.addAll(_typeConstraintsFromTo(sourcePort,
                            destinationPorts));
		}
            }

	    // also need to check connection from the input ports on
            // this composite actor to input ports of contained actors.
	    Iterator boundaryPorts = portList().iterator();
            while (boundaryPorts.hasNext()) {
                TypedIOPort sourcePort = (TypedIOPort)boundaryPorts.next();
                Receiver[][] receivers = sourcePort.deepGetReceivers();
                List destinationPorts = _receiverToPort(receivers);
                result.addAll(_typeConstraintsFromTo(sourcePort,
                        destinationPorts));
            }

	    // collect constraints from contained Typeables
	    Iterator ports = portList().iterator();
	    while (ports.hasNext()) {
		Typeable port = (Typeable)ports.next();
		result.addAll(port.typeConstraintList());
	    }

	    Iterator attributes = attributeList().iterator();
	    while (attributes.hasNext()) {
		Attribute attribute = (Attribute)attributes.next();
		if (attribute instanceof Typeable) {
		    result.addAll(((Typeable)attribute).typeConstraintList());
		}
	    }

	    return result;
	} finally {
	    workspace().doneReading();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an actor to this container with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  implements the TypedActor interface. This
     *  method does not alter the actor in any way.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity TypedActor to contain.
     *  @exception IllegalActionException If the actor has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument does not implement the TypedActor interface.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the actor contents list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof TypedActor)) {
            throw new IllegalActionException(this, entity,
                    "TypedCompositeActor can only contain entities that " +
		    "implement the TypedActor interface.");
        }
        super._addEntity(entity);
    }

    /** Add a port to this actor. This overrides the base class to
     *  throw an exception if the proposed port is not an instance of
     *  TypedIOPort.  This method should not be used directly.  Call the
     *  s() method of the port instead. This method does not set
     *  the container of the port to point to this actor.
     *  It assumes that the port is in the same workspace as this
     *  actor, but does not check.  The caller should check.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param port The TypedIOPort to add to this actor.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this actor, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this, port,
                    "TypedCompositeActor can only contain instances of " +
		    "TypedIOPort.");
        }
        super._addPort(port);
    }

    /** Add a relation to this container. This overrides the base class to
     *  throw an exception if the proposed relation is not an instance of
     *  TypedIORelation. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set the container of the relation to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The TypedIORelation to contain.
     *  @exception IllegalActionException If the relation has no name, or is
     *   not an instance of TypedIORelation.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "TypedCompositeActor can only contain instances of " +
		    "TypedIORelation.");
        }
        super._addRelation(relation);
    }


    // NOTE: There is nothing new to report in the _description() method,
    // so we do not override it.

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check types from a source port to a group of destination ports,
    // assuming the source port is connected to all the ports in the
    // group of destination ports.  Return an list of
    // TypedIOPorts that have type conflicts.
    private List _checkTypesFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
	List result = new LinkedList();

	boolean isUndeclared = sourcePort.getTypeTerm().isSettable();
	if (!isUndeclared) {
	    // sourcePort has a declared type.
	    Type srcDeclared = sourcePort.getType();
	    Iterator destinationPorts = destinationPortList.iterator();
	    while (destinationPorts.hasNext()) {
            	TypedIOPort destinationPort = (TypedIOPort)destinationPorts.next();
		isUndeclared = destinationPort.getTypeTerm().isSettable();

	    	if (!isUndeclared) {
	    	    // both source/destination ports are declared, check type
	    	    Type destDeclared = destinationPort.getType();
		    int compare = TypeLattice.compare(srcDeclared,
                            destDeclared);
		    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
		    	result.add(sourcePort);
		    	result.add(destinationPort);
	    	    }
		}
	    }
        }
	return result;
    }

    // Return all the ports containing the specified receivers.
    private List _receiverToPort(Receiver[][] receivers) {
	List result = new LinkedList();
	if (receivers != null) {
	    for (int i = 0; i < receivers.length; i++) {
		if (receivers[i] != null) {
		    for (int j = 0; j < receivers[i].length; j++) {
			result.add(receivers[i][j].getContainer());
		    }
		}
	    }
	}
	return result;
    }

    // Return the type constraints on all connections starting from the
    // specified source port to all the ports in a group of destination
    // ports.
    private List _typeConstraintsFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
	List result = new LinkedList();

	boolean srcUndeclared = sourcePort.getTypeTerm().isSettable();
	Iterator destinationPorts = destinationPortList.iterator();
	while (destinationPorts.hasNext()) {
            TypedIOPort destinationPort = (TypedIOPort)destinationPorts.next();
	    boolean destUndeclared = destinationPort.getTypeTerm().isSettable();

	    if (srcUndeclared || destUndeclared) {
	    	// at least one of the source/destination ports does not have
		// declared type, form type constraint.
		Inequality ineq = new Inequality(sourcePort.getTypeTerm(),
                        destinationPort.getTypeTerm());
		result.add(ineq);
	    }
	}

	return result;
    }
}
