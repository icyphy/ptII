package ptolemy.data.ontologies.gui;

import java.awt.event.ActionEvent;

import ptolemy.data.ontologies.lattice.DeltaConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

/* An attribute that creates options to configure and run actions of
 * the DeltaConstraintSolver.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

//// DeltaConstraintSolverDisplayActions

/**
 This is an attribute that creates options to configure and run actions of
 the DeltaConstraintSolver.  It extends OntologyDisplayActions to provide
 some additional actions specific to the DeltaConstraintSolver and subclasses.

 @author Beth Latronico
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ltrnc)
 @Pt.AcceptedRating Red (ltrnc)
 */

public class DeltaConstraintSolverDisplayActions extends OntologyDisplayActions {

    /** Construct a DeltaConstraintSolverDisplayActions controller with the
     *  specified container and name.
     *  @param container The container.
     *  @param name The name of the DeltaConstraintSolverDisplayActions.
     *  @exception IllegalActionException If this object is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DeltaConstraintSolverDisplayActions(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Return a new node controller which supports additional menu options.
     *  @param controller The associated graph controller.
     *  @return A new node controller.
     */
    @Override
    public NamedObjController create(GraphController controller) {
        super.create(controller);
        return new DeltaConstraintSolverHighlighterController(this, controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    /** The controller that adds commands to the context menu for the
     *  DeltaConstraintSolver ontology solver.
     */
    private static class DeltaConstraintSolverHighlighterController extends
    HighlighterController {

        /** Create a DeltaConstraintSolverHighlighterController that is associated with a controller.
         *  @param displayActions The OntologyDisplayActions object reference.
         *  @param controller The controller.
         */
        public DeltaConstraintSolverHighlighterController(
                DeltaConstraintSolverDisplayActions displayActions,
                GraphController controller) {
            super(displayActions, controller);

            HighlightConflicts highlightConflicts = displayActions.new HighlightConflicts();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    highlightConflicts));
        }
    }

    /** The action for the highlight conflicts command to be added
     *  to the context menu.
     */
    @SuppressWarnings("serial")
    private class HighlightConflicts extends FigureAction {

        /** Create a new ResolveConflicts object to be added to the
         *  OntologySolver's context menu.
         */
        public HighlightConflicts() {
            super("Resolve Conflicts");
        }

        /** Called when the gui "Resolve Conflicts" menu action is
         *  clicked. This method calls the OntologySolver's MoML
         *  handler to highlight conflicting concepts identified by
         *  the DeltaConstraintSolver.
         *  @param e The action event that is passed in when the action
         *   is triggered.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            NamedObj container = getContainer();

            // Get the set of objects to highlight from the
            // DeltaConstraintSolver and highlight them
            if (container instanceof DeltaConstraintSolver) {
                DeltaConstraintSolver solver = (DeltaConstraintSolver) container;

                // Invoke the solver to identify conflicts.
                // Catch and ignore any exceptions regarding unacceptable
                // concepts (finding these is the point of this solver)
                try {
                    // Call resolveConflicts, which identifies the key
                    // constraints that are causing the problem
                    solver.resolveConflicts();
                    if (solver.hasUnacceptableTerms()) {
                        solver.getMoMLHandler().clearDisplay(true, true);
                        solver.getMoMLHandler().highlightConcepts();
                        solver.getMoMLHandler().showConceptAnnotations();
                    }
                } catch (KernelException ex) {
                    MessageHandler.error(
                            "Identifying solver conflicts failed.", ex);
                }
            }
        }
    }
}
