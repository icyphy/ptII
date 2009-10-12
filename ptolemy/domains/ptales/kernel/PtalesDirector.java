package ptolemy.domains.ptales.kernel;

import ptolemy.actor.Receiver;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PtalesDirector extends SDFDirector {

    public PtalesDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setScheduler(new PtalesScheduler(this, "PtalesScheduler"));
    }
    
    public Receiver newReceiver() {
        return new PtalesReceiver();
    }
}
