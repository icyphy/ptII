package ptolemy.domains.ptides.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class ActuatorTransmitter extends EnvironmentTransmitter {
    public ActuatorTransmitter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
}
