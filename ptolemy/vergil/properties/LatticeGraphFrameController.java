/*
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009 The Regents of the University of California. All rights
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
package ptolemy.vergil.properties;

import java.awt.event.ActionEvent;

import ptolemy.domains.properties.PropertyLatticeComposite;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.TransitionController;
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
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeGraphFrameController extends FSMGraphController {
    /**
     * Create a new basic controller with default terminal and edge interactors.
     */
    public LatticeGraphFrameController() {
        super();

        // FIXME: Having this action is only temporary.
        //        _menuFactory.addMenuItemFactory(
        //                new MenuActionFactory(_checkIsLatticeAction));
    }

    /**
     * Add hot keys to the actions in the given JGraph.
     * 
     * @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);

        _stateController.addHotKeys(jgraph);
    }

    @Override
    protected void _createControllers() {
        _attributeController = new AttributeController(this,
                AttributeController.FULL);
        _portController = new ExternalIOPortController(this,
                AttributeController.FULL);
        _stateController = new LatticeElementController(this,
                AttributeController.FULL);
        _transitionController = new TransitionController(this);
        _modalTransitionController = _transitionController;
        //_transitionController.setEdgeRenderer(new BasicEdgeRenderer());
    }

    /**
     * The action for checking whether the graph is a lattice.
     */
    protected CheckIsLatticeAction _checkIsLatticeAction = new CheckIsLatticeAction();

    /**
     * An action that checks whether the model graph is a valid lattice. The
     * user is given an message upon the completion of the check.
     */
    protected static class CheckIsLatticeAction extends FigureAction {
        public CheckIsLatticeAction() {
            super("Check Lattice Graph");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj target = getTarget();

            boolean isLattice = ((PropertyLatticeComposite) target).isLattice();

            if (isLattice) {
                MessageHandler.message("This is good.");
            } else {
                MessageHandler.error("This is not a Lattice.");
            }
        }
    }

}
