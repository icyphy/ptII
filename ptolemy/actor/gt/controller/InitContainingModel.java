package ptolemy.actor.gt.controller;

import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.domains.erg.kernel.ERGModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class InitContainingModel extends Init {

    public InitContainingModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    protected CompositeEntity _getInitialModel() throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ERGModalModel modalModel = (ERGModalModel) controller.getContainer();
        CompositeEntity containingModel =
            (CompositeEntity) modalModel.getContainer();
        return containingModel;
    }
}
