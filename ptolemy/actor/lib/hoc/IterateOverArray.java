/* An actor that iterates a contained actor over input arrays.

 Copyright (c) 2003 The Regents of the University of California.
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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
 */

package ptolemy.actor.lib.hoc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// IterateOverArray
/**
This actor iterates the contained actor over input arrays.
Each input port expects an array. When this actor fires,
an array is read on each input port that has one, and its
contents are provided sequentially to the contained actor
with connected ports.  This actor then iterates the
contained actor until either there are no more
input data for the actor or the prefire() method of the actor
returns false. If postfire() of the actor returns false,
then postfire() of this actor will return false, requesting
a halt to execution of the model.  The outputs from the
contained actor are collected into arrays that are
produced on the outputs of this actor.
<p>
To make this actor easier to use, it reacts to a "drop"
of an actor on it by replicating the ports and parameters
of that actor. The dropped actor can, of course, be a
composite actor. When you interactively add ports or change
the properties of ports of this actor (whether a port is an
input or output, for example), then the same changes are
mirrored in the contained actor.
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
FIXME: There should be an option to reset between
firings of the inside actor.

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.1
*/
public class IterateOverArray extends TypedCompositeActor {

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

     *
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
        getMoMLInfo().className = "ptolemy.actor.lib.hoc.IterateOverArray";
        new IterateDirector(this, uniqueName("IterateDirector"));

        _iterationCount =
              new Variable(this, "iterationCount", new IntToken(0));
        _iterationCount.setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-6\" y=\"10\""
                + "style=\"font-size:24\">?</text>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to return a specialized port and to
     *  create a port on the inside entity, if there is one.
     *  @param name The name of the port to create.
     *  @return A new instance of IteratePort, an inner class.
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
    
