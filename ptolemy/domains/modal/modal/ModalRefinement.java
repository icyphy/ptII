/* Controller for modal models.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.modal;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import ptolemy.actor.IOPort;
import ptolemy.actor.InstanceOpener;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.kernel.ContainmentExtender;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.RefinementActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DropTargetHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

// NOTE: This class duplicates code in ModalController and Refinement, but
// because of the inheritance hierarchy, there appears to be no convenient
// way to share the code.

///////////////////////////////////////////////////////////////////
//// ModalRefinement

/**
 This modal model actor supports mirroring of its ports in its container
 (which is required to be a refinement of a state in a ModalModel).
 This in turn assures
 mirroring of ports in each of the refinements.
 <p>
 Note that this actor has no attributes of its own.
 Requests for attributes are delegated to the container.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class ModalRefinement extends ModalModel implements DropTargetHandler,
RefinementActor {

    /** Construct a modal controller with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ModalRefinement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        ContainmentExtender containmentExtender = new ContainmentExtender(this,
                "_containmentExtender");
        containmentExtender.setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a list of objects being dropped onto a target.
     *
     *  @param target The target on which the objects are dropped.
     *  @param dropObjects The list of objects dropped onto the target.
     *  @param moml The moml string generated for the dropped objects.
     *  @exception IllegalActionException If the handling is unsuccessful.
     */
    @Override
    public void dropObject(NamedObj target, List dropObjects, String moml)
            throws IllegalActionException {
        boolean merge = false;
        if (target instanceof State) {
            State state = (State) target;

            TreeMap<Class<? extends Entity>, String> map = _getRefinementClasses();
            String refinementClass = null;
            boolean conflict = false;
            RefinementSuggestion suggestion = (RefinementSuggestion) target
                    .getAttribute("refinementSuggestion");
            for (Object dropObject : dropObjects) {
                if (suggestion != null) {
                    String className = suggestion
                            .getRefinementClass((NamedObj) dropObject);
                    if (refinementClass == null) {
                        refinementClass = className;
                    } else if (!refinementClass.equals(className)) {
                        conflict = true;
                    }
                } else {
                    for (Class<? extends Entity> keyClass : map.keySet()) {
                        if (keyClass.isInstance(dropObject)) {
                            String value = map.get(keyClass);
                            if (refinementClass == null) {
                                refinementClass = value;
                                break;
                            } else if (!refinementClass.equals(value)) {
                                conflict = true;
                                break;
                            }
                        }
                    }
                }
                if (conflict) {
                    break;
                }
            }

            if (conflict || refinementClass == null) {
                throw new IllegalActionException(this, "Unable to determine "
                        + "the type of all the dropped objects.");
            }

            TypedActor[] refinements = state.getRefinement();
            TypedActor refinement = null;
            if (refinements != null) {
                for (TypedActor actor : refinements) {
                    if (((NamedObj) actor).getClassName().equals(
                            refinementClass)) {
                        refinement = actor;
                        break;
                    }
                }
            }
            if (refinement == null) {
                CompositeEntity containerContainer = (CompositeEntity) state
                        .getContainer().getContainer();
                String name = containerContainer.uniqueName(state.getName());
                addRefinement(state, name, null, refinementClass, null);
                merge = true;
                refinement = (TypedActor) containerContainer.getEntity(name);
            }
            target = (NamedObj) refinement;
        }
        MoMLChangeRequest request = new MoMLChangeRequest(this, target, moml);
        request.setUndoable(true);
        if (merge) {
            request.setMergeWithPreviousUndo(true);
        }
        target.requestChange(request);
    }

    /** Get the state in any ModalRefinement within this ModalModel that has
     *  this ModalRefinement as its refinement, if any. Return null if no such
     *  state is found.
     *
     *  @return The state with this ModalRefinement as its refinement, or null.
     *  @exception IllegalActionException If the specified refinement cannot be
     *   found in a state, or if a comma-separated list is malformed.
     */
    @Override
    public State getRefinedState() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof ModalModel) {
            List<?> controllers = ((ModalModel) container)
                    .entityList(ModalRefinement.class);
            for (Object controllerObject : controllers) {
                ModalRefinement controller = (ModalRefinement) controllerObject;
                List<?> states = controller.entityList(State.class);
                for (Object stateObject : states) {
                    State state = (State) stateObject;
                    TypedActor[] refinements = state.getRefinement();
                    if (refinements != null) {
                        for (TypedActor refinement : refinements) {
                            if (refinement == this) {
                                return state;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Create a new port with the specified name in the container of
     *  this controller, which in turn creates a port in this controller
     *  and all the refinements.
     *  This method is write-synchronized on the workspace.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            ModalModel container = (ModalModel) getContainer();

            if (_mirrorDisable == 1 || container == null) {
                // We are mirroring a change above in the hierarchy
                // (or there is no above in the hierarchy),
                // so we should not mirror this change upwards.
                // But we should mirror it downwards.
                ModalRefinementPort port = new ModalRefinementPort(this, name);

                // Create the links on the outside of the new port.
                if (container != null) {
                    String relationName = name + "Relation";
                    Relation relation = container.getRelation(relationName);

                    if (relation == null) {
                        relation = container.newRelation(relationName);

                        Port containerPort = container.getPort(name);
                        containerPort.link(relation);
                    }

                    port.link(relation);
                }
                // Mirror the change downwards in the hierarchy.
                Iterator entities = entityList().iterator();
                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();

                    if (entity instanceof RefinementActor) {
                        if (entity.getPort(name) == null) {
                            try {
                                ((RefinementActor) entity).setMirrorDisable(1);
                                entity.newPort(name);
                            } finally {
                                ((RefinementActor) entity).setMirrorDisable(0);
                            }
                        }
                    }
                }
                return port;
            } else {
                // We are originating the change, or it originated from
                // below us in the hierarchy, and hence should delegate
                // it upwards. This will cause this newPort() method
                // to be called again by the container after it has
                // created its own port. That will result in the code
                // above executing because it will set _mirrorPort to 1
                // on this port before doing the call.
                ModalRefinementPort containerPort = container == null ? null
                        : (ModalRefinementPort) container.getPort(name);
                if (containerPort == null) {
                    // The container does not have a mirror port.
                    // Delegate upwards.
                    // Note that this will result in the container
                    // creating the port here by calling this
                    // newPort() method. It will set my
                    // _mirrorDisable to 1 before doing that,
                    // and set it back to 0 after.
                    container.newPort(name);
                    return getPort(name);
                } else {
                    // The container already has a mirror port, so
                    // we cannot use it to create our port.
                    // This can happen if the container was created first
                    // and populated with its ports before this refinement
                    // was created.
                    ModalRefinementPort port = new ModalRefinementPort(this,
                            name);
                    // Create the link on the outside of the port.
                    String relationName = name + "Relation";
                    Relation relation = container.getRelation(relationName);
                    // The container should already have this relation,
                    // but in case not, we create it.
                    if (relation == null) {
                        relation = container.newRelation(relationName);
                        containerPort.link(relation);
                    }
                    port.link(relation);

                    // Mirror the change downwards in the hierarchy.
                    Iterator entities = entityList().iterator();
                    while (entities.hasNext()) {
                        Entity entity = (Entity) entities.next();

                        if (entity instanceof RefinementActor) {
                            if (entity.getPort(name) == null) {
                                try {
                                    ((RefinementActor) entity)
                                    .setMirrorDisable(1);
                                    entity.newPort(name);
                                } finally {
                                    ((RefinementActor) entity)
                                    .setMirrorDisable(0);
                                }
                            }
                        }
                    }
                    return port;
                }
            }
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "ModalRefinement.newPort: Internal error: "
                            + ex.getMessage());
        } finally {
            _mirrorDisable = 0;
            _workspace.doneWriting();
        }
    }

    /** Control whether adding a port should be mirrored in the modal
     *  model and refinements.
     *  This is added to allow control by the UI.
     *  @param disable 0 if mirroring should occur, -1
     *   if mirroring should not occur downwards in the hierarchy,
     *   1 if mirroring should not occur upwards in the hierarchy.
     */
    @Override
    public void setMirrorDisable(int disable) {
        _mirrorDisable = disable;
    }

    /** Override the base class to ensure that the proposed container
     *  is a ModalModel or null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof ModalModel) && container != null) {
            throw new IllegalActionException(container, this,
                    "ModalRefinement can only be contained by "
                            + "ModalModel objects.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a map from the classes of the entities to be dropped into a state
     *  and the class names of the refinements that can be used to contain those
     *  entities.
     *
     *  @return The map.
     */
    protected TreeMap<Class<? extends Entity>, String> _getRefinementClasses() {
        TreeMap map = new TreeMap<Class<? extends Entity>, String>(
                new ClassComparator());
        map.put(State.class, ModalRefinement.class.getName());
        map.put(ComponentEntity.class, TypedCompositeActor.class.getName());
        return map;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Indicator that we are processing a newPort request from above
     *  (if 1) or below (if -1) in the hierarchy.
     */
    protected int _mirrorDisable = 0;

    /** Create a refinement for the given state.
     *
     *  @param state The state that will contain the new refinement.
     *  @param name The name of the composite entity that stores the refinement.
     *  @param template The template used to create the refinement, or null if
     *   template is not used.
     *  @param className The class name for the refinement, which is used when
     *   template is null.
     *  @param instanceOpener The instanceOpener, typically a
     *   Configuration, that is used to open the refinement (as a
     *   look-inside action) after it is created, or null if it is not
     *   needed to open the refinement.
     *  @exception IllegalActionException If error occurs while creating the
     *   refinement.
     */
    @Override
    public void addRefinement(State state, final String name, Entity template,
            String className, final InstanceOpener instanceOpener)
                    throws IllegalActionException {
        ModalRefinement.addRefinement(state, name, template, className,
                instanceOpener, (CompositeEntity) getContainer());
    }

    /** Create a refinement for the given state.
     *
     *  @param state The state that will contain the new refinement.
     *  @param name The name of the composite entity that stores the refinement.
     *  @param template The template used to create the refinement, or null if
     *   template is not used.
     *  @param className The class name for the refinement, which is used when
     *   template is null.
     *  @param instanceOpener The instanceOpener, typically a
     *   Configuration, that is used to open the refinement (as a
     *   look-inside action) after it is created, or null if it is not
     *   needed to open the refinement.
     *  @param container The container.
     *  @exception IllegalActionException If error occurs while creating the
     *   refinement.
     */
    public static void addRefinement(State state, final String name,
            Entity template, String className,
            final InstanceOpener instanceOpener, final CompositeEntity container)
            throws IllegalActionException {

        // This method is static so that ModalController can call it
        // and avoid code duplication.

        if (container == null) {
            throw new IllegalActionException(state, "State container has no "
                    + "container!");
        }

        if (container.getEntity(name) != null) {
            throw new IllegalActionException(state, "There is already a "
                    + "refinement with name " + name + ".");
        }

        Attribute allowRefinement = state.getAttribute("_allowRefinement");
        if (allowRefinement instanceof Parameter
                && !((BooleanToken) ((Parameter) allowRefinement).getToken())
                .booleanValue()) {
            throw new IllegalActionException(state, "State does not support "
                    + "refinement.");
        }

        String currentRefinements = state.refinementName.getExpression();

        if (currentRefinements == null || currentRefinements.equals("")) {
            currentRefinements = name;
        } else {
            currentRefinements = currentRefinements.trim() + ", " + name;
        }

        String moml;

        // The MoML we create depends on whether the configuration
        // specified a set of prototype refinements.
        if (template != null) {
            String templateDescription = template.exportMoML(name);
            moml = "<group>" + templateDescription + "<entity name=\""
                    + state.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + currentRefinements + "\"/></entity></group>";
        } else {
            moml = "<group><entity name=\"" + name + "\" class=\"" + className
                    + "\"/>" + "<entity name=\"" + state.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + currentRefinements + "\"/></entity></group>";
        }

        MoMLChangeRequest change = new MoMLChangeRequest(state, container, moml) {
            @Override
            protected void _execute() throws Exception {
                super._execute();

                // Mirror the ports of the container in the refinement.
                // Note that this is done here rather than as part of
                // the MoML because we have set protected variables
                // in the refinement to prevent it from trying to again
                // mirror the changes in the container.
                // The following entity is newly created refinement.
                Entity entity = container.getEntity(name);

                // Get the initial port configuration from the container.
                Iterator ports = container.portList().iterator();

                while (ports.hasNext()) {
                    Port port = (Port) ports.next();

                    try {
                        if (entity instanceof RefinementActor) {
                            // Prevent the newly created refinement from
                            // creating a port in its container.
                            ((RefinementActor) entity).setMirrorDisable(1);
                        }

                        String name = port.getName();
                        Port newPort = entity.getPort(name);
                        if (newPort == null) {
                            newPort = entity.newPort(port.getName());
                        }

                        if (newPort instanceof RefinementPort
                                && port instanceof IOPort) {
                            try {
                                ((RefinementPort) newPort)
                                .setMirrorDisable(true);

                                if (((IOPort) port).isInput()) {
                                    ((RefinementPort) newPort).setInput(true);
                                }

                                if (((IOPort) port).isOutput()) {
                                    ((RefinementPort) newPort).setOutput(true);
                                }

                                if (((IOPort) port).isMultiport()) {
                                    ((RefinementPort) newPort)
                                    .setMultiport(true);
                                }

                                // Copy the location to the new port if any.
                                // (tfeng 08/29/08)
                                if (container instanceof ModalModel) {
                                    FSMActor controller = ((ModalModel) container)
                                            .getController();
                                    if (controller != null
                                            && controller != container) {
                                        Port controllerPort = controller
                                                .getPort(port.getName());
                                        if (controllerPort != null) {
                                            Location location = (Location) controllerPort
                                                    .getAttribute("_location",
                                                            Location.class);
                                            if (location != null) {
                                                location = (Location) location
                                                        .clone(newPort
                                                                .workspace());
                                                location.setContainer(newPort);
                                            }
                                        }
                                    }
                                }
                            } finally {
                                ((RefinementPort) newPort)
                                .setMirrorDisable(false);
                            }
                        }
                    } finally {
                        if (entity instanceof RefinementActor) {
                            ((RefinementActor) entity).setMirrorDisable(0);
                        }
                    }
                }

                if (instanceOpener != null) {
                    // Look inside.
                    instanceOpener.openAnInstance(entity);
                }
            }
        };

        container.requestChange(change);
    }
}
