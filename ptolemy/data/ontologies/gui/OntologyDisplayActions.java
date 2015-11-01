/* GUI element that provides context menus for an OntologySolver.

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
package ptolemy.data.ontologies.gui;

import java.awt.event.ActionEvent;

import ptolemy.data.ontologies.OntologySolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.MoMLModelAttributeController;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// OntologyDisplayActions

/** GUI element that provides context menus for an OntologySolver.
 *  This UI will be invoked when you right click on the
 *  OntologySolver.
 *
 *  @author Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
public class OntologyDisplayActions extends NodeControllerFactory {

    /** Construct an OntologyDisplayActions object with the specified container
     *  and name.
     *  @param container The container which should be an OntologySolver object.
     *  @param name The name of the OntologyDisplayActions object.
     *  @exception IllegalActionException If the OntologyDisplayActions object
     *   cannot be created.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public OntologyDisplayActions(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new node controller.  This base class returns an
     *  instance of IconController.  Derived
     *  classes can return some other class to customize the
     *  context menu.
     *  @param controller The associated graph controller.
     *  @return A new node controller.
     */
    @Override
    public NamedObjController create(GraphController controller) {
        super.create(controller);
        //return new ConfigureHighlightController(controller);
        // FIXME: This should not create a new one each time...
        return new HighlighterController(this, controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected inner classes               ////

    /** The controller that adds commands to the context menu.
     */
    protected static class HighlighterController extends
    MoMLModelAttributeController {

        /** Create a HighlighterController that is associated with a controller.
         *  @param displayActions The OntologyDisplayActions object reference.
         *  @param controller The controller.
         */
        public HighlighterController(OntologyDisplayActions displayActions,
                GraphController controller) {
            super(controller);

            ClearResolution clearResolution = displayActions.new ClearResolution();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    clearResolution));

            ResolveConcepts resolveConcepts = displayActions.new ResolveConcepts();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    resolveConcepts));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    /** The action for the clear concept resolution command to be added to the
     *  context menu.  This clears the list of resolved concepts (if any)
     *  and also clears the display.
     */
    @SuppressWarnings("serial")
    private class ClearResolution extends FigureAction {

        /** Create a new ClearResolution object to be added to the
         *  OntologySolver's context menu.
         */
        public ClearResolution() {
            super("Clear Concepts");
        }

        /** Called when the gui "Clear Concepts" menu action is
         *  clicked. This method calls the OntologySolver's MoML
         *  handler to clear the concept display highlighting and
         *  annotations.
         *  @param e The action event that is passed in when the action
         *   is triggered.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj container = getContainer();
            if (container instanceof OntologySolver) {
                try {
                    ((OntologySolver) container).reset();
                    ((OntologySolver) container).getMoMLHandler().clearDisplay(
                            true, true);
                } catch (IllegalActionException e1) {
                    MessageHandler.error("Clearing concepts failed", e1);
                }
            }
        }
    }

    /** The action for the resolve concepts command to be added
     *  to the context menu.
     */
    @SuppressWarnings("serial")
    private class ResolveConcepts extends FigureAction {

        /** Create a new ResolveConcepts object to be added to the
         *  OntologySolver's context menu.
         */
        public ResolveConcepts() {
            super("Resolve Concepts");
        }

        /** Called when the gui "Resolve Concepts" menu action is
         *  clicked. This method calls the OntologySolver's MoML
         *  handler to invoke the solver and perform the ontology solver
         *  concept resolution.
         *  @param e The action event that is passed in when the action
         *   is triggered.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj container = getContainer();
            if (container instanceof OntologySolver) {
                try {
                    ((OntologySolver) container).invokeSolver();
                } catch (IllegalActionException e1) {
                    MessageHandler.error("Cannot invoke solver.", e1);
                }
            }
        }
    }
}
