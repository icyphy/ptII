/*
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009-2010 The Regents of the University of California. All rights
 * reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.vergil.ontologies;

import java.awt.event.ActionEvent;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.toolbox.FigureAction;
import diva.graph.JGraph;

/**
 * A Graph Controller for lattice graphs. This controller allows lattice
 * elements to be dragged and dropped onto its graph. Arcs can be created by
 * control-clicking and dragging from one element to another.
 * 
 * @author Man-Kit Leung
 * @version $Id: LatticeGraphFrameController.java 53955 2009-05-27 20:49:38Z
 * cshelton $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class OntologyGraphController extends FSMGraphController {
    /**
     * Create a new basic controller with default terminal and edge interactors.
     */
    public OntologyGraphController() {
        super();

        // FIXME: Need to impose constraints to prevent self loops.
        // Presumably this means overriding LinkCreator in the base class.

        // FIXME: Need to override the NewStateAction of the base class
        // to create a Concept rather than a State.

        // FIXME: Having this action is only temporary.
        //        _menuFactory.addMenuItemFactory(
        //                new MenuActionFactory(_checkIsLatticeAction));
    }

    /**
     * Add hot keys to the actions in the given JGraph.
     * 
     * @param jgraph The JGraph to which hot keys are to be added.
     */
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);

        _stateController.addHotKeys(jgraph);
    }

    protected void _createControllers() {
        _attributeController = new AttributeController(this,
                AttributeController.FULL);
        _portController = new ExternalIOPortController(this,
                AttributeController.FULL);
        _stateController = new ConceptController(this, AttributeController.FULL);
        _transitionController = new RelationController(this);
        _modalTransitionController = _transitionController;
    }

    /**
     * The action for checking whether the graph is a lattice.
     */
    protected CheckIsLatticeAction _checkIsLatticeAction = new CheckIsLatticeAction(this);

    /** An action that checks whether the ontology model graph is a valid lattice.
     *  If the check is successful, the user is given an OK message. If not,
     *  the user is given a message showing a counterexample that shows why the
     *  graph is not a lattice, and the relevant nodes are highlighted in the model.
     */
    protected class CheckIsLatticeAction extends FigureAction {
        public CheckIsLatticeAction(OntologyGraphController graphController) {
            super("Check Lattice Graph");
            _graphController = graphController;
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj target = getTarget();
            Ontology ontologyModel = (Ontology) target.getContainer();
            ReportOntologyLatticeStatus.showStatusAndHighlightCounterExample(
                    ontologyModel, _graphController);
        }
        
        private OntologyGraphController _graphController;
    }

}
