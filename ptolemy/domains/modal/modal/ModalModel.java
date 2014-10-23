/* Modal models.

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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.FSMDirector;
import ptolemy.domains.modal.kernel.RefinementActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// ModalModel

/**
 This is a typed composite actor designed to be a modal model.
 Inside the modal model is a finite-state machine controller, and
 inside each state in the FSM is a refinement model. To use this
 actor, just drag it into a model, and look inside to start constructing
 the controller.  You may add ports to get inputs and outputs, and
 add states to the controller.  You may add one or more refinements
 to a state (each of these refinements will be executed when this
 actor is executed).  Each refinement is required to have its own
 director, so you will need to choose a director.
 <p>
 The controller is a finite-state machine (FSM), which consists of
 states and transitions.  One of the states is an initial state.
 When this actor executes, if the current state has a refinement,
 then that refinement is executed.  Then the guards on all the outgoing
 transitions of the current state are evaluated, and if one of those
 guards is true, then the transition is taken.  Taking the transition
 means that the actions associated with the transition are executed
 (which can result in producing outputs), and the new current state is
 the state at the destination of the transition.  It is an error if
 more than one of the guards evaluates to true.
 <p>
 To add a state, click on a state button in the toolbar, or drag
 in a state from the library at the left.  To add a transition,
 position the mouse over the source state, hold the control button,
 and drag to the destination state.  The destination state may be
 the same state, in which case the transition is used simply to
 execute its actions.
 <p>
 Adding or removing ports in this actor results in the same ports appearing
 or disappearing in the FSM controller and in each of the refinements.
 Similarly, adding or removing ports in the controller or in the
 refinements results in this actor and the other refinements
 reflecting the same change to the ports.  That is, this actor,
 the controller, and the refinments all contain the same ports.
 <p>
 There is one subtlety regarding ports however.  If you add an
 output port to a refinement, then the corresponding port in the
 controller will be both an input and an output.  The reason for
 this is that the controller can access the results of executing
 a refinement in order to choose a transition.
 <p>
 This class is designed to work closely with ModalController and
 Refinement, since changes to ports can be initiated in this class
 or in those. It works with continuous-time as well as discrete-time
 models.
 <p>
 By default, this actor has a conservative causality interface,
 which examines the FSMActor controller and all the refinements
 and defines input/output dependencies that are the oPlus combination
 of all their dependencies. If
 the <i>stateDependentCausality</i> is false (the default),
 then this causality interface in conservative and valid in all
 states. If it is true, then the causality interface will show
 different input/output dependencies depending on the state.
 In each state, only the controller and the current refinement
 will be considered, and in the controller, only the outgoing
 transitions from the current state will be considered.
 <p>
 This class also fulfills the CTEventGenerator interface so that
 it can report events generated inside.

 @see ModalController
 @see Refinement
 @author Edward A. Lee and Shanna-Shaye Forbes
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class ModalModel extends TypedCompositeActor implements ChangeListener {
    /** Construct a modal model in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ModalModel(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a modal model with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ModalModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A director class name. The default value and the list of
     *  choices are obtained from the suggestedModalModelDirectors()
     *  method of the executive director.  If there is no executive
     *  director, then the default is "ptolemy.domains.modal.kernel.FSMDirector".
     */
    public StringParameter directorClass;

    /** Indicate whether input/output dependencies can depend on the
     *  state. By default, this is false (the default), indicating that a conservative
     *  dependency is provided by the causality interface. Specifically,
     *  if there is a dependency in any state, then the causality interface
     *  indicates that there is a dependency. If this is true, then a less
     *  conservative dependency is provided, indicating a dependency only
     *  if there can be one in the current state.  If this is true, then
     *  upon any state transition, this actor issues a change request, which
     *  forces causality analysis to be redone. Note that this can be expensive.
     */
    public Parameter stateDependentCausality;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change of the director or other property. */
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
                    if (className
                            .equals("ptolemy.domains.modal.kernel.FSMDirector")) {
                        className = "ptolemy.domains.modal.kernel.FSMDirector";
                        try {
                            Class.forName(className);
                        } catch (ClassNotFoundException e2) {
                            throw new IllegalActionException(this, null, e2,
                                    "Invalid directorClass. \"" + className
                                    + "\".");
                        }
                    } else {
                        throw new IllegalActionException(this, null, e,
                                "Invalid directorClass. \"" + className + "\".");
                    }
                }

                final String finalClassName = className;
                // NOTE: Creating a new director has to be done in a
                // change request.
                ChangeRequest request = new ChangeRequest(this,
                        "Create a new director") {
                    @Override
                    protected void _execute() throws Exception {
                        Director director = getDirector();

                        // Contruct a new director
                        //Class newDirectorClass = Class.forName(directorClass
                        //        .stringValue());
                        Class newDirectorClass = Class.forName(finalClassName);
                        Constructor newDirectorConstructor = newDirectorClass
                                .getConstructor(new Class[] {
                                        CompositeEntity.class, String.class });
                        Director newDirector = (Director) newDirectorConstructor
                                .newInstance(new Object[] { ModalModel.this,
                                        uniqueName("_Director") });

                        // The director should not be persistent.
                        newDirector.setPersistent(false);
                        try {
                            StringAttribute newControllerName = (StringAttribute) newDirector
                                    .getAttribute("controllerName");
                            newControllerName.setExpression("_Controller");
                        } catch (Exception e) {
                            throw new IllegalActionException(
                                    "Director class \"" + newDirectorClass
                                    + "\" cannot be used "
                                    + "because it does not have a "
                                    + "\"controllerName\" attribute.");
                        }

                        if (director != null
                                && director.getContainer() == ModalModel.this) {
                            // Delete the old director.
                            director.setContainer(null);
                        }

                        // Check whether the modal controller needs to
                        // support multirate firing.
                        Director executiveDirector = getExecutiveDirector();

                        if (executiveDirector != null) {
                            // Need both the executive director and the local director to
                            // support multirate firing in order for this to work.
                            if (newDirector.supportMultirateFiring()
                                    && executiveDirector
                                    .supportMultirateFiring()) {
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

    /** React to a change request has been successfully executed.
     *  This method is called after a change request
     *  has been executed successfully.
     *  This implementation does nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
        // Ignore... Nothing to do.
    }

    /** React to a change request has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution an exception was thrown.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        MessageHandler.error("Failed to create a new director.", exception);
    }

    /** Override the base class to ensure that the _controller private
     *  variable is reset to the controller of the cloned object.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return The new Entity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModalModel newModel = (ModalModel) super.clone(workspace);
        newModel._controller = (FSMActor) newModel.getEntity("_Controller");
        newModel._causalityInterfacesVersions = null;

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

    /** Override the base class to remove any unused refinements
     *  before exporting.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     *  @see ptolemy.kernel.util.MoMLExportable
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        try {
            HashSet<TypedActor> activeRefinements = new HashSet<TypedActor>();
            for (State state : _controller.entityList(State.class)) {
                TypedActor refinements[] = state.getRefinement();
                if (refinements != null) {
                    for (TypedActor refinement : refinements) {
                        activeRefinements.add(refinement);
                    }
                }
            }
            List<Transition> transitions = _controller.relationList();
            for (Transition transition : transitions) {
                TypedActor refinements[] = transition.getRefinement();
                if (refinements != null) {
                    for (TypedActor refinement : refinements) {
                        activeRefinements.add(refinement);
                    }
                }
            }
            for (TypedActor actor : entityList(Refinement.class)) {
                // FindBugs: Using pointer equality to compare _controller (a FSMActor)
                // with actor, which is a Refinement)
                if (!activeRefinements.contains(actor) /*&& actor != _controller*/) {
                    if (MessageHandler
                            .yesNoQuestion("Unused state refinement in modal model: "
                                    + actor.getFullName() + ". Remove it?")) {
                        ((ComponentEntity) actor).setContainer(null);
                    }
                }
            }
        } catch (KernelException ex) {
            // This should not happen. Ignore and continue the export.
            System.out.println(ex);
        }
        super.exportMoML(output, depth, name);
    }

    /** Get the attribute with the given name. The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is contained by a (deeply) contained attribute, port,
     *  relation, or entity.
     *  If the name contains one or more periods, then it is assumed
     *  to be the relative name of an attribute contained by one of
     *  the contained attributes, ports, entities or relations.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    @Override
    public Attribute getAttribute(String name) {
        try {
            _workspace.getReadAccess();

            // Check attributes and ports first.
            Attribute result = super.getAttribute(name);

            //delegate the attribute to the controller
            if (result == null && _controller != null) {
                result = _controller.getAttribute(name);
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get representation of dependencies between input ports and
     *  output ports.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        try {
            boolean stateDependentCausality = ((BooleanToken) _controller.stateDependentCausality
                    .getToken()).booleanValue();
            if (!stateDependentCausality) {
                return super.getCausalityInterface();
            }
            // Causality is state dependent if we get here.
            // Return the causality interface of the
            // controller FSMActor composed with
            // the current refinement causality interfaces.
            if (_causalityInterfacesVersions == null) {
                _causalityInterfacesVersions = new HashMap<State, Long>();
                _causalityInterfaces = new HashMap<State, MirrorCausalityInterface>();
            }
            State currentState = _controller.currentState();
            Long version = _causalityInterfacesVersions.get(currentState);
            MirrorCausalityInterface causality = _causalityInterfaces
                    .get(currentState);
            if (version == null || causality == null
                    || version.longValue() != workspace().getVersion()) {
                // Need to create or update a causality interface for the current state.
                CausalityInterface controllerCausality = _controller
                        .getCausalityInterface();
                causality = new MirrorCausalityInterface(this,
                        controllerCausality);
                Actor[] refinements = currentState.getRefinement();
                if (refinements != null) {
                    for (Actor refinement : refinements) {
                        CausalityInterface refinementCausality = refinement
                                .getCausalityInterface();
                        causality.composeWith(refinementCausality);
                    }
                }
                _causalityInterfaces.put(currentState, causality);
                _causalityInterfacesVersions.put(currentState,
                        Long.valueOf(workspace().getVersion()));
            }
            return causality;
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Get the FSM controller.
     *  @return The FSM controller.
     */
    public FSMActor getController() {
        return _controller;
    }

    /** Handle a model error.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error has been handled, or false if the
     *   error is not handled.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.
     */
    @Override
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {
        if (_debugging) {
            _debug("handleModelError called for the ModalModel "
                    + this.getDisplayName());
        }
        // If the controller FSM can handle the error, let it.
        // Otherwise, delegate to the superclass.
        if (!getController().handleModelError(context, exception)) {
            return super.handleModelError(context, exception);
        }
        return true;
    }

    /** Initialize the mode controller and all the refinements by
     * calling the initialize() method in the super class. Build the
     * local maps for receivers. Suspend all the refinements of states
     * that are not the current state.
     *
     * @exception IllegalActionException If thrown by the initialize()
     *                method of the super class, or can not find mode
     *                controller, or can not find refinement of the
     *                current state.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Reset local receivers here before the initialize method
        // of the director so that any initial outputs that
        // the FSM controller or refinements might produce in their initialize methods
        // are the only outputs produced by initialize.
        Director director = getDirector();
        if (director instanceof FSMDirector) {
            ((FSMDirector) director).resetOutputReceivers();
        }
        super.initialize();
    }

    /** Create a new port with the specified name in this entity, the
     *  controller, and all the refinements.  Link these ports so that
     *  if the new port is set to be an input, output, or multiport, then
     *  the change is mirrored in the other ports.  The new port will be
     *  an instance of ModalPort, which extends TypedIOPort.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
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
    ////                         protected variables               ////

    /** The FSM controller. */
    protected FSMActor _controller;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Initialize the model.
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.domains.modal.modal.ModalModel");

        stateDependentCausality = new Parameter(this, "stateDependentCausality");
        stateDependentCausality.setTypeEquals(BaseType.BOOLEAN);
        stateDependentCausality.setExpression("false");

        // Create a default modal controller.
        // NOTE: It would be much nicer if the director created the
        // controller it likes (or has it configured) and returned it
        // (zk 2002/09/11)
        _controller = new ModalController(this, "_Controller");
        // Whether the controller does conservative analysis or not should
        // depend on the parameter of this actor.
        _controller.stateDependentCausality
        .setExpression("stateDependentCausality");

        // configure the directorClass parameter
        directorClass = new StringParameter(this, "directorClass");

        // Set the director to the default. Note that doing
        // this manually rather than in attributeChanged() prevents
        // attributeChanged() from issuing a change request
        // (because the director class matches the default).
        // Issuing a change request during construction is
        // problematic because it causes the Vergil library
        // to close when you first open a sublibrary containing
        // an instance of ModalModel.
        FSMDirector defaultFSMDirector = new FSMDirector(this, "_Director");
        defaultFSMDirector.controllerName.setExpression("_Controller");

        // NOTE: If there is a container for this ModalModel, and it
        // has a director, then we get the default value from that
        // director, and also get a list of suggested values.
        Director executiveDirector = getExecutiveDirector();

        if (executiveDirector != null) {
            // FIXME: Better solution is to override the returned
            // list of choices in the parameter class.
            String[] suggestions = executiveDirector
                    .suggestedModalModelDirectors();

            for (int i = 0; i < suggestions.length; i++) {
                suggestions[i] = suggestions[i].replace("domains.fsm.",
                        "domains.modal.");
                directorClass.addChoice(suggestions[i]);
                if (i == 0) {
                    directorClass.setExpression(suggestions[i]);
                }
            }
        } else {
            // If there is no executive director. Use the default director.
            // This happens when vergil starts, and when a modal model is
            // dropped into a blank editor. Model designers need to configure
            // it if FSMDirector is not the desired director.
            directorClass
            .setExpression("ptolemy.domains.modal.kernel.FSMDirector");
        }

        // Create a more reasonable default icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
                + "height=\"40\" style=\"fill:red\"/>\n"
                + "<rect x=\"-28\" y=\"-18\" width=\"56\" "
                + "height=\"36\" style=\"fill:lightgrey\"/>\n"
                + "<ellipse cx=\"0\" cy=\"0\"" + " rx=\"15\" ry=\"10\"/>\n"
                + "<circle cx=\"-15\" cy=\"0\""
                + " r=\"5\" style=\"fill:white\"/>\n"
                + "<circle cx=\"15\" cy=\"0\""
                + " r=\"5\" style=\"fill:white\"/>\n" + "</svg>\n");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The causality interfaces by state, for the case
     *  where the causality interface is state dependent.
     */
    private Map<State, MirrorCausalityInterface> _causalityInterfaces;

    /** The workspace version for causality interfaces by state, for the case
     *  where the causality interface is state dependent.
     */
    private Map<State, Long> _causalityInterfacesVersions;

}
