package ptolemy.actor.gt.controller;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

public class PersistenceAttribute extends SingletonAttribute {

    public PersistenceAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    @Override
    public NamedObj clone(Workspace workspace)
            throws CloneNotSupportedException {
        PersistenceAttribute object =
            (PersistenceAttribute) super.clone(workspace);
        object._persistence = true;
        return object;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer;
        if (_oldPersistence != null
            && (oldContainer = getContainer()) != null) {
            oldContainer.setPersistent(_oldPersistence);
        }
        super.setContainer(container);
        if (container == null) {
            _oldPersistence = null;
        } else {
            _oldPersistence = container.isPersistent();
            container.setPersistent(_persistence);
        }
    }

    private Boolean _oldPersistence;

    private boolean _persistence = false;
}
