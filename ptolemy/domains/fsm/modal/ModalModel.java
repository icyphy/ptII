/* Modal models.

Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.modal;

import ptolemy.actor.Director;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

import java.lang.reflect.Constructor;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
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
   This class also fulfills the CTEventGenerator interfact so that
   it can report events generated inside.

   @see ModalController
   @see Refinement
   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (reviewmoderator)
*/
public class ModalModel extends CTCompositeActor implements ChangeListener {
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
    public ModalModel(Workspace workspace)
        throws IllegalActionException, NameDuplicationException {
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
     *  director, then the default is "ptolemy.domains.fsm.kernel.FSMDirector".
     */
    public StringParameter directorClass;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change of the director or other property. */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == directorClass) {
            // We should change the director only if the current
            // director is not of the right class.
            Director director = getDirector();
            String className = directorClass.stringValue();

            if ((director == null)
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
                        protected void _execute() throws Exception {
                            Director director = getDirector();
                            Class newDirectorClass = Class.forName(directorClass
                                                .stringValue());
                            Constructor newDirectorConstructor = newDirectorClass
                                            .getConstructor(new Class[] {
                                                    CompositeEntity.class,
                                                    String.class
                                                });
                            FSMDirector newDirector = (FSMDirector) newDirectorConstructor
                                            .newInstance(new Object[] {
                                                    ModalModel.this,
                                                    uniqueName("_Director")
                                                });

                            // The director should not be persistent.
                            newDirector.setPersistent(false);
                            newDirector.controllerName.setExpression(
                                "_Controller");

                            if ((director != null)
                                            && (director.getContainer() == ModalModel.this)) {
                                // Delete the old director.
                                director.setContainer(null);
                            }
                        }
                    };

                requestChange(request);
            }
        }
    }

    /** React to a change request has been successfully executed.
     *  This method is called after a change request
     *  has been executed successfully.
     *  This implementation does nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModalModel newModel = (ModalModel) super.clone(workspace);
        newModel._controller = (FSMActor) newModel.getEntity("_Controller");

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

    /** Return an instance of DirectedGraph, where the nodes are IOPorts,
     *  and the edges are the relations between ports. The graph shows
     *  the dependencies between the input and output ports. If there is
     *  a path between a pair, input and output, they are dependent.
     *  Otherwise, they are independent.
     */
    public FunctionDependency getFunctionDependency() {
        FunctionDependency functionDependency = (FunctionDependency) getAttribute(FunctionDependency.UniqueName);

        if (functionDependency == null) {
            try {
                functionDependency = new FunctionDependencyOfModalModel(this,
                        FunctionDependency.UniqueName);
            } catch (NameDuplicationException e) {
                // This should not happen.
                throw new InternalErrorException("Failed to construct a"
                    + "function dependency object for " + getName());
            } catch (IllegalActionException e) {
                // This should not happen.
                throw new InternalErrorException("Failed to construct a"
                    + "function dependency object for " + getName());
            }
        }

        return functionDependency;
    }

    /** Get the FSM controller.
     *  @return The FSM controller.
     */
    public FSMActor getController() {
        return _controller;
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
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            ModalPort port = new ModalPort(this, name);

            // Create mirror ports.
            Iterator entities = entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();

                if (entity instanceof ModalController) {
                    if (entity.getPort(name) == null) {
                        try {
                            ((ModalController) entity)._mirrorDisable = true;

                            /*Port newPort = */ entity.newPort(name);

                            /* No longer needed since Yuhong modified
                             * the type system to allow UNKNOWN. EAL
                             if (newPort instanceof TypedIOPort) {
                             ((TypedIOPort)newPort).setTypeSameAs(port);
                             }
                            */
                        } finally {
                            ((ModalController) entity)._mirrorDisable = false;
                        }
                    }
                } else if (entity instanceof Refinement) {
                    if (entity.getPort(name) == null) {
                        try {
                            ((Refinement) entity)._mirrorDisable = true;

                            /*Port newPort = */ entity.newPort(name);

                            /* No longer needed since Yuhong modified
                             * the type system to allow UNKNOWN. EAL
                             if (newPort instanceof TypedIOPort) {
                             ((TypedIOPort)newPort).setTypeSameAs(port);
                             }
                            */
                        } finally {
                            ((Refinement) entity)._mirrorDisable = false;
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
    private void _init()
        throws IllegalActionException, NameDuplicationException {
        // Mark this composite actor as a strict actor. This will be used for
        // scheduling analysis, for example in the DE domain.
        new Attribute(this, "_strictMarker");

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.domains.fsm.modal.ModalModel");

        // Create a default modal controller.
        // NOTE: It would be much nicer if the director created the
        // controller it likes (or has it configured) and returned it
        // (zk 2002/09/11)
        _controller = new ModalController(this, "_Controller");

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
            directorClass.setExpression(
                "ptolemy.domains.fsm.kernel.FSMDirector");
        }

        // Create a more reasonable default icon.
        _attachText("_iconDescription",
            "<svg>\n" + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
            + "height=\"40\" style=\"fill:red\"/>\n"
            + "<rect x=\"-28\" y=\"-18\" width=\"56\" "
            + "height=\"36\" style=\"fill:lightgrey\"/>\n"
            + "<ellipse cx=\"0\" cy=\"0\"" + " rx=\"15\" ry=\"10\"/>\n"
            + "<circle cx=\"-15\" cy=\"0\""
            + " r=\"5\" style=\"fill:white\"/>\n"
            + "<circle cx=\"15\" cy=\"0\""
            + " r=\"5\" style=\"fill:white\"/>\n" + "</svg>\n");
    }
}
