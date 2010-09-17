/* An attribute that creates an editor to configure and run a code generator.

 Copyright (c) 2006-2010 The Regents of the University of California.
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

import java.awt.Frame;
import java.awt.event.ActionEvent;

import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.MoMLModelAttributeController;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// OntologyDisplayActions

/**
 This is an attribute that creates an editor for configuring and
 running a code generator.  This is designed to be contained by
 an instance of CodeGenerator or a subclass of CodeGenerator.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class OntologyDisplayActions extends NodeControllerFactory {

    /** Construct a PropertyHighlighter with the specified container and name.
     *  @param container The container.
     *  @param name The name of the PropertyHighlighter.
     *  @exception IllegalActionException If the PropertyHighlighter is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public OntologyDisplayActions(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Return a new node controller.  This base class returns an
     *  instance of IconController.  Derived
     *  classes can return some other class to customize the
     *  context menu.
     *  @param controller The associated graph controller.
     *  @return A new node controller.
     */
    public NamedObjController create(GraphController controller) {
        super.create(controller);
        //return new ConfigureHighlightController(controller);
        return new HighlighterController(this, controller);
    }
    
    /** The action for the clear concept resolution command to be added to the
     *  context menu.  This clears the list of resolved concepts (if any)
     *  and also clears the display.
     */
    private class ClearResolution extends FigureAction {
        public ClearResolution() {
            super("Clear Concepts");
        }
        
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            
            NamedObj container = getContainer();
            if (container instanceof OntologySolver) {
                try {
                    ((OntologySolver) container).reset();
                    ((OntologySolver) container).getMoMLHandler()
                    .clearDisplay();
                } catch (IllegalActionException e1) {
                 MessageHandler.error("Clearing concepts failed", e1);
                }
            }
        }
    }

    /** The action for the resolve concepts command to be added
     *  to the context menu.
     */
    private class ResolveConcepts extends FigureAction {
        public ResolveConcepts() {
            super("Resolve Concepts");
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj container = getContainer();
            if (container instanceof OntologySolver) {
                ((OntologySolver) container).getMoMLHandler().invokeSolver();
                }
        }
    }

    /** The controller that adds commands to the context menu.
     */
    protected static class HighlighterController extends MoMLModelAttributeController {

        /** Create a DependencyController that is associated with a controller.
         *  @param displayActions The OntologyDisplayActions object reference.
         *  @param controller The controller.
         */
        public HighlighterController(OntologyDisplayActions displayActions, GraphController controller) {
            super(controller);
            
            ClearResolution clearResolution = 
                displayActions.new ClearResolution();
            _menuFactory
                    .addMenuItemFactory(new MenuActionFactory(clearResolution));

            ResolveConcepts resolveConcepts = 
                displayActions.new ResolveConcepts();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    resolveConcepts));
        }
    }

}
