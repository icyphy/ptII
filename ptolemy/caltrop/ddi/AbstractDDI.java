package ptolemy.caltrop.ddi;

import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */

public abstract class AbstractDDI implements DDI {
    public int iterate(int i) throws IllegalActionException {
        return 0;
    }
    public void stop() {
    }

    public void stopFire() {
    }

    public void terminate() {
    }

    public void wrapup() throws IllegalActionException {
    }
}
