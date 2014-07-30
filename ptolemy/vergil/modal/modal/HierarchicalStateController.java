/* The node controller for hierarchical states.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.modal.modal;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.domains.modal.kernel.RefinementActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.domains.modal.modal.RefinementExtender;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.StateController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// HierarchicalStateController

/**
 This class provides interaction with nodes that represent hierarchical
 states in an FSM graph. Hierarchical states are those with refinements,
 and what this adds to the base class is the ability to add a new
 refinement or remove a refinement via a context menu command.
 The base class provides a double click binding to edit the parameters
 of the state, and context menu commands to edit parameters ("Configure"),
 rename, look inside, and get documentation.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class HierarchicalStateController extends StateController {

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public HierarchicalStateController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public HierarchicalStateController(GraphController controller, Access access) {
        super(controller, access);

        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                _addRefinementAction));
        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                _removeRefinementAction));
        _menuFactory
        .addMenuItemFactory(new MenuActionFactory(_lookInsideAction));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to add a new refinement.
     */
    @SuppressWarnings("serial")
    public class AddRefinementAction extends FigureAction {

        public AddRefinementAction() {
            super("Add Refinement");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj target = getTarget();

            if (!(target instanceof State)) {
                MessageHandler.error("Can only add refinements to states.");
                return;
            }

            State state = (State) target;

            // Check that all these containers exist.
            Nameable immediateContainer = state.getContainer();

            if (immediateContainer == null) {
                MessageHandler.error("State has no container!");
                return;
            }

            CompositeEntity container = (CompositeEntity) immediateContainer
                    .getContainer();

            if (container == null) {
                MessageHandler.error("State container has no container!");
                return;
            }

            // Open a dialog to get the refinement name and class.
            Query query = new Query();
            String defaultName = container.uniqueName(state.getName());
            query.addLine("Name", "Name", defaultName);

            Map refinementClasses;
            // See whether the configuration offers state refinements.
            Configuration configuration = ((FSMGraphController) getController())
                    .getConfiguration();
            Entity refinements = configuration.getEntity("_stateRefinements");
            // Check the configuration to see whether the default is overridden.
            if (refinements instanceof CompositeEntity) {
                // There is a specification.
                refinementClasses = new HashMap();
                List refinementList = ((CompositeEntity) refinements)
                        .entityList();
                Iterator iterator = refinementList.iterator();
                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();
                    refinementClasses.put(entity.getName(), entity.getClass()
                            .getName());
                }
            } else {
                refinementClasses = getRefinementClasses();
            }

            // If refinement extenders are defined, add the classes specified in
            // them to the list of choices.
            List<RefinementExtender> extenders = state
                    .attributeList(RefinementExtender.class);
            String firstExtenderDescription = null;
            for (RefinementExtender extender : extenders) {
                try {
                    String description = extender.description.stringValue();
                    if (firstExtenderDescription == null) {
                        firstExtenderDescription = description;
                    }
                    refinementClasses.put(description,
                            extender.className.stringValue());
                } catch (IllegalActionException e1) {
                    // Ignore.
                }
            }

            String[] choiceNames = (String[]) refinementClasses.keySet()
                    .toArray(new String[refinementClasses.size()]);
            query.addChoice("Class", "Class", choiceNames, choiceNames[0], true);
            if (firstExtenderDescription != null) {
                query.set("Class", firstExtenderDescription);
            }

            // Need a frame owner for first arg. for the dialog constructor.
            Frame owner = null;
            GraphController controller = getController();
            if (controller instanceof BasicGraphController) {
                owner = ((BasicGraphController) controller).getFrame();
            }
            ComponentDialog dialog = new ComponentDialog(owner,
                    "Specify Refinement", query);

            if (!dialog.buttonPressed().equals("OK")) {
                return;
            }

            String newName = query.getStringValue("Name");

            if (container.getEntity(newName) != null) {
                MessageHandler.error("There is already a refinement with name "
                        + newName + ".");
                return;
            }

            String choiceName = query.getStringValue("Class");
            String newClass = (String) refinementClasses.get(choiceName);

            /*
            String currentRefinements = state.refinementName.getExpression();

            if ((currentRefinements == null) || currentRefinements.equals("")) {
                currentRefinements = newName;
            } else {
                currentRefinements = currentRefinements.trim() + ", " + newName;
            }
             */

            // The MoML we create depends on whether the configuration
            // specified a set of prototype refinements.
            Entity template = null;
            if (refinements instanceof CompositeEntity) {
                template = ((CompositeEntity) refinements)
                        .getEntity(choiceName);
            }

            try {
                ((RefinementActor) state.getContainer()).addRefinement(state,
                        newName, template, newClass, configuration);
            } catch (IllegalActionException ex) {
                MessageHandler.error(ex.getMessage());
            }
        }
    }

    /** Action to remove refinements. */
    @SuppressWarnings("serial")
    public static class RemoveRefinementAction extends FigureAction {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public RemoveRefinementAction() {
            super("Remove Refinement");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj target = getTarget();

            if (!(target instanceof State)) {
                MessageHandler
                .error("Can only remove refinements from states.");
                return;
            }

            State state = (State) target;

            // Check that all these containers exist.
            CompositeEntity immediateContainer = (CompositeEntity) state
                    .getContainer();

            if (immediateContainer == null) {
                MessageHandler.error("State has no container!");
                return;
            }

            final CompositeEntity container = (CompositeEntity) immediateContainer
                    .getContainer();

            if (container == null) {
                MessageHandler.error("State container has no container!");
                return;
            }

            TypedActor[] refinements;

            try {
                refinements = state.getRefinement();
            } catch (Exception ex) {
                MessageHandler.error("Invalid refinements.", ex);
                return;
            }

            if (refinements == null || refinements.length < 1) {
                MessageHandler.error("No refinements to remove.");
                return;
            }

            String[] choices = new String[refinements.length];

            for (int i = 0; i < refinements.length; i++) {
                choices[i] = ((Nameable) refinements[i]).getName();
            }

            // Open a dialog to get the refinement name and class.
            Query query = new Query();
            query.addChoice("Refinement", "Refinement", choices, choices[0],
                    false);

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
            String currentRefinements = state.refinementName.getExpression();
            StringTokenizer tokenizer = new StringTokenizer(currentRefinements,
                    ",");

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();

                if (!token.trim().equals(refinementName)) {
                    if (newRefinements.length() > 0) {
                        newRefinements.append(", ");
                    }

                    newRefinements.append(token.trim());
                }
            }

            // Check to see whether any other state or transition has
            // this refinment, and if not, remove it from its container.
            Iterator states = immediateContainer.entityList().iterator();
            boolean foundOne = false;

            while (states.hasNext()) {
                NamedObj other = (NamedObj) states.next();

                if (other != state && other instanceof State) {
                    String refinementList = ((State) other).refinementName
                            .getExpression();

                    if (refinementList == null) {
                        continue;
                    }

                    tokenizer = new StringTokenizer(refinementList, ",");

                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();

                        if (token.equals(refinementName)) {
                            foundOne = true;
                            break;
                        }
                    }

                    if (foundOne) {
                        break;
                    }
                }
            }

            if (!foundOne) {
                Iterator transitions = immediateContainer.relationList()
                        .iterator();

                while (transitions.hasNext()) {
                    NamedObj other = (NamedObj) transitions.next();

                    if (other instanceof Transition) {
                        String refinementList = ((Transition) other).refinementName
                                .getExpression();

                        if (refinementList == null) {
                            continue;
                        }

                        tokenizer = new StringTokenizer(refinementList, ",");

                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();

                            if (token.equals(refinementName)) {
                                foundOne = true;
                                break;
                            }
                        }

                        if (foundOne) {
                            break;
                        }
                    }
                }
            }

            String removal = "";

            if (!foundOne) {
                removal = "<deleteEntity name=\"" + refinementName + "\"/>";
            }

            String moml = "<group><entity name=\"" + state.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + newRefinements.toString() + "\"/></entity>" + removal
                    + "</group>";
            MoMLChangeRequest change = new MoMLChangeRequest(this, container,
                    moml);
            container.requestChange(change);
        }
    }

    /** Return a map with the keys as the names of the refinement types, and the
     *  values as the names of the classes that implement those refinement
     *  types.
     *  @return The map of supported refinement types.
     */
    protected Map getRefinementClasses() {
        Map map = new TreeMap();
        map.put("Default Refinement", "ptolemy.domains.modal.modal.Refinement");
        map.put("State Machine Refinement",
                "ptolemy.domains.modal.modal.ModalRefinement");
        return map;
    }

    /** The action that adds a refinement. */
    protected AddRefinementAction _addRefinementAction = new AddRefinementAction();

    /** The action that handles look inside.  This is accessed by
     *  by ActorViewerController to create a hot key for the editor.
     */
    protected LookInsideAction _lookInsideAction = new LookInsideAction();

    /** The action that removes a refinement. */
    protected RemoveRefinementAction _removeRefinementAction = new RemoveRefinementAction();
}
