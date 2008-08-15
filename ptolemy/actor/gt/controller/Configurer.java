package ptolemy.actor.gt.controller;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class Configurer extends CompositeActor {

    public Configurer(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);

        new ContainmentExtender(this, "_containmentExtender");
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Configurer newObject = (Configurer) super.clone(workspace);
        newObject._configured = null;
        return newObject;
    }

    public NamedObj getConfiguredObject() {
        return _configured;
    }

    public void setConfiguredObject(NamedObj configured) {
        _configured = configured;
    }

    public class ContainmentExtender extends Attribute implements
    ptolemy.data.expr.ContainmentExtender {

        public ContainmentExtender(Configurer container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        public NamedObj getContainedObject(String name)
                throws IllegalActionException {
            return ((Configurer) getContainer()).getEntity(name);
        }

        public NamedObj getExtendedContainer() throws IllegalActionException {
            return ((Configurer) getContainer()).getConfiguredObject();
        }

    }

    private NamedObj _configured;
}
