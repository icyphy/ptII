/* The transition controller for modal transitions

 Copyright (c) 1998-2003 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.fsm.modal;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.fsm.modal.*;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.fsm.TransitionController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

//////////////////////////////////////////////////////////////////////////
//// ModalTransitionController
/**
This class provides interaction with relations that contain general purpose
refinements that are fired on state transitions in an FSM graph. It's
implementation is largely copied from ModalController.

@author David Hermann, Research In Motion Limited
@version $Id$
@since Ptolemy II 2.0
*/
public class ModalTransitionController extends TransitionController {

    /** Create a transition controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public ModalTransitionController(final GraphController controller) {
        super(controller);

        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new AddRefinementAction()));
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new RemoveRefinementAction()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to add a new refinement.
     */
    private class AddRefinementAction extends FigureAction {
        public AddRefinementAction() {
            super("Add Refinement");
        }
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            NamedObj target = getTarget();
            if (!(target instanceof Transition)) {
                MessageHandler.error("Can only add refinements to transitions.");
                return;
            }
            Transition transition = (Transition)target;

            // Check that all these containers exist.
            Nameable immediateContainer = transition.getContainer();
            if (immediateContainer == null) {
                MessageHandler.error("Transition has no container!");
                return;
            }
            final CompositeEntity container
                = (CompositeEntity)immediateContainer.getContainer();
            if (container == null) {
                MessageHandler.error("Transition container has no container!");
                return;
            }

            // Open a dialog to get the refinement name and class.
            Query query = new Query();
            String defaultName = container.uniqueName(transition.getName());
            query.addLine("Name", "Name", defaultName);

            // See whether the configuration offers transition refinements.
            Configuration configuration
                = ((FSMGraphController)getController()).getConfiguration();
            Entity refinements
                = configuration.getEntity("_transitionRefinements");

            // Default choices.
            String[] choiceClasses
                = {"ptolemy.vergil.fsm.modal.TransitionRefinement" };
            String[] choiceNames = {"Default Refinement"};

            // Check the configuration to see whether the default is overridden.
            if (refinements instanceof CompositeEntity) {
                // There is a specification.
                List refinementList
                    = ((CompositeEntity)refinements).entityList();
                choiceNames = new String[refinementList.size()];
                choiceClasses = new String[refinementList.size()];
                Iterator iterator = refinementList.iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Entity entity = (Entity)iterator.next();
                    choiceNames[count] = entity.getName();
                    choiceClasses[count++] = entity.getClass().getName();
                }
            }
            query.addChoice(
                    "Class", "Class", choiceNames, choiceNames[0], true);

            // FIXME: Need a frame owner for first arg.
            // Perhaps calling getController(), which returns a GraphController
            // will be a good start.
            ComponentDialog dialog = new ComponentDialog(null,
                    "Specify Refinement", query);
            if (!dialog.buttonPressed().equals("OK")) {
                return;
            }

            final String newName = query.getStringValue("Name");
            if (container.getEntity(newName) != null) {
                MessageHandler.error("There is already a refinement with name "
                        + newName + ".");
                return;
            }
            int choiceIndex = query.getIntValue("Class");
            String newClass = choiceClasses[choiceIndex];

