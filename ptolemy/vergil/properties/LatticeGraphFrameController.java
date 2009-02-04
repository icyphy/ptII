package ptolemy.vergil.properties;

import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.fsm.TransitionController;
import ptolemy.vergil.kernel.AttributeController;
import diva.graph.basic.BasicEdgeRenderer;



public class LatticeGraphFrameController extends FSMGraphController {
    /** Create a new basic controller with default
     *  terminal and edge interactors.
     */
    public LatticeGraphFrameController() {
        super();
    }

    protected void _createControllers() {
        _attributeController = new AttributeController(this,
                AttributeController.FULL);
        _portController = new ExternalIOPortController(this,
                AttributeController.FULL);
        _stateController = new LatticeElementController(this, AttributeController.FULL);
        _transitionController = new TransitionController(this);
        _modalTransitionController = _transitionController;
        _transitionController.setEdgeRenderer(new BasicEdgeRenderer());
    }
}
