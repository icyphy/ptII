package ptolemy.actor.gt.controller;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class ModelAttribute extends Attribute {

    public ModelAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public CompositeEntity getModel() {
        return _model;
    }

    public void setModel(CompositeEntity model) {
        _model = model;
    }

    private CompositeEntity _model;
}
