/*  A composite that contain one actor and mirror the ports and parameters of that actor.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.MirrorPortParameter;
import ptolemy.actor.parameters.ParameterMirrorPort;
import ptolemy.actor.parameters.PortParameter;
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
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.HandlesInternalLinks;

///////////////////////////////////////////////////////////////////
//// ReflectComposite

/**
 A composite that contains one actor and mirrors the ports and
 parameters of that actor. In this base class, ports that are
 not instances of MirrorPort are not mirrored. The subclass
 MirrorComposite will mirror those ports.

 @author Ilge Akkaya and Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class ReflectComposite extends TypedCompositeActor implements
        HandlesInternalLinks {

    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ReflectComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init(true);
    }

    /** Construct a ReflectComposite in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public ReflectComposite(Workspace workspace) {
        super(workspace);
        _init(true);
    }

    /** Create an actor with a name and a container that optionally
     *  mirrors the ports that are instances of ParameterPort.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @param mirrorParameterPorts If false, then ports that are instances of
     *   ParameterPort are not mirrored.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ReflectComposite(CompositeEntity container, String name,
            boolean mirrorParameterPorts) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        _init(mirrorParameterPorts);
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ReflectComposite result = (ReflectComposite) super.clone(workspace);

        // Fix port associations.
        Iterator entities = result.entityList().iterator();

        while (entities.hasNext()) {
            Entity insideEntity = (Entity) entities.next();
            Iterator ports = result.portList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                Port insidePort = insideEntity.getPort(port.getName());

                if (insidePort instanceof MirrorPort) {
                    ((MirrorPort) port)
                            .setAssociatedPort((MirrorPort) insidePort);
                } else if (insidePort instanceof ParameterMirrorPort) {
                    ((ParameterMirrorPort) port)
                            .setAssociatedPort((ParameterMirrorPort) insidePort);
                }
            }
        }
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
            Port result = new MirrorPort(this, name);

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

    /** Create a new ParameterMirrorPort.
     *  @param name The name of the port to create.
     *  @return A new instance of PtidesMirrorPort, an inner class.
     *  @exception NameDuplicationException If the container already has a port
     *  with this name.
     */
    public Port newParameterPort(String name) throws NameDuplicationException {
        try {
            PortParameter parameter = new MirrorPortParameter(this, name);
            return parameter.getPort();
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        }
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
    @Override
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (entity.isClassDefinition()) {
            throw new IllegalActionException(this,
                    "Cannot place a class definition in an "
                            + "ReflectComposite actor.");
        }

        super._addEntity(entity);

        // Issue a change request to add the appropriate
        // ports and connections to the new entity.
        ChangeRequest request = new ChangeRequest(this, // originator
                "Adjust contained entities, ports and parameters") {
            // Override this to indicate that the change is localized.
            // This keeps the EntityTreeModel from closing open libraries
            // when notified of this change.
            @Override
            public NamedObj getLocality() {
                return ReflectComposite.this;
            }

            @Override
            protected void _execute() throws Exception {
                // NOTE: We defer to a change request
                // because only at this point can we be sure that the
                // change request that triggered this has completed (i.e. that
                // the entity being added has been added.
                synchronized (ReflectComposite.this) {
                    try {
                        workspace().getWriteAccess();

                        // Entity most recently added.
                        ComponentEntity entity = null;

                        // Delete any previously contained entities.
                        // The strategy here is a bit tricky if this ReflectComposite
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

                        Iterator entityPorts = entity.portList().iterator();

                        while (entityPorts.hasNext()) {
                            ComponentPort insidePort = (ComponentPort) entityPorts
                                    .next();
                            // Use a strategy pattern here so that subclasses can control
                            // which ports are mirrored.
                            if (!_mirrorPort(insidePort)) {
                        	continue;
                            }
                            String name = insidePort.getName();

                            // The outside port may already exist (e.g.
                            // as a consequence of cloning).
                            IOPort newPort = (IOPort) getPort(name);

                            if (newPort == null) {
                                newPort = (IOPort) newPort(name);
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
                                ComponentRelation relation = newRelation(uniqueName("relation"));
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
     *  mirror the new port, only if it is an instance of MirrorPort
     *  in the contained actor, if there is one,
     *  and to establish a connection to a port on the contained actor.
     *  @param port The TypedIOPort to add to this actor.
     *  @exception IllegalActionException If the port is not an instance
     *   of IteratePort, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    @Override
    protected void _addPort(Port port) throws IllegalActionException,
            NameDuplicationException {

        super._addPort(port);

        if ((port instanceof MirrorPort || port instanceof ParameterMirrorPort)) {
            // Create and connect a matching inside port on contained entities.
            // Do this as a change request to ensure that the action of
            // creating the port passed in as an argument is complete by
            // the time this executes.  Do not use MoML here because it
            // isn't necessary to generate any undo code.  _removePort()
            // takes care of the undo.
            final IOPort castPort = (IOPort) port;

            ChangeRequest request = new ChangeRequest(this,
                    "Add a port on the inside") {
                // Override this to indicate that the change is localized.
                // This keeps the EntityTreeModel from closing open libraries
                // when notified of this change.
                @Override
                public NamedObj getLocality() {
                    return ReflectComposite.this;
                }

                @Override
                protected void _execute() throws Exception {
                    // NOTE: We defer the construction of the MoML
                    // change request to here because only at this
                    // point can we be sure that the change request
                    // that triggered this has completed.
                    synchronized (ReflectComposite.this) {
                        // Create and connect a matching inside port
                        // on contained entities.
                        // NOTE: We assume this propagates to derived
                        // objects because _addPort is called when
                        // MoML is parsed to add a port to
                        // MirrorComposite. Even the MirrorCompositeContents
                        // uses MoML to add this port, so this will
                        // result in propagation.
                        try {
                            workspace().getWriteAccess();
                            _inAddPort = true;

                            String portName = castPort.getName();
                            Iterator entities = entityList().iterator();

                            if (entities.hasNext()) {
                                Entity insideEntity = (Entity) entities.next();
                                Port insidePort = insideEntity
                                        .getPort(portName);

                                if (insidePort == null) {
                                    if (castPort instanceof MirrorPort) {
                                        insidePort = insideEntity
                                                .newPort(portName);
                                    } else if (castPort instanceof ParameterMirrorPort) { // ParameterMirrorPort
                                        insidePort = ((MirrorComposite) insideEntity)
                                                .newParameterPort(portName);
                                    }

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

                                if (insidePort == null) {
                                    // FindBugs was reporting that insidePort could still be null.
                                    throw new InternalErrorException(
                                            ReflectComposite.this,
                                            null,
                                            "insidePort is null? castPort "
                                                    + castPort
                                                    + " is neither a MirrorPort nor a ParameterMirrorPort?");
                                }

                                if (insidePort instanceof MirrorPort) {
                                    ((MirrorPort) castPort)
                                    .setAssociatedPort((MirrorPort) insidePort);
                                } else if (insidePort instanceof ParameterMirrorPort) { // ParameterMirrorPort
                                    ((ParameterMirrorPort) castPort)
                                    .setAssociatedPort((ParameterMirrorPort) insidePort);
                                }

                                // Create a link only if it doesn't already exist.
                                List connectedPorts = insidePort
                                        .connectedPortList();

                                // Check if inside port is already connected to a port with that name.
                                // Skipping this step causes duplicate link attempts between castPort and insidePort
                                // in the case that _addPort() is called during clone(), in which CompositeEntity.clone()
                                // will already have created a link between the two ports.

                                Iterator connectedPortsIterator = connectedPorts
                                        .iterator();
                                boolean alreadyConnected = false;
                                while (connectedPortsIterator.hasNext()) {
                                    Port cp = (Port) connectedPortsIterator
                                            .next();
                                    if (cp.getName().equals(portName)) {
                                        // do not connect
                                        alreadyConnected = true;
                                    }
                                }
                                if (!alreadyConnected) {
                                    // There is no connection. Create one.
                                    ComponentRelation newRelation = newRelation(uniqueName("relation"));
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
    }

    /** Override the base class to describe contained entities,
     *  attributes, and ports, but not inside links or relations.
     *  The rest of the contents are generated automatically when a
     *  contained entity is inserted.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
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
    
    /** Return true if the specified inside port should be mirrored.
     *  This base class returns true if the inside port is an instance
     *  of MirrorPort.
     *  @param insidePort The port that may be mirrored.
     *  @return True if the inside port should be mirrored.
     */
    protected boolean _mirrorPort(ComponentPort insidePort) {
        // do not mirror ports that are not instances of MirrorPort
        if (insidePort instanceof MirrorPort) {
            return true;
        }
        return false;
    }

    /** Override the base class to remove the ports and inside relations
     *  of this actor. This method assumes the caller has write access
     *  on the workspace.
     *  @param entity The entity being removed from this entity.
     */
    @Override
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
        Iterator ports = new LinkedList(portList()).iterator();

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
    @Override
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag indicating that we are executing _addPort(). */
    protected boolean _inAddPort = false;

    /** Flag indicating that we are executing _removeEntity(). */
    protected boolean _inRemoveEntity = false;

    /** Flag indicating whether to mirror instances of ParameterPort. */
    protected boolean _mirrorParameterPorts = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the class.
     *  @param mirrorParameterPorts If true, then mirror instances of ParameterPort.
     */
    private void _init(boolean mirrorParameterPorts) {
        setClassName("ptolemy.actor.lib.hoc.ReflectComposite");
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-6\" y=\"10\""
                + "style=\"font-size:24\">?</text>\n" + "</svg>\n");
        _mirrorParameterPorts = mirrorParameterPorts;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ReflectCompositeContents

    /** This is a specialized composite actor for use in ReflectComposite.
     *  In particular, it ensures that if ports are added or deleted
     *  locally, then corresponding ports will be added or deleted
     *  in the container.  That addition will result in appropriate
     *  connections being made.
     */
    public static class ReflectCompositeContents extends TypedCompositeActor {
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
        public ReflectCompositeContents(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Override the base class to return a specialized port.
         *  @param name The name of the port to create.
         *  @return A new instance of MirrorPort.
         *  @exception NameDuplicationException If the container already has
         *  a port with this name.
         */
        @Override
        public Port newPort(String name) throws NameDuplicationException {
            try {
                return new MirrorPort(this, name);
            } catch (IllegalActionException ex) {
                // This exception should not occur, so we throw a runtime
                // exception.
                throw new InternalErrorException(this, ex, null);
            }
        }

        /** Add a port to this actor. This overrides the base class to
         *  add a corresponding port to the container using a change
         *  request, if the port is an instance of MirrorPort and
         *  does not already exist.
         *  @param port The TypedIOPort to add to this actor.
         *  @exception IllegalActionException If the port is not an instance of
         *   MirrorPort, or the port has no name.
         *  @exception NameDuplicationException If the port name
         *  collides with a name already in the actor.
         */
        @Override
        protected void _addPort(final Port port) throws IllegalActionException,
                NameDuplicationException {

            super._addPort(port);

            if ((port instanceof MirrorPort || port instanceof ParameterMirrorPort)) {

                final ReflectComposite container = (ReflectComposite) getContainer();

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
                    @Override
                    public NamedObj getLocality() {
                        return getContainer();
                    }

                    @Override
                    protected void _execute() throws Exception {
                        try {
                            workspace().getWriteAccess();

                            if (port instanceof ParameterMirrorPort) {
                                ParameterMirrorPort newPort = (ParameterMirrorPort) container
                                        .getPort(port.getName());

                                if (newPort == null) {
                                    newPort = (ParameterMirrorPort) container
                                            .newParameterPort(port.getName());
                                }
                            } else { // MirrorPort
                                MirrorPort newPort = (MirrorPort) container
                                        .getPort(port.getName());

                                if (newPort == null) {
                                    newPort = (MirrorPort) container
                                            .newPort(port.getName());
                                }

                                if (port instanceof IOPort) {
                                    newPort.setInput(((IOPort) port).isInput());
                                    newPort.setOutput(((IOPort) port)
                                            .isOutput());
                                    newPort.setMultiport(((IOPort) port)
                                            .isMultiport());
                                }
                            }
                        } finally {
                            workspace().doneWriting();
                        }
                    }
                };
                container.requestChange(request);
            }
        }
    }
}
