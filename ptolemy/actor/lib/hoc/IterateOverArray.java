/* An actor that iterates a contained actor over input arrays.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ArrayElementTypeFunction;
import ptolemy.actor.util.GLBFunction;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// IterateOverArray

/**
 This actor iterates the contained actor or model over input arrays.
 To use it, either drop an actor on it and provide arrays to the inputs,
 or use a default configuration where the actor is effectively a
 composite actor. In the latter case,
 you can simply look inside and
 populate that actor with a submodel that will be applied to the
 array elements.  The submodel is required to have a director.
 An SDF director will
 often be sufficient for operations taken on array elements,
 but other directors can be used as well.
 Note that this inside director should not impose a limit
 on the number of iterations of the inside model. If it does,
 then that limit will be respected, which may result in a failure
 to iterate over all the input data.
 <p>
 Each input port expects an array. When this actor fires,
 an array is read on each input port that has one, and its
 contents are provided sequentially to the contained actor or model.
 This actor then iterates the contained actor or model until either
 there are no more input data for the actor or the prefire()
 method of the actor or model
 returns false. If postfire() of the actor returns false,
 then postfire() of this actor will return false, requesting
 a halt to execution of the model.  The outputs from the
 contained actor are collected into arrays that are
 produced on the outputs of this actor.</p>
 <p>
 A special variable named "iterationCount" can be used in
 any expression setting the value of a parameter of this actor
 or its contents. This variable has an integer value that
 starts at 1 during the first iteration of the contained
 actor(s) and is incremented by 1 on each firing. If the
 inside actors consume one token on each firing, then
 its final value will be the size of the input array(s).</p>
 <p>
 This actor is properly viewed as a "higher-order component" in
 that its contained actor is a parameter that specifies how to
 operate on input arrays.  It is inspired by the higher-order
 functions of functional languages, but unlike those, the
 contained actor need not be functional. That is, it can have
 state.</p>
 <p>
 Note that you cannot place class definitions inside this
 actor. There should be no need to because class instances
 inside it can be instances of classes defined outside of it.</p>
 <p>
 This actor (and many of the other higher-order components)
 has its intellectual roots in the higher-order functions
 of functional languages, which have been in use since
 the 1970s. Similar actors were implemented in Ptolemy
 Classic, and are described in Lee &amp; Parks, "Dataflow
 Process Networks," <i>Proceedings of the IEEE</i>, 1995.
 Those were inspired by [2].
 Alternative approaches are found dataflow visual programming
 since the beginning (Sutherland in the 1960s, Prograph and
 Labview in the 1980s), and in time-based visual languages
 (Simulink in the 1990s).</p>
 <p>
 There are a number of known bugs or limitations in this
 implementation:</p>
 <ul>
 <li> FIXME: When you drop in an actor, and then another actor,
 and then select "undo," the second actor is deleted without
 the first one being re-created. Thus, undo is only a partial
 undo.  The fix to this is extremely complicated. Probably the
 only viable mechanism is to use UndoStackAttribute.getUndoInfo()
 to get the undo stack and then to manipulate the contents
 of that stack directly.</li>
 <li> FIXME: There should be an option to reset between
 firings of the inside actor.</li>
 <li> FIXME: If you drop a new actor onto an
 IterateOverArray in a subclass, it will replace the
 version inherited from the prototype. This is not right,
 since it violates the derivation invariant. Any attempt
 to modify the contained object in the prototype will trigger
 an exception.  There are two possible fixes. One is to
 relax the derivation invariant and allow derived objects
 to not perfectly mirror the hierarchy of the prototype.
 Another is for this class to somehow refuse to accept
 the new object in a subclass. But it is not obvious how
 to do this.</li>
 <li>
 FIXME: If an instance of IterateOverArray in a derived class has
 overridden values of parameters, those are lost if contained
 entity of the instance in the base class is replaced and
 then an undo is requested.</li>
 </ul>
 <b>References</b>
 <ol>
 <li> E. A. Lee and T. M. Parks, "Dataflow Process Networks,"
 Proceedings of the IEEE, 83(5): 773-801, May, 1995.</li>
 <li> H. J. Reekie,
<a href="http://ptolemy.eecs.berkeley.edu/~johnr/papers/thesis.html#in_browser">Realtime Signal Processing: Dataflow, Visual,
 and Functional Programming</a>," Ph.D. Thesis,
 University of Technology, Sydney, Sydney, Australia, 1995.</li>
 </ol>

 @author Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class IterateOverArray extends MirrorComposite {
    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  You should set a director before attempting to execute it.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public IterateOverArray(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an IterateOverArray in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public IterateOverArray(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. This overrides
     *  the base class to instantiate a new IterateDirector and to set
     *  the association with iterationCount.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        IterateOverArray result = (IterateOverArray) super.clone(workspace);
        try {
            // Remove the old inner IterateDirector(s) that is(are) in the wrong workspace.
            String iterateDirectorName = null;
            Iterator iterateDirectors = result.attributeList(
                    IterateDirector.class).iterator();
            while (iterateDirectors.hasNext()) {
                IterateDirector oldIterateDirector = (IterateDirector) iterateDirectors
                        .next();
                if (iterateDirectorName == null) {
                    iterateDirectorName = oldIterateDirector.getName();
                }
                oldIterateDirector.setContainer(null);
            }

            // Create a new IterateDirector that is in the right workspace.
            IterateDirector iterateDirector = result.new IterateDirector(
                    workspace);
            iterateDirector.setContainer(result);
            iterateDirector.setName(iterateDirectorName);
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }
        result._iterationCount = (Variable) result
                .getAttribute("iterationCount");
        return result;
    }

    /** Override the base class to return a specialized port.
     *  @param name The name of the port to create.
     *  @return A new instance of IteratePort, an inner class.
     *  @exception NameDuplicationException If the container already has a port
     *  with this name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            IteratePort result = new IteratePort(this, name);

            // NOTE: We would like prevent deletion via MoML
            // (or name changes, for that matter), but the following
            // also prevents making it an input, which makes
            // adding ports via the port dialog fail.
            // result.setDerivedLevel(1);
            // Force the port to be persistent despite being derived.
            // result.setPersistent(true);
            return result;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        }
    }

    /** Override the base class to ensure that the input ports of this
     *  actor all have array types.
     *  @return A list of instances of Inequality.
     *  @exception IllegalActionException If the typeConstraints
     *  of one of the deeply contained objects throws it.
     *  @see ptolemy.graph.Inequality
     */
    @Override
    public Set<Inequality> typeConstraints() throws IllegalActionException {
        Iterator ports = inputPortList().iterator();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            port.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
            // With backward type resolution, we also need the following
            // to ensure that the type does not resolve to GENERAL.
            port.setTypeAtMost(new ArrayType(BaseType.GENERAL));
        }

        return super.typeConstraints();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check types from a source port to a group of destination ports,
     *  assuming the source port is connected to all the ports in the
     *  group of destination ports.  Return a list of instances of
     *  Inequality that have type conflicts.  This overrides the base
     *  class so that if one of the ports belongs to this IterateOverArray
     *  actor, then its element type is compared against the inside port.
     *  @param sourcePort The source port.
     *  @param destinationPortList A list of destination ports.
     *  @return A list of instances of Inequality indicating the
     *   type constraints that are not satisfied.
     */
    @Override
    protected List _checkTypesFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
        List result = new LinkedList();

        boolean isUndeclared = sourcePort.getTypeTerm().isSettable();

        if (!isUndeclared) {
            // sourcePort has a declared type.
            Type srcDeclared = sourcePort.getType();
            Iterator destinationPorts = destinationPortList.iterator();

            while (destinationPorts.hasNext()) {
                TypedIOPort destinationPort = (TypedIOPort) destinationPorts
                        .next();
                isUndeclared = destinationPort.getTypeTerm().isSettable();

                if (!isUndeclared) {
                    // both source/destination ports are declared,
                    // check type
                    Type destinationDeclared = destinationPort.getType();

                    int compare;

                    // If the source port belongs to me, then we want to
                    // compare its array element type to the type of the
                    // destination.
                    if (sourcePort.getContainer() == this
                            && destinationPort.getContainer() != this) {
                        // The source port belongs to me, but not the
                        // destination.
                        Type srcElementType = ((ArrayType) srcDeclared)
                                .getElementType();
                        compare = TypeLattice.compare(srcElementType,
                                destinationDeclared);
                    } else if (sourcePort.getContainer() != this
                            && destinationPort.getContainer() == this) {
                        // The destination port belongs to me, but not
                        // the source.
                        Type destinationElementType = ((ArrayType) destinationDeclared)
                                .getElementType();
                        compare = TypeLattice.compare(srcDeclared,
                                destinationElementType);
                    } else {
                        compare = TypeLattice.compare(srcDeclared,
                                destinationDeclared);
                    }

                    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                        Inequality inequality = new Inequality(
                                sourcePort.getTypeTerm(),
                                destinationPort.getTypeTerm());
                        result.add(inequality);
                    }
                }
            }
        }

        return result;
    }

    /** Return the type constraints on all connections starting from the
     *  specified source port to all the ports in a group of destination
     *  ports. This overrides the base class to ensure that if the source
     *  port or the destination port is a port of this composite, then
     *  the port is forced to be an array type and the proper constraint
     *  on the element type of the array is made. If the source port
     *  has no possible sources of data, then no type constraints are
     *  added for it.
     *  @param sourcePort The source port.
     *  @return A list of instances of Inequality.
     */
    @Override
    protected List _destinationTypeConstraints(TypedIOPort sourcePort) {
        Iterator<IOPort> destinationPorts;
        List<Inequality> result = new LinkedList<Inequality>();
        boolean srcUndeclared = sourcePort.getTypeTerm().isSettable();

        if (sourcePort.isInput()) {
            destinationPorts = sourcePort.insideSinkPortList().iterator();
        } else {
            destinationPorts = sourcePort.sinkPortList().iterator();
        }

        while (destinationPorts.hasNext()) {
            TypedIOPort destinationPort = (TypedIOPort) destinationPorts.next();
            boolean destUndeclared = destinationPort.getTypeTerm().isSettable();

            if (srcUndeclared || destUndeclared) {
                // At least one of the source/destination ports does
                // not have declared type, form type constraint.
                if (sourcePort.getContainer() == this
                        && destinationPort.getContainer() == this) {
                    // Both ports belong to this.
                    // Require the output to be at least the input.
                    Inequality ineq1 = new Inequality(sourcePort.getTypeTerm(),
                            destinationPort.getTypeTerm());
                    result.add(ineq1);

                    // Finally, if backward type inference is enabled,
                    // require that the source array element type be greater
                    // than or equal to the GLB of all the destination ports.
                    if (isBackwardTypeInferenceEnabled()) {
                        InequalityTerm typeTerm = sourcePort.getTypeTerm();
                        if (typeTerm.isSettable()) {
                            result.add(new Inequality(new GLBArrayFunction(
                                    sourcePort), typeTerm));
                        }
                    }
                } else if (sourcePort.getContainer().equals(this)) {
                    // The source port belongs to this, so its array element
                    // type must be less than or equal to the type of the destination
                    // port.
                    if (sourcePort.sourcePortList().size() == 0) {
                        // Skip this port. It is not connected on the outside.
                        continue;
                    }

                    // Require the source port to be an array.
                    Inequality arrayInequality = new Inequality(
                            ArrayType.ARRAY_BOTTOM, sourcePort.getTypeTerm());
                    result.add(arrayInequality);

                    // Next require that the element type of the
                    // source port array be compatible with the
                    // destination port.
                    try {
                        Inequality ineq = new Inequality(
                                ArrayType.elementType(sourcePort),
                                destinationPort.getTypeTerm());
                        result.add(ineq);

                        // Finally, if backward type inference is enabled,
                        // require that the source array element type be greater
                        // than or equal to the GLB of all the destination ports.
                        if (isBackwardTypeInferenceEnabled()) {
                            InequalityTerm typeTerm = sourcePort.getTypeTerm();
                            if (typeTerm.isSettable()) {
                                result.add(new Inequality(new GLBArrayFunction(
                                        sourcePort), typeTerm));
                            }
                        }
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(e);
                    }

                } else if (destinationPort.getContainer().equals(this)) {
                    // Require that the destination port type be an array
                    // with elements compatible with the source port.
                    try {
                        Inequality ineq = new Inequality(
                                ArrayType.arrayOf(sourcePort),
                                destinationPort.getTypeTerm());
                        result.add(ineq);

                        // Also require that the source port type
                        // be greater than or equal to the GLB of all
                        // its destination ports (or array element types
                        // of destination ports that are ports of this
                        // IterateOverArray actor).
                        // This ensures that backward type inference occurs.
                        // NOTE: We used to do this only if backward type
                        // inference was enabled globally. But the cost of
                        // doing this locally is small, and there is no
                        // mechanism for coercing the type of the inside
                        // actor, so if we want to be able to coerce the
                        // type without backward type inference being
                        // enabled globally, then we need to do this here.
                        // if (isBackwardTypeInferenceEnabled()) {
                        InequalityTerm typeTerm = sourcePort.getTypeTerm();
                        if (typeTerm.isSettable()) {
                            result.add(new Inequality(
                                    new ArrayElementTypeFunction(
                                            destinationPort), typeTerm));
                        }
                        // }
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(e);
                    }
                }
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Variable that reflects the current iteration count on the
    // inside.
    private Variable _iterationCount;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        setClassName("ptolemy.actor.lib.hoc.IterateOverArray");

        // Create the IterateDirector in the proper workspace.
        IterateDirector iterateDirector = new IterateDirector(workspace());
        iterateDirector.setContainer(this);
        iterateDirector.setName(uniqueName("IterateDirector"));

        _iterationCount = new Variable(this, "iterationCount", new IntToken(0));
        _iterationCount.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// GLBArrayFunction

    /** This class implements a monotonic function that returns an array
     *  type with element type equal to the greatest
     *  lower bound (GLB) of its arguments, or if any
     *  of its arguments is itself a port belonging to
     *  this IterateOverArray actor, then its array element type.
     */
    private class GLBArrayFunction extends GLBArrayElementFunction {

        public GLBArrayFunction(TypedIOPort sourcePort) {
            super(sourcePort);
        }

        /** Return the current value of this monotonic function.
         *  @return A Type.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            Type elementType = (Type) super.getValue();
            return new ArrayType(elementType);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// GLBArrayElementFunction

    /** This class implements a monotonic function that returns the greatest
     *  lower bound (GLB) of its arguments or the array element type
     *  of its arguments. Specifically, if one of the arguments is a port
     *  belonging to the enclosing task, then it references the array element
     *  type of that port rather than the port type.
     */
    private class GLBArrayElementFunction extends GLBFunction {

        public GLBArrayElementFunction(TypedIOPort sourcePort) {
            super(sourcePort);
        }

        /** Return the current value of this monotonic function.
         *  @return A Type.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            _updateArguments();

            Set<Type> types = new HashSet<Type>();
            types.addAll(_cachedTypes);
            for (InequalityTerm _cachedTerm : _cachedTerms) {
                Object termObject = _cachedTerm.getAssociatedObject();
                if (termObject instanceof IOPort
                        && ((IOPort) termObject).getContainer() == IterateOverArray.this) {
                    // The type term belongs to a port of this IterateOverArray actor.
                    // Use its element type rather than its type.
                    Object value = _cachedTerm.getValue();
                    if (value instanceof ArrayType) {
                        types.add(((ArrayType) value).getElementType());
                    } else if (value.equals(BaseType.GENERAL)) {
                        // To ensure that this function is monotonic, we have to
                        // handle the case where the value is greater than ArrayType.
                        // The only thing greater than ArrayType is GENERAL.
                        types.add(BaseType.GENERAL);
                    }
                    // If the value is not an array type, then it must be unknown,
                    // so we don't need to add it to the collection. Adding unknown
                    // to the arguments to GLB does nothing.
                } else {
                    types.add((Type) _cachedTerm.getValue());
                }
            }
            // If there are no destination outputs at all, then set
            // the output type to unknown.
            if (types.size() == 0) {
                return BaseType.UNKNOWN;
            }
            // If there is only one destination, the GLB is equal to the
            // type of that port.
            if (types.size() == 1) {
                return types.toArray()[0];
            }

            return TypeLattice.lattice().greatestLowerBound(types);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// IterateComposite

    /** This is a specialized composite actor for use in IterateOverArray.
     *  In particular, it ensures that if ports are added or deleted
     *  locally, then corresponding ports will be added or deleted
     *  in the container.  That addition will result in appropriate
     *  connections being made.
     */
    public static class IterateComposite extends
    MirrorComposite.MirrorCompositeContents {
        // NOTE: This has to be a static class so that MoML can
        // instantiate it.

        /** Construct an actor with a name and a container.
         *  @param container The container.
         *  @param name The name of this actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public IterateComposite(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Override the base class to return a specialized port.
         *  @param name The name of the port to create.
         *  @return A new instance of IteratePort, an inner class.
         *  @exception NameDuplicationException If the container already has
         *  a port with this name.
         */
        @Override
        public Port newPort(String name) throws NameDuplicationException {
            try {
                return new IteratePort(this, name);
            } catch (IllegalActionException ex) {
                // This exception should not occur, so we throw a runtime
                // exception.
                throw new InternalErrorException(this, ex, null);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// IterateDirector

    /** This is a specialized director that fires contained actors
     *  in the order in which they appear in the actor list repeatedly
     *  until either there is no more input data for the actor or
     *  the prefire() method of the actor returns false. If postfire()
     *  of any actor returns false, then postfire() of this director
     *  will return false, requesting a halt to execution of the model.
     */
    private class IterateDirector extends Director {
        /** Construct an IterateDirector in the specified workspace with
         *  no container and an empty string as a name. You can then change
         *  the name with setName(). If the workspace argument is null, then
         *  use the default workspace.  You should set the local director or
         *  executive director before attempting to send data to the actor
         *  or to execute it. Add the actor to the workspace directory.
         *  Increment the version number of the workspace.
         *  @param workspace The workspace that will list the actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public IterateDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
            setPersistent(false);
        }

        /** Invoke iterations on the contained actor of the
         *  container of this director repeatedly until either it runs out
         *  of input data or prefire() returns false. If postfire() of the
         *  actor returns false, then set a flag indicating to postfire() of
         *  this director to return false.
         *  @exception IllegalActionException If any called method of
         *   of the contained actor throws it, or if the contained
         *   actor is not opaque.
         */
        @Override
        public void fire() throws IllegalActionException {
            // Don't call "super.fire();" here, this actor contains its
            // own director.
            CompositeActor container = (CompositeActor) getContainer();
            Iterator actors = container.entityList().iterator();
            _postfireReturns = true;

            while (actors.hasNext() && !_stopRequested) {
                Actor actor = (Actor) actors.next();

                if (!((ComponentEntity) actor).isOpaque()) {
                    throw new IllegalActionException(container,
                            "Inside actor is not opaque "
                                    + "(perhaps it needs a director).");
                }

                int result = Executable.COMPLETED;
                int iterationCount = 0;

                while (result != Executable.NOT_READY) {
                    iterationCount++;
                    _iterationCount.setToken(new IntToken(iterationCount));

                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.BEFORE_ITERATE, iterationCount));
                    }

                    result = actor.iterate(1);

                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_ITERATE, iterationCount));
                    }

                    // Should return if there is no more input data,
                    // irrespective of return value of prefire() of
                    // the actor, which is not reliable.
                    boolean outOfData = true;
                    Iterator inPorts = actor.inputPortList().iterator();

                    while (inPorts.hasNext()) {
                        IOPort port = (IOPort) inPorts.next();

                        for (int i = 0; i < port.getWidth(); i++) {
                            if (port.hasToken(i)) {
                                outOfData = false;
                                break;
                            }
                        }
                    }

                    if (outOfData) {
                        if (_debugging) {
                            _debug("No more input data for: "
                                    + ((Nameable) actor).getFullName());
                        }

                        break;
                    }

                    if (result == Executable.STOP_ITERATING) {
                        if (_debugging) {
                            _debug("Actor requests halt: "
                                    + ((Nameable) actor).getFullName());
                        }

                        _postfireReturns = false;
                        break;
                    }
                }
            }
        }

        /** Return a new instance of QueueReceiver.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        @Override
        public Receiver newReceiver() {
            return new QueueReceiver();
        }

        /** Override the base class to return the logical AND of
         *  what the base class postfire() method returns and the
         *  flag set in fire().  As a result, this will return
         *  false if any contained actor returned false in its
         *  postfire() method.
         */
        @Override
        public boolean postfire() throws IllegalActionException {
            boolean superReturns = super.postfire();
            return superReturns && _postfireReturns;
        }

        /** Transfer data from an input port of the
         *  container to the ports it is connected to on the inside.
         *  This method extracts tokens from the input array and
         *  provides them sequentially to the corresponding ports
         *  of the contained actor.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        @Override
        public boolean transferInputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;

            for (int i = 0; i < port.getWidth(); i++) {
                // NOTE: This is not compatible with certain cases
                // in PN, where we don't want to block on a port
                // if nothing is connected to the port on the
                // inside.
                try {
                    if (port.isKnown(i)) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);

                            if (_debugging) {
                                _debug(getName(), "transferring input from "
                                        + port.getName());
                            }

                            ArrayToken arrayToken = (ArrayToken) t;

                            for (int j = 0; j < arrayToken.length(); j++) {
                                port.sendInside(i, arrayToken.getElement(j));
                            }

                            result = true;
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }

            return result;
        }

        /** Transfer data from the inside receivers of an output port of the
         *  container to the ports it is connected to on the outside.
         *  This method packages the available tokens into a single array.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @see IOPort#transferOutputs
         */
        @Override
        public boolean transferOutputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;

            // Output type might be GENERAL, in which case, we
            // let the element type be GENERAL.
            Type elementType = BaseType.GENERAL;
            Type portType = ((TypedIOPort) port).getType();
            if (portType instanceof ArrayType) {
                elementType = ((ArrayType) portType).getElementType();
            }

            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    ArrayList list = new ArrayList();

                    while (port.isKnownInside(i) && port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        list.add(t);
                    }

                    if (list.size() != 0) {
                        Token[] tokens = (Token[]) list.toArray(new Token[list
                                                                          .size()]);

                        if (_debugging) {
                            _debug(getName(),
                                    "transferring output to " + port.getName());
                        }

                        port.send(i, new ArrayToken(elementType, tokens));
                    } else {
                        // Send an empty list
                        port.send(i, new ArrayToken(elementType));
                    }

                    result = true;
                } catch (NoTokenException ex) {
                    throw new InternalErrorException(this, ex, null);
                }
            }

            return result;
        }

        //////////////////////////////////////////////////////////////
        ////                   private variables                  ////
        // Indicator that at least one actor returned false in postfire.
        private boolean _postfireReturns = true;
    }

    ///////////////////////////////////////////////////////////////////
    //// IteratePort

    /** This is a specialized port for IterateOverArray.
     *  If the container is an instance of IterateOverArray,
     *  then it handles type conversions between
     *  the array types of the ports of the enclosing IterateOverArray
     *  actor and the scalar types (or arrays with one less dimension)
     *  of the actor that are contained.  It has a notion of an
     *  "associated port," and ensures that changes to the status
     *  of one port (whether it is input, output, or multiport)
     *  are reflected in the associated port.
     */
    public static class IteratePort extends MirrorPort {
        /** Construct a port in the specified workspace with an empty
         *  string as a name. You can then change the name with setName().
         *  If the workspace argument
         *  is null, then use the default workspace.
         *  The object is added to the workspace directory.
         *  Increment the version number of the workspace.
         *  @param workspace The workspace that will list the port.
         * @exception IllegalActionException If port parameters cannot be initialized.
         */
        public IteratePort(Workspace workspace) throws IllegalActionException {
            // This constructor is needed for Shallow codgen.
            super(workspace);
        }

        // NOTE: This class has to be static because otherwise the
        // constructor has an extra argument (the first argument,
        // actually) that is an instance of the enclosing class.
        // The MoML parser cannot know what the instance of the
        // enclosing class is, so it would not be able to instantiate
        // these ports.

        /** Create a new instance of a port for IterateOverArray.
         *  @param container The container for the port.
         *  @param name The name of the port.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @exception NameDuplicationException Not thrown in this base class.
         */
        public IteratePort(TypedCompositeActor container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            // NOTE: Ideally, Port are created when an entity is added.
            // However, there appears to be no clean way to do this.
            // Instead, ports are added when an entity is added via a
            // change request registered with this IterateOverArray actor.
            // Consequently, these ports have to be persistent, and this
            // constructor and class have to be public.
            // setPersistent(false);
        }

        /** Override the base class to convert the token to the element
         *  type rather than to the type of the port, unless the port
         *  is of type GENERAL, in which case, no conversion is necessary.
         *  @param token The token to convert.
         *  @return The converted token.
         *  @exception IllegalActionException If the conversion is
         *   invalid.
         */
        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (!(getContainer() instanceof IterateOverArray) || !isOutput()) {
                return super.convert(token);
            }
            if (getType().equals(BaseType.GENERAL)) {
                return token;
            }
            Type type = ((ArrayType) getType()).getElementType();

            if (type.equals(token.getType())) {
                return token;
            } else {
                Token newToken = type.convert(token);
                return newToken;
            }
        }

        /** Override the base class to convert the token to the element
         *  type rather than to the type of the port.
         *  @param channelIndex The index of the channel, from 0 to width-1
         *  @param token The token to send
         *  @exception NoRoomException If there is no room in the receiver.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        @Override
        public void sendInside(int channelIndex, Token token)
                throws IllegalActionException, NoRoomException {
            if (!(getContainer() instanceof IterateOverArray)) {
                super.sendInside(channelIndex, token);
                return;
            }

            Receiver[][] farReceivers;

            if (_debugging) {
                _debug("send inside to channel " + channelIndex + ": " + token);
            }

            if (_hasPortEventListeners) {
                _notifyPortEventListeners(new IOPortEvent(this,
                        IOPortEvent.SEND_BEGIN, channelIndex, true, token));
            }

            try {
                try {
                    _workspace.getReadAccess();

                    ArrayType type = (ArrayType) getType();
                    int compare = TypeLattice.compare(token.getType(),
                            type.getElementType());

                    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                        throw new IllegalActionException(
                                "Run-time type checking failed. Token type: "
                                        + token.getType().toString()
                                        + ", port: " + getFullName()
                                        + ", port type: "
                                        + getType().toString());
                    }

                    // Note that the getRemoteReceivers() method doesn't throw
                    // any non-runtime exception.
                    farReceivers = deepGetReceivers();

                    if (farReceivers == null
                            || farReceivers[channelIndex] == null) {
                        return;
                    }
                } finally {
                    _workspace.doneReading();
                }

                for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                    TypedIOPort port = (TypedIOPort) farReceivers[channelIndex][j]
                            .getContainer();
                    Token newToken = port.convert(token);
                    farReceivers[channelIndex][j].put(newToken);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // NOTE: This may occur if the channel index is out of range.
                // This is allowed, just do nothing.
            } finally {
                if (_hasPortEventListeners) {
                    _notifyPortEventListeners(new IOPortEvent(this,
                            IOPortEvent.SEND_END, channelIndex, true, token));
                }
            }
        }
    }
}
