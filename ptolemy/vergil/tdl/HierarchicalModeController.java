/* A state controller associated with a graph controller.
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
package ptolemy.vergil.tdl;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.domains.modal.modal.RefinementPort;
import ptolemy.domains.tdl.kernel.TDLMode;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.StateController;
import ptolemy.vergil.modal.modal.HierarchicalStateController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// HierarchicalModeController

/** A state controller associated with a graph controller.

    @author Patricia Derler
    @version $Id$
    @since Ptolemy II 8.0
    @Pt.ProposedRating Red (eal)
    @Pt.AcceptedRating Red (eal)
 */
public class HierarchicalModeController extends StateController {
    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public HierarchicalModeController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public HierarchicalModeController(GraphController controller, Access access) {
        super(controller, access);

        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                new AddRefinementAction()));
        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                new HierarchicalStateController.RemoveRefinementAction()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to add a new refinement.
     */
    @SuppressWarnings("serial")
    private class AddRefinementAction extends FigureAction {
        public AddRefinementAction() {
            super("Add Refinement");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // This method is similar to a method in
            // ptolemy/vergil/modal/modal/HierarchicalStateController.java
            super.actionPerformed(e);

            NamedObj target = getTarget();

            if (!(target instanceof State)) {
                MessageHandler.error("Can only add refinements to states.");
                return;
            }

            TDLMode state = (TDLMode) target;

            // Check that all these containers exist.
            Nameable immediateContainer = state.getContainer();

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

            // Open a dialog to get the refinement name and class.
            Query query = new Query();
            String defaultName = container.uniqueName(state.getName());
            query.addLine("Name", "Name", defaultName);

            // See whether the configuration offers state refinements.
            Configuration configuration = ((FSMGraphController) getController())
                    .getConfiguration();
            Entity refinements = configuration.getEntity("_stateRefinements");

            // Default choices.
            String[] choiceClasses = { "ptolemy.domains.tt.tdl.kernel.TDLRefinement" };
            String[] choiceNames = { "TDL Refinement" };

            // Check the configuration to see whether the default is overridden.
            if (refinements instanceof CompositeEntity) {
                // There is a specification.
                List refinementList = ((CompositeEntity) refinements)
                        .entityList();
                choiceNames = new String[refinementList.size()];
                choiceClasses = new String[refinementList.size()];

                Iterator iterator = refinementList.iterator();
                int count = 0;

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();
                    choiceNames[count] = entity.getName();
                    choiceClasses[count++] = entity.getClass().getName();
                }
            }

            query.addChoice("Class", "Class", choiceNames, choiceNames[0], true);

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

            final String newName = query.getStringValue("Name");

            if (container.getEntity(newName) != null) {
                MessageHandler.error("There is already a refinement with name "
                        + newName + ".");
                return;
            }

            int choiceIndex = query.getIntValue("Class");
            String newClass = choiceClasses[choiceIndex];

            String currentRefinements = state.refinementName.getExpression();

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
                Entity template = ((CompositeEntity) refinements)
                        .getEntity(choiceName);
                String templateDescription = template.exportMoML(newName);
                moml = "<group>" + templateDescription + "<entity name=\""
                        + state.getName(container)
                        + "\"><property name=\"refinementName\" value=\""
                        + currentRefinements + "\"/></entity></group>";
            } else {
                moml = "<group><entity name=\"" + newName + "\" class=\""
                        + newClass + "\"/>" + "<entity name=\""
                        + state.getName(container)
                        + "\"><property name=\"refinementName\" value=\""
                        + currentRefinements + "\"/></entity></group>";
            }

            MoMLChangeRequest change = new MoMLChangeRequest(this, container,
                    moml) {
                @Override
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
                        Port port = (Port) ports.next();

                        try {
                            // NOTE: This is awkward.
                            if (entity instanceof Refinement) {
                                // FIXME: not sure if this should be -1 or 1.
                                ((Refinement) entity).setMirrorDisable(1);

                                ((Refinement) entity).setMirrorDisable(1);
                            } else if (entity instanceof ModalController) {
                                // FIXME: not sure if this should be -1 or 1.
                                ((ModalController) entity).setMirrorDisable(1);
                            }

                            Port newPort = entity.newPort(port.getName());

                            if (newPort instanceof RefinementPort
                                    && port instanceof IOPort) {
                                try {
                                    ((RefinementPort) newPort)
                                    .setMirrorDisable(true);

                                    if (((IOPort) port).isInput()) {
                                        ((RefinementPort) newPort)
                                        .setInput(true);
                                    }

                                    if (((IOPort) port).isOutput()) {
                                        ((RefinementPort) newPort)
                                        .setOutput(true);
                                    }

                                    if (((IOPort) port).isMultiport()) {
                                        ((RefinementPort) newPort)
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
                                    ((RefinementPort) newPort)
                                    .setMirrorDisable(false);
                                }
                            }
                        } finally {
                            // NOTE: This is awkward.
                            if (entity instanceof Refinement) {
                                ((Refinement) entity).setMirrorDisable(0);
                            } else if (entity instanceof ModalController) {
                                ((ModalController) entity).setMirrorDisable(0);
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
}
