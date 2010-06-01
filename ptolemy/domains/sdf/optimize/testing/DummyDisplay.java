package ptolemy.domains.sdf.optimize.testing;

import ptolemy.actor.lib.Sink;
import ptolemy.domains.sdf.optimize.BufferingProfile;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class DummyDisplay extends Sink implements BufferingProfile {

    public DummyDisplay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public int iterate(int iterationCount, boolean fireExclusive) throws IllegalActionException {
        return super.iterate(iterationCount);
    }

    public int sharedBuffers() {
        return 0;
    }

    public int exclusiveBuffers() {
        return 0;
    }

    public int sharedExecutionTime() {
        return 1;
    }
    public int exclusiveExecutionTime() {
        return 1;
    }


}