    /** Override the base class to ensure that the ports of this
     *  actor all have array types.
     *  @return A list of instances of Inequality.
     *  @exception IllegalActionException If the typeConstraintList
     *  of one of the deeply contained objects throws it.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() throws IllegalActionException {
        Iterator ports = portList().iterator();
        while(ports.hasNext()) {
            TypedIOPort port = (TypedIOPort)ports.next();
            ArrayType arrayType = new ArrayType(BaseType.UNKNOWN);
            ((TypedIOPort)port).setTypeEquals(arrayType);
        }
        return super.typeConstraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to queue a change request to mirror
     *  the ports of the added entity.
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
            "Cannot place a class definition in an IterateOverArray actor.");
        }
        super._addEntity(entity);
        
        // This needs to be a MoMLChangeRequest so that undo works.
        // Alternatively, we could explicitly create the undo request,
        // but then we would also have to create the redo.
        // Use the container as a context so that redraw occurs after
        // the change request is executed.
        NamedObj tmpContext = MoMLChangeRequest
                .getDeferredToParent((NamedObj)getContainer());
        if (tmpContext == null) {
            tmpContext = ((NamedObj)getContainer());
        }
        final NamedObj context = tmpContext;
        MoMLChangeRequest request = new MoMLChangeRequest(
                this,
                context,
                "Adjust ports and parameters") {
            protected void _execute() throws Exception {
                // NOTE: We defer the construction of the MoML change request
                // to here because only at this point can we be sure that the
                // change request that triggered this has completed.

                synchronized(this) {

                    StringBuffer command = new StringBuffer(
                            "<entity name=\""
                            + getName(context)
                            + "\">\n");

                    // Entity most recently added.
                    ComponentEntity entity = null;

                    // Delete any previously contained entities.
                    Iterator priors = entityList().iterator();
                    LinkedList deletedEntities = new LinkedList();
                    while (priors.hasNext()) {
                        ComponentEntity prior = (ComponentEntity)priors.next();
                        // If there is at least one more contained object,
                        // then delete this one.
                        if (priors.hasNext()) {
                            command.append("<deleteEntity name=\""
                                    + prior.getName() + "\"/>\n");
                            deletedEntities.add(prior);
                        } else {
                            entity = prior;
                        }
                    }

                    if (entity == null) {
                        // Nothing to do.
                        return;
                    }

                    // Remove all inside relations. This will have the
                    // side effect of removing connections on the inside.
                    Iterator relations = relationList().iterator();
                    while(relations.hasNext()) {
                        command.append("<deleteRelation name=\""
                                + ((NamedObj)relations.next()).getName()
                                + "\"/>\n");
                    }

                    // Add commands to delete ports.
                    Iterator ports = portList().iterator();
                    while (ports.hasNext()) {
                        Port port = (Port)ports.next();
                        // Only delete ports whose names don't match the
                        // current entity. This preserves connections to
                        // ports with the same name.
                        
                        // NOTE: then if I add ports to an empty
                        // instance (no inside entity), then these ports
                        // don't go away if I drop in an entity.  Maybe
                        // this is OK?
                        
                        // NOTE: When reading a MoML file (vs. processing
                        // a change request), the ports of the entity have
                        // not been created because the entity has not been
                        // fully constructed when this _addEntity() method
                        // is called.  Thus, it is important to only delete
                        // ports that match something in a deleted entity.
                        if (entity.getPort(port.getName()) == null) {
                            Iterator deleted = deletedEntities.iterator();
                            while (deleted.hasNext()) {
                                ComponentEntity deletedEntity = (ComponentEntity)deleted.next();
                                if (deletedEntity.getPort(port.getName()) != null) {
                                    // FIXME: Must explicitly delete relations linked
                                    // to these ports, or undo won't work properly.
                                    // This is a bit of a pain, since we have to pop
                                    // out of this context to do it.
                                    command.append("<deletePort name=\"" + port.getName() + "\"/>\n");
                                    break;
                                }
                            }
                        }
                    }

                    // Set up the inside connections.
                    int count = 1;
                    Iterator entityPorts = entity.portList().iterator();
                    while (entityPorts.hasNext()) {
                        Port insidePort = (Port)entityPorts.next();

                        String name = insidePort.getName();

                        // If there isn't already a port with this name,
                        // the MoML parser will create one.
                        // Do not specify a class so that the MoMLParser
                        // uses newPort() to create it; newPort() will
                        // take care of the connections with the inside
                        // port.
                        command.append("<port name=\"" + name + "\">");
                        if (insidePort instanceof IOPort) {
                            IOPort castPort = (IOPort)insidePort;
                            command.append(
                                    "<property name=\"multiport\" value=\""
                                    + castPort.isMultiport()
                                    + "\"/>"
                                    + "<property name=\"input\" value=\""
                                    + castPort.isInput()
                                    + "\"/>"
                                    + "<property name=\"output\" value=\""
                                    + castPort.isOutput()
                                    + "\"/>");
                        }
                        command.append("</port>\n");

                        // Set up inside connections. Note that if the outside
                        // port was preserved from before, then it will have
                        // lost its inside links when the inside relation was
                        // deleted. Thus, we need to recreate them.
                        // Presumably, there are no inside relations now, so
                        // we can use any suitable names.

                        String relationName = "insideRelation" + count++;
                        command.append("<relation name=\"" + relationName + "\"/>\n");

                        command.append("<link port=\"" + name + "\" relation=\""
                                + relationName + "\"/>\n");

                        command.append("<link port=\""
                                + insidePort.getName(IterateOverArray.this)
                                + "\" relation=\""
                                + relationName
                                + "\"/>\n");
                    }

                    command.append("</entity>\n");

                    // The MoML command is the description of the change request.
                    setDescription(command.toString());

                    // Uncomment the following to see the (rather complicated)
                    // MoML command that is issued.
                    // System.out.println("--------in _addEntity() -------------------");
                    // System.out.println(command.toString());
                    // System.out.println("--------- context: " + context.getFullName());
                    // System.out.println("-------------------------------------------");

                    try {
                        // Disable reactions to added ports.
                        _inAddEntity = true;
                        super._execute();
                    } finally {
                        _inAddEntity = false;
                    }
                }
            }
        };
        request.setUndoable(true);
        // Do this so that a single undo reverses the entire operation.
        request.setMergeWithPreviousUndo(true);

        requestChange(request);
    }

    /** Add a port to this actor. This overrides the base class to
     *  mirror the new port in the contained actor, if there is one.
     *  @param port The TypedIOPort to add to this actor.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this actor, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    protected void _addPort(final Port port)
            throws IllegalActionException, NameDuplicationException {
        super._addPort(port);
        if (!_inAddEntity) {
            // Create and connect a matching inside port on contained entities.
            // Do this as a change request to ensure that the action of
            // creating the port passed in as an argument is complete by
            // the time this executes.  Do not use MoML here because it
            // isn't necessary to generate any undo code.  _removePort()
            // takes care of the undo.
            ChangeRequest request = new ChangeRequest(
                    this,
                    "Add a port on the inside") {
                protected void _execute() throws Exception {
                    // NOTE: We defer the construction of the MoML change request
                    // to here because only at this point can we be sure that the
                    // change request that triggered this has completed.

                    synchronized(this) {
                        // Create and connect a matching inside port on contained entities.
                        String portName = port.getName();
                        Iterator entities = entityList().iterator();
                        if (entities.hasNext()) {
                            Entity insideEntity = (Entity)entities.next();
                            Port insidePort = insideEntity.getPort(portName);
                            boolean createdInsidePort = false;
                            if (insidePort == null) {
                                insidePort = insideEntity.newPort(portName);
                                if (port instanceof IOPort
                                        && insidePort instanceof IOPort) {
                                    IOPort castInsidePort = (IOPort)insidePort;
                                    IOPort castPort = (IOPort)port;
                                    castInsidePort.setInput(castPort.isInput());
                                    castInsidePort.setOutput(castPort.isOutput());
                                    castInsidePort.setMultiport(castPort.isMultiport());
                                }
                            }
                            if (port instanceof IteratePort) {
                                ((IteratePort)port).associatePort(insidePort);
                            }
                            // Create a link only if it doesn't already exist.
                            List connectedPorts = insidePort.connectedPortList();
                            ComponentRelation newRelation = null;
                            if (!connectedPorts.contains(port)) {
                                // There is no connection. Create one.
                                newRelation = newRelation(uniqueName("relation"));
                                insidePort.link(newRelation);
                                port.link(newRelation);
                            }
                        }
                    }
                }
            };
            requestChange(request);
        }
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
                TypedIOPort destinationPort =
                    (TypedIOPort)destinationPorts.next();
                isUndeclared = destinationPort.getTypeTerm().isSettable();

                if (!isUndeclared) {
                    // both source/destination ports are declared,
                    // check type
                    Type destinationDeclared = destinationPort.getType();

                    int compare;
                    // If the source port belongs to me, then we want to
                    // compare its array element type to the type of the
                    // destination.
                    if(sourcePort.getContainer() == this
                            && destinationPort.getContainer() != this) {

                        // The source port belongs to me, but not the
                        // destination.

                        Type srcElementType =
                            ((ArrayType)srcDeclared).getElementType();
                        compare = TypeLattice.compare(srcElementType,
                                destinationDeclared);
                    } else if(sourcePort.getContainer() != this
                            && destinationPort.getContainer() == this) {

                        // The destination port belongs to me, but not
                        // the source.

                        Type destinationElementType
                            = ((ArrayType)destinationDeclared).getElementType();
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

    /** Override the base class to remove the associated port on the
     *  inside entity and the link to it, if there is one.
     *  @param port The port being removed from this entity.
     */
    protected void _removePort(final Port port) {
        super._removePort(port);
        
        // NOTE: Do not use MoML here because we do not want to generate
        // undo actions to recreate the inside relation and port.
        // This is because _addPort() will take care of that.
        
        // The cast is safe because all my ports are instances of IOPort.
        Iterator relations = ((IOPort)port).insideRelationList().iterator();
        while (relations.hasNext()) {
            ComponentRelation relation = (ComponentRelation)relations.next();
            try {
                relation.setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException(ex);
            }
        }

        Iterator entities = entityList().iterator();
        while (entities.hasNext()) {
            Entity insideEntity = (Entity)entities.next();
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
            TypedIOPort destinationPort = (TypedIOPort)destinationPorts.next();
            boolean destUndeclared =
                    destinationPort.getTypeTerm().isSettable();

            if (srcUndeclared || destUndeclared) {
                // At least one of the source/destination ports does
                // not have declared type, form type constraint.
                if(sourcePort.getContainer() == this &&
                        destinationPort.getContainer() == this) {
                    // Both ports belong to this, so their type must be equal.
                    // Represent this with two inequalities.
                    Inequality ineq1 = new Inequality(sourcePort.getTypeTerm(),
                            destinationPort.getTypeTerm());
                    result.add(ineq1);
                    Inequality ineq2 =
                        new Inequality(destinationPort.getTypeTerm(),
                                sourcePort.getTypeTerm());
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
                        + sourcePort.getFullName()
                        + ", but it had type: "
                        + sourcePortType);
                    }
                    InequalityTerm elementTerm =
                        ((ArrayType)sourcePortType).getElementTypeTerm();
                    Inequality ineq = new Inequality(elementTerm,
                            destinationPort.getTypeTerm());
                    result.add(ineq);
                } else if (destinationPort.getContainer().equals(this)) {
                    Type destinationPortType = destinationPort.getType();
                    if (!(destinationPortType instanceof ArrayType)) {
                        throw new InternalErrorException(
                        "Destination port was expected to be an array type: "
                        + destinationPort.getFullName()
                        + ", but it had type: "
                        + destinationPortType);
                    }
                    InequalityTerm elementTerm =
                        ((ArrayType)destinationPortType).getElementTypeTerm();
                    Inequality ineq =
                        new Inequality(sourcePort.getTypeTerm(), elementTerm);
                    result.add(ineq);
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag indicating that we are executing _addEntity().
    private boolean _inAddEntity = false;
    
    // Variable that reflects the current iteration count on the
    // inside.
    private Variable _iterationCount;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

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
         *  of the contained actor throws it.
         */
        public void fire() throws IllegalActionException {
            CompositeActor container = (CompositeActor)getContainer();
            Iterator actors = container.entityList().iterator();
            _postfireReturns = true;
            while (actors.hasNext() && !_stopRequested) {

                Actor actor = (Actor)actors.next();
                int result = Actor.COMPLETED;
                int iterationCount = 0;
                while (result != Actor.NOT_READY) {
                    iterationCount++;
                    _iterationCount.setToken(new IntToken(iterationCount));
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.BEFORE_ITERATE,
                                iterationCount));
                    }
                    result = actor.iterate(1);
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_ITERATE,
                                iterationCount));
                    }

                    // Should return if there is no more input data,
                    // irrespective of return value of prefire() of
                    // the actor, which is not reliable.

                    boolean outOfData = true;
                    Iterator inPorts = actor.inputPortList().iterator();
                    while (inPorts.hasNext()) {
                        IOPort port = (IOPort)inPorts.next();
                        for(int i = 0; i < port.getWidth(); i++) {
                            if (port.hasToken(i)) {
                                outOfData = false;
                                break;
                            }
                        }
                    }
                    if (outOfData) {
                        if (_debugging) {
                            _debug("No more input data for: "
                                    + ((Nameable)actor).getFullName());
                        }
                        break;
                    }
                    if (result == Actor.STOP_ITERATING) {
                        if (_debugging) {
                            _debug("Actor requests halt: "
                                    + ((Nameable)actor).getFullName());
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
        public void fireAt(Actor actor, double time)
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
                                        "transferring input from "
                                        + port.getName());
                            }
                            ArrayToken arrayToken = (ArrayToken)t;
                            for(int j = 0; j < arrayToken.length(); j++) {
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
                    while(port.isKnownInside(i) && port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        list.add(t);
                    }
                    if(list.size() != 0) {
                        Token[] tokens =
                            (Token[])list.toArray(new Token[list.size()]);
                        if (_debugging) {
                            _debug(getName(),
                                    "transferring output to "
                                    + port.getName());
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

    /** This is a specialized port for handling type conversions between
     *  the array types of the ports of the enclosing IterateOverArray
     *  actor and the scalar types (or arrays with one less dimension)
     *  of the actor that are contained.
     */
    public static class IteratePort extends TypedIOPort {

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
        public IteratePort(IterateOverArray container, String name)
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

        /** Specify an associated inside port.  Once this is specified,
         *  then any changes made to this port (its name, whether it
         *  is an input or output, and whether it is a multiport) are
         *  mirrored in the associated port.
         */
        public void associatePort(Port port) {
            _associatedPort = port;
        }

        /** Override the base class to convert the token to the element
         *  type rather than to the type of the port.
         *  @param token The token to convert.
         *  @exception IllegalActionException If the conversion is
         *   invalid.
         */
        public Token convert(Token token) throws IllegalActionException {
            // If this port is an output port, then we assume the data
            // is being sent from the inside, and hence needs to be converted.
            if (isOutput()) {
                Type type = ((ArrayType)getType()).getElementType();
                if (type.equals(token.getType())) {
                    return token;
                } else {
                    Token newToken = type.convert(token);
                    return newToken;
                }
            } else {
                return super.convert(token);
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
            Receiver[][] farReceivers;
            if (_debugging) {
                _debug("send inside to channel " + channelIndex + ": "
                        + token);
            }
            try {
                try {
                    _workspace.getReadAccess();
                    ArrayType type = (ArrayType)getType();
                    int compare = TypeLattice.compare(token.getType(),
                            type.getElementType());
                    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                        throw new IllegalActionException(
                                "Run-time type checking failed. Token type: "
                                + token.getType().toString() + ", port: "
                                + getFullName() + ", port type: "
                                + getType().toString());
                    }

                    // Note that the getRemoteReceivers() method doesn't throw
                    // any non-runtime exception.
                    farReceivers = deepGetReceivers();
                    if (farReceivers == null ||
                            farReceivers[channelIndex] == null) return;
                } finally {
                    _workspace.doneReading();
                }
                for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                    TypedIOPort port =
                        (TypedIOPort)farReceivers[channelIndex][j]
                        .getContainer();
                    Token newToken = port.convert(token);
                    farReceivers[channelIndex][j].put(newToken);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // NOTE: This may occur if the channel index is out of range.
                // This is allowed, just do nothing.
            }
        }

        /** Override the base class to also set the associated port,
         *  if there is one.
         *  @exception IllegalActionException If changing the port status is
         *   not permitted (for example, the port status is fixed by a class
         *   definition).
         */
        public void setInput(boolean isInput) throws IllegalActionException {
            super.setInput(isInput);
            if (_associatedPort instanceof IOPort) {
                ((IOPort)_associatedPort).setInput(isInput);
            }
        }

        /** Override the base class to also set the associated port,
         *  if there is one.
         *  @exception IllegalActionException If changing the port status is
         *   not permitted (for example, the port status is fixed by a class
         *   definition).
         */
        public void setMultiport(boolean isMultiport) throws IllegalActionException {
            super.setMultiport(isMultiport);
            if (_associatedPort instanceof IOPort) {
                ((IOPort)_associatedPort).setMultiport(isMultiport);
            }
        }

        /** Override the base class to also set the associated port,
         *  if there is one.
         */
        public void setName(String name)
                throws IllegalActionException, NameDuplicationException {
            super.setName(name);
            if (_associatedPort != null) {
                _associatedPort.setName(name);
            }
        }

        /** Override the base class to also set the associated port,
         *  if there is one.
         *  @exception IllegalActionException If changing the port status is
         *   not permitted (for example, the port status is fixed by a class
         *   definition).
         */
        public void setOutput(boolean isOutput) throws IllegalActionException {
            super.setOutput(isOutput);
            if (_associatedPort instanceof IOPort) {
                ((IOPort)_associatedPort).setOutput(isOutput);
            }
        }

        // The associated port, if there is one.
        private Port _associatedPort = null;
    }
}
