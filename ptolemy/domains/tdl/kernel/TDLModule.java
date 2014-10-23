/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2008-2014 The Regents of the University of California.
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
 */
package ptolemy.domains.tdl.kernel;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.modal.modal.ModalPort;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////TDLModule

/**
 * A TDL module forms a unit that consists of sensors, actuators, and modes.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 8.0
 */
public class TDLModule extends ModalModel {

    /**
     * Construct a tdl module in the specified workspace with no container and
     * an empty string as a name. You can then change the name with setName().
     * If the workspace argument is null, then use the default workspace.
     *
     * @param workspace
     *            The workspace that will list the actor.
     * @exception IllegalActionException
     *                If the name has a period in it, or the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container already contains an entity with the
     *                specified name.
     */
    public TDLModule(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /**
     * Construct a tdl module with a name and a container. The container
     * argument must not be null, or a NullPointerException will be thrown.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of this actor.
     * @exception IllegalActionException
     *                If the container is incompatible with this actor.
     * @exception NameDuplicationException
     *                If the name coincides with an actor already in the
     *                container.
     */
    public TDLModule(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * React to a change of the director or other property.
     *
     * needs to be overridden because of the director class that should always
     * be TDLModuleDirector.
     *
     * @param attribute Attribute to be changed.
     * @exception IllegalActionException Thrown if director of the module, the TDLModuleDirector,
     * cannot be set.
     *
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == directorClass) {
            // We should change the director only if the current
            // director is not of the right class.
            Director director = getDirector();
            String className = directorClass.stringValue();

            if (director == null
                    || !director.getClass().getName().equals(className)) {
                // Check the class name to get immediate feedback
                // to the user.
                try {
                    Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this, null, e,
                            "Invalid directorClass.");
                }

                // NOTE: Creating a new director has to be done in a
                // change request.
                ChangeRequest request = new ChangeRequest(this,
                        "Create a new director") {
                    @Override
                    protected void _execute() throws Exception {
                        Director director = getDirector();

                        // Contruct a new director
                        Class newDirectorClass = Class.forName(directorClass
                                .stringValue());
                        Constructor newDirectorConstructor = newDirectorClass
                                .getConstructor(new Class[] {
                                        CompositeEntity.class, String.class });
                        TDLModuleDirector newDirector = (TDLModuleDirector) newDirectorConstructor
                                .newInstance(new Object[] { TDLModule.this,
                                        uniqueName("_Director") });

                        // The director should not be persistent.
                        newDirector.setPersistent(false);
                        newDirector.controllerName.setExpression("_Controller");

                        if (director != null
                                && director.getContainer() == TDLModule.this) {
                            // Delete the old director.
                            director.setContainer(null);
                        }

                        // Check whether the modal controller needs to
                        // support multirate firing.
                        Director executiveDirector = getExecutiveDirector();

                        if (executiveDirector != null) {
                            boolean supportMultirateFiring = executiveDirector
                                    .supportMultirateFiring();

                            if (supportMultirateFiring) {
                                getController().setSupportMultirate(true);
                            }
                        }
                    }
                };
                requestChange(request);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Override the base class to ensure that the _controller private variable
     * is reset to the controller of the cloned object.
     *
     * @param workspace
     *            The workspace for the cloned object.
     * @exception CloneNotSupportedException
     *                If cloned ports cannot have as their container the cloned
     *                entity (this should not occur), or if one of the
     *                attributes cannot be cloned.
     * @return The new Entity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TDLModule newModel = (TDLModule) super.clone(workspace);
        newModel._controller = (TDLActor) newModel.getEntity("_Controller");

        try {
            // Validate the directorClass parameter so that the director
            // gets created in the clone.
            newModel.directorClass.validate();
            newModel.executeChangeRequests();
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException(
                    "Failed to validate the director of the clone of "
                            + getFullName());
        }

        return newModel;
    }

    @Override
    public CausalityInterface getCausalityInterface() {
        if (getDirector().defaultDependency() instanceof BooleanDependency) {
            return new BreakCausalityInterface(this, getDirector()
                    .defaultDependency());
        } else {
            return super.getCausalityInterface();
        }
    }

    /**
     * Create a new port with the specified name in this entity, the controller,
     * and all the refinements. Link these ports so that if the new port is set
     * to be an input, output, or multiport, then the change is mirrored in the
     * other ports. The new port will be an instance of ModalPort, which extends
     * TypedIOPort. This method is write-synchronized on the workspace, and
     * increments its version number.
     *
     * @param name
     *            The name to assign to the newly created port.
     * @return The new port.
     * @exception NameDuplicationException
     *                If the entity already has a port with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            ModalPort port = new ModalPort(this, name);

            // Create mirror ports.
            Iterator entities = entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();

                if (entity instanceof TDLController) {
                    if (entity.getPort(name) == null) {
                        try {
                            ((TDLController) entity)._mirrorDisable = true;

                            /* Port newPort = */entity.newPort(name);

                            /*
                             * No longer needed since Yuhong modified the type
                             * system to allow UNKNOWN. EAL if (newPort
                             * instanceof TypedIOPort) {
                             * ((TypedIOPort)newPort).setTypeSameAs(port); }
                             */
                        } finally {
                            ((TDLController) entity)._mirrorDisable = false;
                        }
                    }
                } else if (entity instanceof Refinement) {
                    if (entity.getPort(name) == null) {
                        try {
                            // FIXME: not sure if this should be -1 or 1.
                            ((Refinement) entity).setMirrorDisable(1);

                            /* Port newPort = */entity.newPort(name);

                            /*
                             * No longer needed since Yuhong modified the type
                             * system to allow UNKNOWN. EAL if (newPort
                             * instanceof TypedIOPort) {
                             * ((TypedIOPort)newPort).setTypeSameAs(port); }
                             */
                        } finally {
                            ((Refinement) entity).setMirrorDisable(0);
                        }
                    }
                }
            }

            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "ModalModel.newPort: Internal error: " + ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initialize the model.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {

        setClassName("ptolemy.domains.tdl.kernel.TDLModule");
        directorClass
        .setExpression("ptolemy.domains.tdl.kernel.TDLModuleDirector");
        _controller.removeAllEntities();
        _controller.removeAllPorts();
        _controller.removeAllRelations();
        _controller.setContainer(null);

        TDLModuleDirector defaultTDLDirector = new TDLModuleDirector(this,
                "_TDLDirector");
        defaultTDLDirector.controllerName.setExpression("_Controller");

        ComponentEntity controller = getEntity("_Controller");
        if (controller != null) {
            controller.setContainer(null);
        }
        _controller = new TDLController(this, "_Controller");

        _controller.stateDependentCausality
        .setExpression("stateDependentCausality");

    }

}
