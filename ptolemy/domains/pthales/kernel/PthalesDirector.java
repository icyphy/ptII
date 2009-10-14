package ptolemy.domains.pthales.kernel;

import ptolemy.actor.Receiver;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** 
 * FIXME: Should the director specify the order of dimensions?
 * If it doesn't, then the implicit raster order of unspecified
 * dimensions is hard to control.
 * 
 * FIXME: Need to export production and consumption data for
 * SDF, allowing these Pthales models to be nested within SDF
 * or within Pthales, which will also allow it to be nested
 * within modal models.
 * 
 * @author eal
 *
 */
public class PthalesDirector extends SDFDirector {

    public PthalesDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setScheduler(new PthalesScheduler(this, "PtalesScheduler"));
    }
    
    public Receiver newReceiver() {
        return new PthalesReceiver();
    }
}
