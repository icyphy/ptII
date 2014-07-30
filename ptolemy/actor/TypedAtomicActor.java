/* An executable entity whose ports have types.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.Time;
import ptolemy.data.type.Typeable;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TypedAtomicActor

/**
 A TypedAtomicActor is an AtomicActor whose ports and parameters have types.
 <p>
 The final method typeConstraints() returns the type constraints among the
 contained ports and parameters. It gathers these constraints by invoking
 three different protected methods (listed in order of execution):</p>
 <ul>
   <li> _customTypeConstraints()</li>
   <li> _defaultTypeConstraints()</li>
   <li> _containedTypeConstraints()</li>
 </ul>

 <p>
 Derived classes may constrain the container by overriding
 _checkContainer(). The Ports of TypedAtomicActors are constrained to be
 TypedIOPorts.  Derived classes may further constrain the ports by
 overriding the public method newPort() to create a port of the
 appropriate subclass, and the protected method _addPort() to throw an
 exception if its argument is a port that is not of the appropriate
 subclass.
 </p>
 @author Yuhong Xiong, Marten Lohstroh
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 @see ptolemy.actor.AtomicActor
 @see ptolemy.actor.TypedCompositeActor
 @see ptolemy.actor.TypedIOPort
 */
public class TypedAtomicActor extends AtomicActor<TypedIOPort> implements
TypedActor {
    // All the constructors are wrappers of the super class constructors.

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public TypedAtomicActor() {
        super();
        _init();
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
        _init();
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
        _init();
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
    @Override
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        Director director = getDirector();

        if (director != null) {
            director.invalidateResolvedTypes();
        }
        _typesValid = false; // Set flag to invalidate cached type constraints
    }

    /** clone() is not supported, call clone(Workspace workspace)
     *  instead.  Usually it is a mistake for an actor to have a
     *  clone() method and call super.clone(), instead the actor
     *  should have a clone(Workspace workspace) method and
     *  call super.clone(workspace).
     *  @exception CloneNotSupportedException Always thrown.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported "
                + "in actors, call clone(Workspace workspace) instead. "
                + "Sometimes actors are mistakenly written to have a "
                + "clone() method instead of a "
                + "clone(Workspace workspace) method.");
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TypedAtomicActor newObject = (TypedAtomicActor) super.clone(workspace);

        // When the user calls typeConstraints(), the _typeConstraints object
        // will be updated.
        newObject._cachedTypeConstraints = new HashSet<Inequality>();
        newObject._typeConstraintsVersion = -1;
        newObject._typesValid = false;

        return newObject;
    }

    /** Return true if backward type inference is enabled in the first opaque
     *  composite actor up the hierarchy, or false otherwise.
     * @return true If backward type inference is enabled, or false otherwise.
     */
    @Override
    public boolean isBackwardTypeInferenceEnabled() {
        NamedObj container = getContainer();
        if (container != null && container instanceof TypedActor) {
            return ((TypedActor) container).isBackwardTypeInferenceEnabled();
        }
        return false;
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
    @Override
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
     *  The constraints have the form of a list of inequalities that are
     *  gathered by calling a set of protected non-final methods that can be
     *  overridden for customization.
     *
     *  First, <code>_customTypeConstraints()</code> is called. This method is
     *  defined as an empty stub in the base class, that is to be overridden by
     *  subclasses that require a specific set of constraints to be setup.
     *  Second, <code>_defaultTypeConstraints()</code> is called. Its purpose is
     *  to setup type constraints between inputs and outputs that have no types
     *  declared. It ensures that outputs are greater than or equal to inputs,
     *  meaning that lossless conversion is possible on the tokens that pass
     *  through the actor.
     *  Finally, <code>_containedTypeConstraints()</code> is called to collect
     *  all type constraints that are stored in the contained Typeables.
     *
     *  Note that all constraints are cached and only recomputed if necessary.
     *  This method is read-synchronized on the workspace.
     *  @return A list of instances of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    @Override
    public final Set<Inequality> typeConstraints() {
        // TODO: Rather disable type resolution completely if there where not topology or type changes
        // do not update if the cached constraints are still valid
        //if (_typesValid && _typeConstraintsVersion == workspace().getVersion()) {
        //  return _cachedTypeConstraints;
        //}
        // clear the cached typed constraints
        _cachedTypeConstraints.clear();

        try {
            _workspace.getReadAccess();

            Set<Inequality> cts = null;

            // setup custom constraints
            if ((cts = _customTypeConstraints()) != null) {
                _cachedTypeConstraints.addAll(cts);
            }

            // setup default constraints
            if ((cts = _defaultTypeConstraints()) != null) {
                _cachedTypeConstraints.addAll(cts);
            }

            // collect constraints from contained Typeables
            if ((cts = _containedTypeConstraints()) != null) {
                _cachedTypeConstraints.addAll(cts);
            }

            // validate cached type constraints
            _typeConstraintsVersion = _workspace.getVersion();
            _typesValid = true;

            return _cachedTypeConstraints;
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
    @Deprecated
    public List<Inequality> typeConstraintList() {
        LinkedList<Inequality> result = new LinkedList<Inequality>();
        result.addAll(typeConstraints());
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Collect all type constraints from contained Typeables (ports,
     *  variables, and parameters).
     *  @return A set of type constraints
     */
    protected Set<Inequality> _containedTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();
        Iterator<TypedIOPort> ports = portList().iterator();
        while (ports.hasNext()) {
            result.addAll(ports.next().typeConstraints());
        }

        Iterator<Typeable> typeables = attributeList(Typeable.class).iterator();

        while (typeables.hasNext()) {
            result.addAll(typeables.next().typeConstraints());
        }
        return result;
    }

    /** Return the default type constraints. These constraints involve only
     *  undeclared ports and require outputs to be greater than or equal to
     *  inputs.
     *
     *  In addition, if backward type inference is enabled, then also
     *  establish constraint that require inputs to be greater than or equal
     *  to outputs. With backward type inference, the types of undeclared
     *  inputs and outputs are unified.
     *
     *  Override this method to eliminate the default type constraints, or to
     *  specify different ones.
     *
     * @return A set of type constraints
     */
    protected Set<Inequality> _defaultTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();

        for (TypedIOPort input : inputPortList()) {
            for (TypedIOPort output : outputPortList()) {
                Set<Inequality> inPortConstraints = input.typeConstraints();
                Set<Inequality> outPortConstraints = output.typeConstraints();

                // 1) no default constraint if input port is output port, or
                //    if one of both ports have a declared type, or the port
                //    is a ParameterPort
                if (input == output || !input.getTypeTerm().isSettable()
                        || !output.getTypeTerm().isSettable()
                        || input instanceof ParameterPort) {
                    continue;
                }
                // 2) only set default constraint of none are set already
                if (inPortConstraints.isEmpty() && outPortConstraints.isEmpty()) {
                    result.add(new Inequality(input.getTypeTerm(), output
                            .getTypeTerm()));
                    if (isBackwardTypeInferenceEnabled()) {
                        result.add(new Inequality(output.getTypeTerm(), input
                                .getTypeTerm()));
                    }
                }
            }
        }
        return result;
    }

    /** Empty stub to be used for setting up custom type constraints for
     *  subclasses of this actor.
     *  @return null
     */
    protected Set<Inequality> _customTypeConstraints() {
        return null;
    }

    /** Request a firing of this actor at the specified time
     *  and throw an exception if the director does not agree to
     *  do it at the requested time. This is a convenience method
     *  provided because many actors need it.
     *  @param time The requested time.
     *  @exception IllegalActionException If the director does not
     *  agree to fire the actor at the specified time, or if there
     *  is no director.
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Whether or not the resolved types are still valid. */
    protected boolean _typesValid;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initialize the variables that keep track of the validity of the cached
     * type constraints.
     */
    private void _init() {

        _cachedTypeConstraints = new HashSet<Inequality>();
        _typeConstraintsVersion = -1;
        _typesValid = false;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Cached set of type constraints. */
    private Set<Inequality> _cachedTypeConstraints;

    /** Version number when the cache was last updated. */
    @SuppressWarnings("unused")
    private long _typeConstraintsVersion;

}