            String currentRefinements
                = transition.refinementName.getExpression();
            if (currentRefinements == null || currentRefinements.equals("")) {
                currentRefinements = newName;
            } else {
                currentRefinements = currentRefinements.trim() + ", " + newName;
            }
            String moml;
            // The MoML we create depends on whether the configuration
            // specified a set of prototype refinements.
            if (refinements instanceof CompositeEntity) {
                String choiceName = choiceNames[choiceIndex];
                Entity template = ((CompositeEntity)refinements)
                    .getEntity(choiceName);
                String templateDescription = template.exportMoML(newName);
                moml = "<group>"
                    + templateDescription
                    + "<relation name=\""
                    + transition.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + currentRefinements
                    + "\"/></relation></group>";
            } else {
                moml = "<group><entity name=\""
                    + newName
                    + "\" class=\""
                    + newClass
                    + "\"/>"
                    + "<relation name=\""
                    + transition.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + currentRefinements
                    + "\"/></relation></group>";
            }
            MoMLChangeRequest change = new MoMLChangeRequest(
                    this, container, moml)  {
                    protected void _execute() throws Exception {
                        super._execute();

                        // Mirror the ports of the container in the refinement.
                        // Note that this is done here rather than as part of
                        // the MoML because we have set protected variables
                        // in the refinement to prevent it from trying to again
                        // mirror the changes in the container.
                        Entity entity = container.getEntity(newName);

                        // Get the initial port configuration from the container.
                        Iterator ports = container.portList().iterator();
                        while (ports.hasNext()) {
                            Port port = (Port)ports.next();
                            try {
                                // NOTE: This is awkward.
                                if (entity instanceof Refinement) {
                                    ((Refinement)entity).setMirrorDisable(true);
                                } else if (entity instanceof ModalController) {
                                    ((ModalController)entity).setMirrorDisable(true);
                                }
                                Port newPort = entity.newPort(port.getName());
                                if (newPort instanceof RefinementPort
                                        && port instanceof IOPort) {
                                    try {
                                        ((RefinementPort)newPort)
                                            .setMirrorDisable(true);
                                        if (((IOPort)port).isInput()) {
                                            ((RefinementPort)newPort)
                                                .setInput(true);
                                        }
                                        if (((IOPort)port).isOutput()) {
                                            ((RefinementPort)newPort)
                                                .setOutput(true);
                                        }
                                        if (((IOPort)port).isMultiport()) {
                                            ((RefinementPort)newPort)
                                                .setMultiport(true);
                                        }
                                        /* No longer needed since Yuhong modified
                                         * the type system to allow UNKNOWN. EAL
                                         if (port instanceof TypedIOPort
                                         && newPort instanceof TypedIOPort) {
                                         ((TypedIOPort)newPort).setTypeSameAs(
                                         (TypedIOPort)port);
                                         }
                                        */
                                    } finally {
                                        ((RefinementPort)newPort)
                                            .setMirrorDisable(false);
                                    }
                                }
                            } finally {
                                // NOTE: This is awkward.
                                if (entity instanceof Refinement) {
                                    ((Refinement)entity).setMirrorDisable(false);
                                } else if (entity instanceof ModalController) {
                                    ((ModalController)entity)
                                        .setMirrorDisable(false);
                                }
                            }
                        }
                        if (_configuration != null) {
                            // Look inside.
                            _configuration.openModel(entity);
                        }
                    }
                };
            container.requestChange(change);
        }
    }

    /** Action to remove refinements. */
    private class RemoveRefinementAction extends FigureAction {
        public RemoveRefinementAction() {
            super("Remove Refinement");
        }
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            NamedObj target = getTarget();
            if (!(target instanceof Transition)) {
                MessageHandler.error(
                        "Can only remove refinements from transitions.");
                return;
            }
            Transition transition = (Transition)target;

            // Check that all these containers exist.
            CompositeEntity immediateContainer = (CompositeEntity)
                transition.getContainer();
            if (immediateContainer == null) {
                MessageHandler.error("Transition has no container!");
                return;
            }
            final CompositeEntity container
                = (CompositeEntity)immediateContainer.getContainer();
            if (container == null) {
                MessageHandler.error("Transition container has no container!");
                return;
            }

            TypedActor[] refinements;
            try {
                refinements = transition.getRefinement();
            } catch (Exception ex) {
                MessageHandler.error("Invalid refinements.", ex);
                return;
            }
            if (refinements == null || refinements.length < 1) {
                MessageHandler.error("No refinements to remove.");
                return;
            }
            String[] choices = new String[refinements.length];
            for (int i = 0; i<refinements.length; i++) {
                choices[i] = ((Nameable)refinements[i]).getName();
            }

            // Open a dialog to get the refinement name and class.
            Query query = new Query();
            query.addChoice("Refinement", "Refinement", choices,
                    choices[0], false);
            // FIXME: Need a frame owner for first arg.
            // Perhaps calling getController(), which returns a GraphController
            // will be a good start.
            ComponentDialog dialog = new ComponentDialog(null,
                    "Specify Refinement", query);
            if (!dialog.buttonPressed().equals("OK")) {
                return;
            }

            String refinementName = query.getStringValue("Refinement");
            StringBuffer newRefinements = new StringBuffer();
            String currentRefinements
                = transition.refinementName.getExpression();
            StringTokenizer tokenizer
                = new StringTokenizer(currentRefinements, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (!token.trim().equals(refinementName)) {
                    if (newRefinements.length() > 0) {
                        newRefinements.append(", ");
                    }
                    newRefinements.append(token.trim());
                }
            }
            // Check to see whether any other transition has
            // this refinment, and if not, remove it from its container.
            Iterator transitions =
                immediateContainer.relationList().iterator();
            boolean foundOne = false;
            while (transitions.hasNext()) {
                NamedObj other = (NamedObj)transitions.next();
                if (other != transition && other instanceof Transition) {
                    String refinementList = ((Transition)other)
                        .refinementName.getExpression();
                    if (refinementList == null) continue;
                    tokenizer = new StringTokenizer(refinementList, ",");
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        if (token.equals(refinementName)) {
                            foundOne = true;
                            break;
                        }
                    }
                    if (foundOne) break;
                }
            }
            if (!foundOne) {
                Iterator states =
                    immediateContainer.entityList().iterator();
                while (states.hasNext()) {
                    NamedObj other = (NamedObj)states.next();
                    if (other instanceof State) {
                        String refinementList = ((State)other)
                            .refinementName.getExpression();
                        if (refinementList == null) continue;
                        tokenizer = new StringTokenizer(refinementList, ",");
                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();
                            if (token.equals(refinementName)) {
                                foundOne = true;
                                break;
                            }
                        }
                        if (foundOne) break;
                    }
                }
            }
            String removal = "";
            if (!foundOne) {
                removal = "<deleteEntity name=\"" + refinementName + "\"/>";
            }

            String moml = "<group><relation name=\""
                + transition.getName(container)
                + "\"><property name=\"refinementName\" value=\""
                + newRefinements.toString()
                + "\"/></relation>"
                + removal
                + "</group>";
            MoMLChangeRequest change = new MoMLChangeRequest(
                    this, container, moml);
            container.requestChange(change);
        }
    }
}
