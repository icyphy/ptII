/* GUI element that provides context menus for a ProductLatticeOntologySolver.

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

import java.awt.event.ActionEvent;
import java.util.List;

import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.ProductLatticeOntology;
import ptolemy.data.ontologies.lattice.ProductLatticeOntologyMoMLHandler;
import ptolemy.data.ontologies.lattice.ProductLatticeOntologySolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntologySolverDisplayActions

/** GUI element that provides context menus for a ProductLatticeOntologySolver.
 *  This UI will be invoked when you right click on the
 *  ProductLatticeOntologySolver.
 *  
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntologySolverDisplayActions extends OntologyDisplayActions {

    /** Construct a ProductLatticeOntologyDisplayActions object with the specified container
     *  and name.
     *  @param container The container which should be a ProductLatticeOntologySolver object.
     *  @param name The name of the ProductLatticeOntologyDisplayActions object.
     *  @exception IllegalActionException If the ProductLatticeOntologyDisplayActions object
     *   cannot be created.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ProductLatticeOntologySolverDisplayActions(NamedObj container, String name)
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
    public NamedObjController create(GraphController controller) {
        super.create(controller);
        return new ProductLatticeHighlighterController(this, controller);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected inner classes               ////
    
    /** The controller that adds commands to the context menu. This extends
     *  the HighlighterController in {@link OntologyDisplayActions} by adding
     *  a submenu to select which of the component ontologies in the product
     *  lattice ontology to use as a basis for highlighting the resolved concepts.
     */
    protected static class ProductLatticeHighlighterController extends HighlighterController {

        /** Create a HighlighterController that is associated with a controller.
         *  @param displayActions The ProductLatticeOntologySolverDisplayActions object reference.
         *  @param controller The controller.
         */
        public ProductLatticeHighlighterController(ProductLatticeOntologySolverDisplayActions displayActions, GraphController controller) {
            super(displayActions, controller);

            SetHighlightColorsAction[] highlightColorsActions = null;
            
            // Create a new SetHighlightColorsAction for each ontology contained
            // in the product lattice ontology.
            try {
                ProductLatticeOntology ontology = ((ProductLatticeOntologySolver) displayActions.
                        getContainer()).getOntology();
                List<Ontology> subOntologies = ontology.getLatticeOntologies();
                highlightColorsActions = new SetHighlightColorsAction[subOntologies.size() + 1];
                for (int i = 0; i < subOntologies.size(); i++) {
                    highlightColorsActions[i] = displayActions.new SetHighlightColorsAction(subOntologies.get(i));
                }
            } catch (IllegalActionException ex) {
                throw new IllegalStateException("Could not create the highlight " +
                		"colors menu actions for the ProductLatticeOntologySolver" + displayActions.
                                getContainer().getName(), ex);
            }
            // Also create a "None" option for when we want to show no colors.
            highlightColorsActions[highlightColorsActions.length - 1] = displayActions.new SetHighlightColorsAction(null);
            
            // Create the set highlight colors sub menu and add it to the context menu.
            SetHighlightColorsMenu highlightColorsMenu = displayActions.new SetHighlightColorsMenu(highlightColorsActions, "Set Highlight Colors");           
            _menuFactory.addMenuItemFactory(highlightColorsMenu);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////
    
    /** A highlight action command to be added to the "Set Highlight Colors"
     *  sub menu in the ProductLatticeOntologySolver's context menu. When
     *  clicked, the ProductLatticeOntologySolver will highlight
     *  its resolved concepts using the colors from the specified
     *  ontology that is a component of the product lattice ontology.
     */
    private class SetHighlightColorsAction extends FigureAction {

        /** Create a new SetHighlightColorsAction for the given ontology.
         *  @param highlightOntology The ontology for which this menu action is
         *   being created.
         */
        public SetHighlightColorsAction(Ontology highlightOntology) {
            super(highlightOntology == null ? "None" : highlightOntology.getName());            
            _highlightOntology = highlightOntology;
        }

        /** Called when the gui "Set Highlight Colors" menu subaction is
         *  clicked. This method calls the OntologySolver's MoML
         *  handler to set the highlight ontology and highlight the concepts based
         *  on the colors from that ontology.
         *  @param e The action event that is passed in when the action
         *   is triggered. 
         */
        public void actionPerformed(ActionEvent e) {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);

            ProductLatticeOntologyMoMLHandler target = (ProductLatticeOntologyMoMLHandler)
                ((ProductLatticeOntologySolver) ProductLatticeOntologySolverDisplayActions.this
                    .getContainer()).getMoMLHandler();
            try {
                target.setHighlightOntology(_highlightOntology);
                target.highlightConcepts();
            } catch (IllegalActionException ex) {
                throw new IllegalStateException("Could not highlight " +
                		"the concept colors for the ontology " +
                		_highlightOntology.getName() + ".", ex);
            }
        }
        
        /** The ontology from which to get highlight colots for this menu action. */
        Ontology _highlightOntology;
    }
    
    /** The class that represents the submenu for the "Set Highlight Colors"
     *  option in the ProductLatticeOntologySolver's context menu. It contains
     *  all the actions that allow the user to specify which of the product
     *  lattice ontology's component ontologies to use for highlighy colors.
     */    
    private class SetHighlightColorsMenu extends MenuActionFactory {
        
        /** Create a new SetHighlightColorsMenu object with the given name
         *  and SetHighlightColors actions.
         *  @param actions The array of menu actions for the "Set Highlight Colors"
         *   submenu that represent the different ontologies contained in the
         *   product lattice ontology for which the user can specify which
         *   colors to use.
         *  @param name The name of the SetHighlightColorsMenu object.
         */        
        public SetHighlightColorsMenu(SetHighlightColorsAction[] actions, String name) {
            super(actions, name);
        }
    }
}
