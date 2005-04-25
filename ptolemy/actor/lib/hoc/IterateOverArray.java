/* An actor that iterates a contained actor over input arrays.

Copyright (c) 2004-2005 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
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
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.HandlesInternalLinks;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// IterateOverArray

/**
   This actor iterates the contained actor or model over input arrays.
   To use it, either drop an actor on it and provide arrays to the inputs,
   or use a default configuration where the actor contains
   an instance of IterateComposite. In the latter case,
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
   produced on the outputs of this actor.
   <p>
   A special variable named "iterationCount" can be used in
   any expression setting the value of a parameter of this actor
   or its contents. This variable has an integer value that
   starts at 1 during the first iteration of the contained
   actor(s) and is incremented by 1 on each firing. If the
   inside actors consume one token on each firing, then
   its final value will be the size of the input array(s).
   <p>
   This actor is properly viewed as a "higher-order component" in
   that its contained actor is a parameter that specifies how to
   operate on input arrays.  It is inspired by the higher-order
   functions of functional languages, but unlike those, the
   contained actor need not be functional. That is, it can have
   state.
   <p>
   Note that you cannot place class definitions inside this
   actor. There should be no need to because class instances
   inside it can be instances of classes defined outside of it.
   <p>
   This actor (and many of the other higher-order components)
   has its intellectual roots in the higher-order functions
   of functional languages, which have been in use since
   the 1970s. Similar actors were implemented in Ptolemy
   Classic, and are described in Lee & Parks, "Dataflow
   Process Networks," <i>Proceedings of the IEEE</i>, 1995.
   Those were inspired by [2].
   Alternative approaches are found dataflow visual programming
   since the beginning (Sutherland in the 1960s, Prograph and
   Labview in the 1980s), and in time-based visual languages
   (Simulink in the 1990s).
   <p>
   There are a number of known bugs or limitations in this
   implementation:
   <ul>
   <li>
   FIXME: When you drop in an actor, and then another actor,
   and then select "undo," the second actor is deleted without
   the first one being re-created. Thus, undo is only a partial
   undo.  The fix to this is extremely complicated. Probably the
   only viable mechanism is to use UndoStackAttribute.getUndoInfo()
   to get the undo stack and then to manipulate the contents
   of that stack directly.
   <li>
   FIXME: There should be an option to reset between
   firings of the inside actor.
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
   to do this.
   <li>
   FIXME: If an instance of IterateOverArray in a derived class has
   overridden values of parameters, those are lost if contained
   entity of the instance in the base class is replaced and
   then an undo is requested.
   </ul>
   <p><b>References</b>
   <p><ol>
   <li> E. A. Lee and T. M. Parks, "Dataflow Process Networks,"
   Proceedings of the IEEE, 83(5): 773-801, May, 1995.
   <li> H. J. Reekie, "Toward Effective Programming for
   Parallel Digital Signal Processing," Ph.D. Thesis,
   University of Technology, Sydney, Sydney, Australia, 1992.
   </ol>

   @author Edward A. Lee, Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (neuendor)
*/
public class IterateOverArray extends TypedCompositeActor
    implements HandlesInternalLinks {
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
        setClassName("ptolemy.actor.lib.hoc.IterateOverArray");
        new IterateDirector(this, uniqueName("IterateDirector"));

        _iterationCount = new Variable(this, "iterationCount", new IntToken(0));
        _iterationCount.setTypeEquals(BaseType.INT);

        _attachText("_iconDescription",
            "<svg>\n" + "<rect x=\"-30\" y=\"-20\" "
            + "width=\"60\" height=\"40\" " + "style=\"fill:white\"/>\n"
            + "<text x=\"-6\" y=\"10\"" + "style=\"font-size:24\">?</text>\n"
            + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. This overrides
     *  the base class to set up the associations in the mirror ports
     *  and to set a flag indicating that cloning is complete.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        IterateOverArray result = (IterateOverArray) super.clone(workspace);

        result._iterationCount = (Variable) result.getAttribute(
                "iterationCount");

        // Fix port associations.
        Iterator entities = result.entityList().iterator();

        while (entities.hasNext()) {
            Entity insideEntity = (Entity) entities.next();
            Iterator ports = result.portList().iterator();

            while (ports.hasNext()) {
                MirrorPort port = (MirrorPort) ports.next();
                Port insidePort = insideEntity.getPort(port.getName());

                if (insidePort instanceof MirrorPort) {
                    port.setAssociatedPort((MirrorPort) insidePort);
                }
            }
        }

        // Set a flag indicating the cloning is done.
        result._cloning = false;
        return result;
    }

    /** Override the base class to return a specialized port.
     *  This port is specified to always be derived,
     *  so it can only be deleted via kernel calls (not via the
     *  UI or via MoML).
     *  @param name The name of the port to create.
     *  @return A new instance of IteratePort, an inner class.
     *  @exception NameDuplicationException If the container already has a port
     *  with this name.
     */
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

    /** Override the base class to ensure that the ports of this
     *  actor all have array types.
     *  @return A list of instances of Inequality.
     *  @exception IllegalActionException If the typeConstraintList
     *  of one of the deeply contained objects throws it.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() throws IllegalActionException {
        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            ArrayType arrayType = new ArrayType(BaseType.UNKNOWN);
            ((TypedIOPort) port).setTypeEquals(arrayType);
        }

        super.getDirector();
        return super.typeConstraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to queue a change request to remove
     *  any previously contained entity and its mirrored ports, and
     *  to mirror the ports of the added entity.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException If the entity has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument does not implement the TypedActor interface.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the actor contents list, or if the added element is a
     *   class definition.
     */
    protected void _addEntity(ComponentEntity entity)
        throws IllegalActionException, NameDuplicationException {
        if (entity.isClassDefinition()) {
            throw new IllegalActionException(this,
                "Cannot place a class definition in an "
                + "IterateOverArray actor.");
        }

        super._addEntity(entity);

        // Issue a change request to add the appropriate
        // ports and connections to the new entity.
        ChangeRequest request = new ChangeRequest(this, // originator
                "Adjust contained entities, ports and parameters") {
                // Override this to indicate that the change is localized.
                // This keeps the EntityTreeModel from closing open libraries
                // when notified of this change.
                public NamedObj getLocality() {
                    return IterateOverArray.this;
                }

                protected void _execute() throws Exception {
                    // NOTE: We defer to a change request
                    // because only at this point can we be sure that the
                    // change request that triggered this has completed (i.e. that
                    // the entity being added has been added.
                    synchronized (this) {
                        try {
                            workspace().getWriteAccess();

                            // Entity most recently added.
                            ComponentEntity entity = null;

                            // Delete any previously contained entities.
                            // The strategy here is a bit tricky if this IterateOverArray
                            // is within a class definition (that is, if it has derived objects).
                            // The key is that derived objects do not permit deletion (via
                            // MoML) of contained entities. They cannot because this would
                            // violate the invariant of classes where derived objects
                            // always contain the same objects as their parents.
                            // Thus, if this is derived, we _cannot_ delete contained
                            // entities. Thus, we should not generate entity removal
                            // commands.
                            List priorEntities = entityList();
                            Iterator priors = priorEntities.iterator();

                            while (priors.hasNext()) {
                                ComponentEntity prior = (ComponentEntity) priors
                                                .next();

                                // If there is at least one more contained object,
                                // then delete this one.
                                // NOTE: How do we prevent the user from attempting to
                                // override the contained object in a subclass?
                                // It doesn't work to not remove this if the object
                                // is derived, because then derived objects won't
                                // track the prototype.
                                if (priors.hasNext()) {
                                    prior.setContainer(null);
                                } else {
                                    // The last entity in the entityList is
                                    // the one that we just added.
                                    entity = prior;
                                }
                            }

                            if (entity == null) {
                                // Nothing to do.
                                return;
                            }

                            // Set up the inside connections.
                            // First, create all the ports.
                            // The above execute() will have deleted all ports
                            // (in _removeEntity()).
                            int count = 1;
                            Iterator entityPorts = entity.portList().iterator();

                            while (entityPorts.hasNext()) {
                                ComponentPort insidePort = (ComponentPort) entityPorts
                                                .next();
                                String name = insidePort.getName();

                                // The outside port may already exist (e.g.
                                // as a consequence of cloning).
                                IteratePort newPort = (IteratePort) getPort(name);

                                if (newPort == null) {
                                    newPort = (IteratePort) newPort(name);
                                }

                                if (insidePort instanceof IOPort) {
                                    IOPort castPort = (IOPort) insidePort;
                                    newPort.setMultiport(castPort.isMultiport());
                                    newPort.setInput(castPort.isInput());
                                    newPort.setOutput(castPort.isOutput());
                                }

                                // Set up inside connections.
                                // Do this only if they are not already connected.
                                List connectedPorts = insidePort
                                                .connectedPortList();

                                if (!connectedPorts.contains(newPort)) {
                                    ComponentRelation relation = newRelation(uniqueName(
                                                "relation"));
                                    newPort.link(relation);
                                    insidePort.link(relation);
                                }
                            }
                        } finally {
                            workspace().doneWriting();
                        }
                    }
                }
            };

        requestChange(request);
    }

    /** Add a port to this actor. This overrides the base class to
     *  mirror the new port in the contained actor, if there is one,
     *  and to establish a connection to a port on the contained actor.
     *  @param port The TypedIOPort to add to this actor.
     *  @exception IllegalActionException If the port is not an instance
     *   of IteratePort, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    protected void _addPort(Port port)
        throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof IteratePort)) {
            throw new IllegalActionException(this,
                "IterateOverArray ports are required to be "
                + "instances of IteratePort");
        }

        super._addPort(port);

        // Create and connect a matching inside port on contained entities.
        // Do this as a change request to ensure that the action of
        // creating the port passed in as an argument is complete by
        // the time this executes.  Do not use MoML here because it
        // isn't necessary to generate any undo code.  _removePort()
        // takes care of the undo.
        final IteratePort castPort = (IteratePort) port;

        ChangeRequest request = new ChangeRequest(this,
                "Add a port on the inside") {
                // Override this to indicate that the change is localized.
                // This keeps the EntityTreeModel from closing open libraries
                // when notified of this change.
                public NamedObj getLocality() {
                    return IterateOverArray.this;
                }

                protected void _execute() throws Exception {
                    // NOTE: We defer the construction of the MoML
                    // change request to here because only at this
                    // point can we be sure that the change request
                    // that triggered this has completed.
                    synchronized (this) {
                        // Create and connect a matching inside port
                        // on contained entities.
                        // NOTE: We assume this propagates to derived
                        // objects because _addPort is called when
                        // MoML is parsed to add a port to
                        // IterateOverArray. Even the IterateComposite
                        // uses MoML to add this port, so this will
                        // result in propagation.
                        try {
                            workspace().getWriteAccess();
                            _inAddPort = true;

                            String portName = castPort.getName();
                            Iterator entities = entityList().iterator();

                            if (entities.hasNext()) {
                                Entity insideEntity = (Entity) entities.next();
                                Port insidePort = insideEntity.getPort(portName);

                                if (insidePort == null) {
                                    insidePort = insideEntity.newPort(portName);

                                    if (insidePort instanceof IOPort) {
                                        IOPort castInsidePort = (IOPort) insidePort;
                                        castInsidePort.setInput(castPort
                                                        .isInput());
                                        castInsidePort.setOutput(castPort
                                                        .isOutput());
                                        castInsidePort.setMultiport(castPort
                                                        .isMultiport());
                                    }
                                }

                                if (insidePort instanceof MirrorPort) {
                                    castPort.setAssociatedPort((MirrorPort) insidePort);
                                }

                                // Create a link only if it doesn't already exist.
                                List connectedPorts = insidePort
                                                .connectedPortList();

                                if (!connectedPorts.contains(castPort)) {
                                    // There is no connection. Create one.
                                    ComponentRelation newRelation = newRelation(uniqueName(
                                                "relation"));
                                    insidePort.link(newRelation);
                                    castPort.link(newRelation);
                                }
                            }
                        } finally {
                            workspace().doneWriting();
                            _inAddPort = false;
                        }
                    }
                }
            };

        requestChange(request);
    }

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
                    if ((sourcePort.getContainer() == this)
                                    && (destinationPort.getContainer() != this)) {
                        // The source port belongs to me, but not the
                        // destination.
                        Type srcElementType = ((ArrayType) srcDeclared)
                                        .getElementType();
                        compare = TypeLattice.compare(srcElementType,
                                destinationDeclared);
                    } else if ((sourcePort.getContainer() != this)
                                    && (destinationPort.getContainer() == this)) {
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

                    if ((compare == CPO.HIGHER)
                                    || (compare == CPO.INCOMPARABLE)) {
                        Inequality inequality = new Inequality(sourcePort
                                            .getTypeTerm(),
                                destinationPort.getTypeTerm());
                        result.add(inequality);
                    }
                }
            }
        }

        return result;
    }

    /** Override the base class to set a flag indicating that
     *  a clone is in progress. That flag is reset by the clone
     *  method prior to returning the constructed object.
     *  @param source The object from which this was cloned.
     */
    protected void _clonedFrom(NamedObj source) {
        _cloning = true;
    }

    /** Override the base class to describe contained entities,
     *  attributes, and ports, but not inside links or relations.
     *  The rest of the contents are generated automatically when a
     *  contained entity is inserted.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
        throws IOException {
        Iterator attributes = attributeList().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            attribute.exportMoML(output, depth);
        }

        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            Port port = (Port) ports.next();
            port.exportMoML(output, depth);
        }

        Iterator entities = entityList().iterator();

        while (entities.hasNext()) {
            ComponentEntity entity = (ComponentEntity) entities.next();
            entity.exportMoML(output, depth);
        }
    }

    /** Override the base class to remove the ports and inside relations
     *  of this actor. This method assumes the caller has write access
     *  on the workspace.
     *  @param entity The entity being removed from this entity.
     */
    protected void _removeEntity(ComponentEntity entity) {
        super._removeEntity(entity);

        // Remove all inside relations. This will have the
        // side effect of removing connections on the inside.
        Iterator relations = relationList().iterator();

        while (relations.hasNext()) {
            try {
                ((ComponentRelation) relations.next()).setContainer(null);
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        }

        // Have to copy the list to avoid a concurrent
        // modification exception.
        Iterator ports = (new LinkedList(portList())).iterator();

        while (ports.hasNext()) {
            Port port = (Port) ports.next();

            try {
                _inRemoveEntity = true;
                port.setContainer(null);
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            } finally {
                _inRemoveEntity = false;
            }
        }
    }

    /** Override the base class to remove the associated port on the
     *  inside entity and the link to it, if there is one.
     *  This method assumes the caller has write access on the
     *  workspace.
     *  @param port The port being removed from this entity.
     */
    protected void _removePort(final Port port) {
        super._removePort(port);

        // NOTE: Do not use MoML here because we do not want to generate
        // undo actions to recreate the inside relation and port.
        // This is because _addPort() will take care of that.
        // The cast is safe because all my ports are instances of IOPort.
        Iterator relations = ((IOPort) port).insideRelationList().iterator();

        while (relations.hasNext()) {
            ComponentRelation relation = (ComponentRelation) relations.next();

            try {
                relation.setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException(ex);
            }
        }

        // Remove the ports from the inside entity only if this
        // is not being called as a side effect of calling _removeEntity().
        if (_inRemoveEntity) {
            return;
        }

        Iterator entities = entityList().iterator();

        while (entities.hasNext()) {
            Entity insideEntity = (Entity) entities.next();
            Port insidePort = insideEntity.getPort(port.getName());

            if (insidePort != null) {
                try {
                    insidePort.setContainer(null);
                } catch (KernelException ex) {
                    throw new InternalErrorException(ex);
                }
            }
        }
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
     *  @param destinationPortList The destination port list.
     *  @return A list of instances of Inequality.
     */
    protected List _typeConstraintsFromTo(TypedIOPort sourcePort,
        List destinationPortList) {
        List result = new LinkedList();

        boolean srcUndeclared = sourcePort.getTypeTerm().isSettable();
        Iterator destinationPorts = destinationPortList.iterator();

        while (destinationPorts.hasNext()) {
            TypedIOPort destinationPort = (TypedIOPort) destinationPorts.next();
            boolean destUndeclared = destinationPort.getTypeTerm().isSettable();

            if (srcUndeclared || destUndeclared) {
                // At least one of the source/destination ports does
                // not have declared type, form type constraint.
                if ((sourcePort.getContainer() == this)
                                && (destinationPort.getContainer() == this)) {
                    // Both ports belong to this, so their type must be equal.
                    // Represent this with two inequalities.
                    Inequality ineq1 = new Inequality(sourcePort.getTypeTerm(),
                            destinationPort.getTypeTerm());
                    result.add(ineq1);

                    Inequality ineq2 = new Inequality(destinationPort
                                        .getTypeTerm(), sourcePort.getTypeTerm());
                    result.add(ineq2);
                } else if (sourcePort.getContainer().equals(this)) {
                    if (sourcePort.sourcePortList().size() == 0) {
                        // Skip this port. It is not connected on the outside.
                        continue;
                    }

                    Type sourcePortType = sourcePort.getType();

                    if (!(sourcePortType instanceof ArrayType)) {
                        throw new InternalErrorException(
                            "Source port was expected to be an array type: "
                            + sourcePort.getFullName() + ", but it had type: "
                            + sourcePortType);
                    }

                    InequalityTerm elementTerm = ((ArrayType) sourcePortType)
                                    .getElementTypeTerm();
                    Inequality ineq = new Inequality(elementTerm,
                            destinationPort.getTypeTerm());
                    result.add(ineq);
                } else if (destinationPort.getContainer().equals(this)) {
                    Type destinationPortType = destinationPort.getType();

                    if (!(destinationPortType instanceof ArrayType)) {
                        throw new InternalErrorException(
                            "Destination port was expected to be an array type: "
                            + destinationPort.getFullName()
                            + ", but it had type: " + destinationPortType);
                    }

                    InequalityTerm elementTerm = ((ArrayType) destinationPortType)
                                    .getElementTypeTerm();
                    Inequality ineq = new Inequality(sourcePort.getTypeTerm(),
                            elementTerm);
                    result.add(ineq);
                }
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Flag indicating that cloning is in progress.
    private boolean _cloning = false;

    // Flag indicating that we are executing _addPort().
    private boolean _inAddPort = false;

    // Flag indicating that we are executing _removeEntity().
    private boolean _inRemoveEntity = false;

    // Variable that reflects the current iteration count on the
    // inside.
    private Variable _iterationCount;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// IterateComposite

    /** This is a specialized composite actor for use in IterateOverArray.
     *  In particular, it ensures that if ports are added or deleted
     *  locally, then corresponding ports will be added or deleted
     *  in the container.  That addition will result in appropriate
     *  connections being made.
     */
    public static class IterateComposite extends TypedCompositeActor {
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
        public Port newPort(String name) throws NameDuplicationException {
            try {
                return new IteratePort(this, name);
            } catch (IllegalActionException ex) {
                // This exception should not occur, so we throw a runtime
                // exception.
                throw new InternalErrorException(this, ex, null);
            }
        }

        /** Add a port to this actor. This overrides the base class to
         *  add a corresponding port to the container using a change
         *  request, if that port does not already exist.
         *  @param port The TypedIOPort to add to this actor.
         *  @exception IllegalActionException If the port is not an instance of
         *   MirrorPort, or the port has no name.
         *  @exception NameDuplicationException If the port name
         *  collides with a name already in the actor.
         */
        protected void _addPort(final Port port)
            throws IllegalActionException, NameDuplicationException {
            if (!(port instanceof MirrorPort)) {
                throw new IllegalActionException(this,
                    "Ports in IterateOverArray$IterateComposite must be MirrorPort.");
            }

            super._addPort(port);

            final IterateOverArray container = (IterateOverArray) getContainer();

            if (container._inAddPort) {
                return;
            }

            // Use a change request so we can be sure the port
            // being added is fully constructed.
            ChangeRequest request = new ChangeRequest(this,
                    "Add mirror port to the container.") {
                    // Override this to indicate that the change is localized.
                    // This keeps the EntityTreeModel from closing open libraries
                    // when notified of this change.
                    public NamedObj getLocality() {
                        return getContainer();
                    }

                    protected void _execute() throws Exception {
                        try {
                            workspace().getWriteAccess();

                            // The port may already exist (if we are
                            // inside a clone() call).
                            IteratePort newPort = (IteratePort) container
                                            .getPort(port.getName());

                            if (newPort == null) {
                                newPort = (IteratePort) container.newPort(port
                                                    .getName());
                            }

                            if (port instanceof IOPort) {
                                newPort.setInput(((IOPort) port).isInput());
                                newPort.setOutput(((IOPort) port).isOutput());
                                newPort.setMultiport(((IOPort) port)
                                                .isMultiport());
                            }
                        } finally {
                            workspace().doneWriting();
                        }
                    }
                };

            container.requestChange(request);
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
        /** Create a new instance of the director for IterateOverArray.
         *  @param container The container for the director.
         *  @param name The name of the director.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @exception NameDuplicationException Not thrown in this base class.
         */
        public IterateDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
            super(container, name);
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
        public void fire() throws IllegalActionException {
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

                int result = Actor.COMPLETED;
                int iterationCount = 0;

                while (result != Actor.NOT_READY) {
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

                    if (result == Actor.STOP_ITERATING) {
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

        /** Delegate by calling fireAt() on the director of the container's
         *  container.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         */
        public void fireAt(Actor actor, Time time)
            throws IllegalActionException {
            Director director = IterateOverArray.this.getExecutiveDirector();

            if (director != null) {
                director.fireAt(actor, time);
            }
        }

        /** Delegate by calling fireAtCurrentTime() on the director
         *  of the container's container.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         */
        public void fireAtCurrentTime(Actor actor)
            throws IllegalActionException {
            Director director = IterateOverArray.this.getExecutiveDirector();

            if (director != null) {
                director.fireAtCurrentTime(actor);
            }
        }

        /** Return a new instance of QueueReceiver.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        public Receiver newReceiver() {
            return new QueueReceiver();
        }

        /** Override the base class to return the logical AND of
         *  what the base class postfire() method returns and the
         *  flag set in fire().  As a result, this will return
         *  false if any contained actor returned false in its
         *  postfire() method.
         */
        public boolean postfire() throws IllegalActionException {
            boolean superReturns = super.postfire();
            return (superReturns && _postfireReturns);
        }

        /** Transfer data from an input port of the
         *  container to the ports it is connected to on the inside.
         *  This method extracts tokens from the input array and
         *  provides them sequentially to the corresponding ports
         *  of the contained actor.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         */
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
                                _debug(getName(),
                                    "transferring input from " + port.getName());
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
         *  @exception IllegalActionException Not thrown in this base class.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @see IOPort#transferOutputs
         */
        public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
            boolean result = false;

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

                        port.send(i, new ArrayToken(tokens));
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
         *  type rather than to the type of the port.
         *  @param token The token to convert.
         *  @return The converted token.
         *  @exception IllegalActionException If the conversion is
         *   invalid.
         */
        public Token convert(Token token) throws IllegalActionException {
            if (!(getContainer() instanceof IterateOverArray) || !isOutput()) {
                return super.convert(token);
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

            try {
                try {
                    _workspace.getReadAccess();

                    ArrayType type = (ArrayType) getType();
                    int compare = TypeLattice.compare(token.getType(),
                            type.getElementType());

                    if ((compare == CPO.HIGHER)
                                    || (compare == CPO.INCOMPARABLE)) {
                        throw new IllegalActionException(
                            "Run-time type checking failed. Token type: "
                            + token.getType().toString() + ", port: "
                            + getFullName() + ", port type: "
                            + getType().toString());
                    }

                    // Note that the getRemoteReceivers() method doesn't throw
                    // any non-runtime exception.
                    farReceivers = deepGetReceivers();

                    if ((farReceivers == null)
                                    || (farReceivers[channelIndex] == null)) {
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
            }
        }
    }
}
