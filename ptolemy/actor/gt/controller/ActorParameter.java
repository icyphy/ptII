package ptolemy.actor.gt.controller;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ObjectType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class ActorParameter extends Parameter {

    public ActorParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(new ObjectType(CompositeEntity.class));
    }
}
