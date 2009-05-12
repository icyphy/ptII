/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.vergil.properties;

import java.awt.event.ActionEvent;

import ptolemy.domains.properties.PropertyLatticeComposite;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.TransitionController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import diva.graph.JGraph;



public class LatticeGraphFrameController extends FSMGraphController {
    /** Create a new basic controller with default
     *  terminal and edge interactors.
     */
    public LatticeGraphFrameController() {
        super();

        // FIXME: Having this action is only temporary.
//        _menuFactory.addMenuItemFactory(
//                new MenuActionFactory(_checkIsLatticeAction));
    }

    /** Add hot keys to the actions in the given JGraph.
     *
     *  @param jgraph The JGraph to which hot keys are to be added.
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
        _stateController = new LatticeElementController(this, AttributeController.FULL);
        _transitionController = new TransitionController(this);
        _modalTransitionController = _transitionController;
        //_transitionController.setEdgeRenderer(new BasicEdgeRenderer());
    }

    protected CheckIsLatticeAction _checkIsLatticeAction =
        new CheckIsLatticeAction();

    protected static class CheckIsLatticeAction extends FigureAction {
        public CheckIsLatticeAction() {
            super("Check Lattice");
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj target = getTarget();

            boolean isLattice = ((PropertyLatticeComposite)
                    target).isLattice();

            if (isLattice) {
                MessageHandler.message("This is good.");
            } else {
                MessageHandler.error("This is not a Lattice.");
            }
        }
    }

}
