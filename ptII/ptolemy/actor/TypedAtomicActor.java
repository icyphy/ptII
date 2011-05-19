/* An executable entity whose ports have types.

 Copyright (c) 1997-2010 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.util.Time;
import ptolemy.data.type.Typeable;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TypedAtomicActor

/**
 A TypedAtomicActor is an AtomicActor whose ports and parameters have types.
 <p>
 The typeConstraints() method returns the type constraints among
 the contained ports and parameters.  This base class provides a default
 implementation of this method, which should be suitable for most of the
 derived classes.
 <p>
 Derived classes may constrain the container by overriding
 _checkContainer(). The Ports of TypedAtomicActors are constrained to be
 TypedIOPorts.  Derived classes may further constrain the ports by
 overriding the public method newPort() to create a port of the
 appropriate subclass, and the protected method _addPort() to throw an
 exception if its argument is a port that is not of the appropriate
 subclass.

 @author Yuhong Xiong
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 @see ptolemy.actor.AtomicActor
 @see ptolemy.actor.TypedCompositeActor
 @see ptolemy.actor.TypedIOPort
 */
public class TypedAtomicActor extends AtomicActor implements TypedActor {
    // All the constructors are wrappers of the super class constructors.

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
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
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TypedAtomicActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the type of an attribute.  This method is
     *  called by a contained attribute when its type changes.
     *  In this base class, the method informs the director to invalidate
     *  type resolution, if the director is not null.
     *  Thus, by default, attribute type changes cause type resolution to
     *  be redone at the next opportunity.
     *  If an actor does not allow attribute types to change, then it should
     *  override this method.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     *   Derived classes can throw this exception if type change is not allowed.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        Director director = getDirector();

        if (director != null) {
            director.invalidateResolvedTypes();
        }
    }

    /** clone() is not supported, call clone(Workspace workspace)
     *  instead.  Usually it is a mistake for an actor to have a
     *  clone() method and call super.clone(), instead the actor
     *  should have a clone(Workspace workspace) method and
     *  call super.clone(workspace).
     *  @exception CloneNotSupportedException Always thrown.
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported "
                + "in actors, call clone(Workspace workspace) instead. "
                + "Sometimes actors are mistakenly written to have a "
                + "clone() method instead of a "
                + "clone(Workspace workspace) method.");
    }

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
            _workspace.getWriteAccess();

            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return the type constraints of this actor.
     *  The constraints have the form of a list of inequalities.
     *  In this base class, if an input port and an output port do not have
     *  their types declared, and they do not have any constraints stored in
     *  them, a constraint is set up that requires the type of the input
     *  to be less than or equal to the type of the output.
     *  In addition, this method also collects type constraints from the
     *  contained Typeables (ports, variables, and parameters).
     *  This method is read-synchronized on the workspace.
     *  @return A list of instances of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    public Set<Inequality> typeConstraints() {
        try {
            _workspace.getReadAccess();

            Set<Inequality> result = new HashSet<Inequality>();
            Iterator inPorts = inputPortList().iterator();

            while (inPorts.hasNext()) {
                TypedIOPort inPort = (TypedIOPort) inPorts.next();
                boolean isUndeclared = inPort.getTypeTerm().isSettable();

                if (isUndeclared) {
                    // inPort has undeclared type.
                    Iterator outPorts = outputPortList().iterator();

                    while (outPorts.hasNext()) {
                        TypedIOPort outPort = (TypedIOPort) outPorts.next();

                        isUndeclared = outPort.getTypeTerm().isSettable();

                        if (isUndeclared && (inPort != outPort)) {
                            // output also undeclared, not bidirectional port,
                            // check if there is any type constraints stored
                            // in ports.
                            Set<Inequality> inPortConstraints = inPort
                                    .typeConstraints();
                            Set<Inequality> outPortConstraints = outPort
                                    .typeConstraints();

                            if (inPortConstraints.isEmpty()
                                    && outPortConstraints.isEmpty()) {
                                // ports not constrained, use default
                                // constraint
                                Inequality inequality = new Inequality(inPort
                                        .getTypeTerm(), outPort.getTypeTerm());
                                result.add(inequality);
                            }
                        }
                    }
                }
            }

            // collect constraints from contained Typeables
            Iterator ports = portList().iterator();

            while (ports.hasNext()) {
                Typeable port = (Typeable) ports.next();
                result.addAll(port.typeConstraints());
            }

            Iterator typeables = attributeList(Typeable.class).iterator();

            while (typeables.hasNext()) {
                Typeable typeable = (Typeable) typeables.next();
                result.addAll(typeable.typeConstraints());
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the type constraints of this variable.
     *  The constraints include the ones explicitly set to this variable,
     *  plus the constraint that the type of this variable must be no less
     *  than its current type, if it has one.
     *  The constraints are a list of inequalities.
     *  @return a list of Inequality objects.
     *  @see ptolemy.graph.Inequality
     *  @deprecated Use typeConstraints().
     */
    public List typeConstraintList() {
        LinkedList result = new LinkedList();
        result.addAll(typeConstraints());
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to throw an exception if the added port
     *  is not an instance of TypedIOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this actor.
     *  Derived classes may override this method to further constrain the
     *  port to be a subclass of TypedIOPort. This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @param port The port to add to this actor.
     *  @exception IllegalActionException If the port is not an instance
     *   of TypedIOPort, or the port has no name.
     *  @exception NameDuplicationException If the port name coincides with
     *   the name of another port already in the actor.
     */
    protected void _addPort(Port port) throws IllegalActionException,
            NameDuplicationException {
        // In the future, this method can be changed to allow IOPort to be
        // added. In that case, the type system just ignores instances of
        // IOPort during type checking. Since there is no intended application
        // for that change yet, constrain the port to be TypedIOPort for now.
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this actor.");
        }

        super._addPort(port);
    }

    /** Request a firing of this actor at the specified time
     *  and throw an exception if the director does not agree to
     *  do it at the requested time. This is a convenience method
     *  provided because many actors need it.
     *  @param time The requested time.
     *  @exception IllegalActionException If the director does not
     *   agree to fire the actor at the specified time, or if there
     *   is no director.
     */
    protected void _fireAt(Time time) throws IllegalActionException {
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director.");
        }
        Time result = director.fireAt(this, time);
        if (!result.equals(time)) {
            throw new IllegalActionException(this,
                    "Director is unable to fire the actor at the requested time: "
                            + time + ". It responds it will fire it at: "
                            + result);
        }
    }

    /** Request a firing of this actor at the specified time
     *  and throw an exception if the director does not agree to
     *  do it at the requested time. This is a convenience method
     *  provided because many actors need it.
     *  @param time The requested time, as a double.
     *  @exception IllegalActionException If the director does not
     *   agree to fire the actor at the specified time, or if there is
     *   no director.
     */
    protected void _fireAt(double time) throws IllegalActionException {
        // Need to use the Time class to ensure we take into account
        // the time resolution.
        _fireAt(new Time(getDirector(), time));
    }
}
