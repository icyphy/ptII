/* An executable entity whose ports have types.

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

*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import ptolemy.graph.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TypedAtomicActor
/**
A TypedAtomicActor is an AtomicActor whose ports have types.
The typeConstraints() method returns the type constraints among
the ports.  This base class provides a default implementation of this
method, which should be suitable for most of the derived classes.
The container is required to be an instance of TypedCompositeActor.
Derived classes may further constrain the container by overriding
setContainer(). The Ports of TypedAtomicActors are constrained to be
TypedIOPorts.  Derived classes may further constrain the ports by
overriding the public method newPort() to create a port of the
appropriate subclass, and the protected method _addPort() to throw an
exception if its argument is a port that is not of the appropriate
subclass.

@author Yuhong Xiong
$Id$
@see ptolemy.actors.AtomicActor
@see ptolemy.actors.TypedCompositeActor
@see ptolemy.actors.TypedIOPort
*/
public class TypedAtomicActor extends AtomicActor implements TypedActor {

    /** Construct an actor in the default workspace with an empty string
     *  The object is added to the workspace directory.
     *  as its name. Increment the version number of the workspace.
     */
    public TypedAtomicActor() {
	super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public TypedAtomicActor(Workspace workspace) {
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
    public TypedAtomicActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " + ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of TypedCompositeActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the actor from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the argument is not a TypedCompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof TypedCompositeActor) &&
             (container != null)) {
            throw new IllegalActionException(container, this,
                    "TypedAtomicActor can only be contained by instances of " +
                    "TypedCompositeActor.");
        }
        super.setContainer(container);
    }

    /** Return the type constraints of this actor.
     *  The constraints is an enumeration of inequalities.
     *  In this base class, the default implementation of type constraints
     *  is: If all the ports of this actor have undeclared type,
     *  then the type of any input port must be less than or equal to
     *  the type of any output port, unless the input port and the output
     *  port are the same; If at least one of the ports has a declared
     *  type, then no constraint is generated.  In the latter case, an empty
     *  enumeration is returned.
     *  This method is read-synchronized on the workspace.
     *  @return an Enumeration of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    public Enumeration typeConstraints()  {
	try {
	    workspace().getReadAccess();

	    LinkedList result = new LinkedList();
	    // check if all the ports are undeclared
	    boolean allUndeclared = true;
            for (Enumeration ports = getPorts(); ports.hasMoreElements() ;) {
            	TypedIOPort p = (TypedIOPort)ports.nextElement();
            	if (p.declaredType() != null) {
		    // at least one port has a declared type
		    allUndeclared = false;
		    break;
	    	}
	    }

	    if (allUndeclared) {
	    	for (Enumeration inPorts = inputPorts();
                     inPorts.hasMoreElements() ;) {
	    	    TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
	    	    for (Enumeration outPorts = outputPorts();
                         outPorts.hasMoreElements() ;) {
		    	TypedIOPort outport =
                            (TypedIOPort)outPorts.nextElement();

		    	if (inport != outport) {
		            // not bi-directional port
		            Inequality ineq = new Inequality(inport, outport);
			    ineq.addVariable(inport);
			    ineq.addVariable(outport);
			    result.insertLast(ineq);
		        }
		    }
	        }
	    }

	    return result.elements();
	}finally {
	    workspace().doneReading();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to throw an exception if the added port
     *  is not an instance of TypedIOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of TypedIOPort. This method is <i>not</i> synchronized on
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
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this entity.");
        }
        super._addPort(port);
    }

    // NOTE: There is nothing new to report in the _description() method,
    // so we do not override it.

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}

