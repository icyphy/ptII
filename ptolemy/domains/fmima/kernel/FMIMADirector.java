/**
 * 
 */
package ptolemy.domains.fmima.kernel;

import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * @author fabio
 *
 */
public class FMIMADirector extends DEDirector {

    /**
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public FMIMADirector() throws IllegalActionException, NameDuplicationException {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param workspace
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public FMIMADirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public FMIMADirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }
    
    /** Fire actors according to events in the event queue. The actual
     *  selecting which events to process is done in _fire(). _fire()
     *  will return whether the previous firing was successful. According
     *  to this information, it is decided whether _fire() should be called
     *  again in order to keep processing events. After each actor firing,
     *  book keeping procedures are called, to keep track of the current
     *  state of the scheduler. The model time of the next events are also
     *  checked to see if we have produced an event of smaller timestamp.
     *  @see #_fire
     *  @exception IllegalActionException If we couldn't process an event
     *  or if an event of smaller timestamp is found within the event queue.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("========= " + this.getName() + " director fires at "
                    + getModelTime() + "  with microstep as " + _microstep);
        }

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        while (true) {
            int result = _fire();
            assert result <= 1 && result >= -1;
            if (result == 1) {
                continue;
            } else if (result == -1) {
                _noActorToFire();
                return;
            } // else if 0, keep executing

            // after actor firing, the subclass may wish to perform some book keeping
            // procedures. However in this class the following method does nothing.
            _actorFired();

            if (!_checkForNextEvent()) {
                break;
            } // else keep executing in the current iteration
        } // Close the BIG while loop.

        // Since we are now actually stopping the firing, we can set this false.
        _stopFireRequested = false;

        if (_debugging) {
            _debug("DE director fired!");
        }
    }

}
