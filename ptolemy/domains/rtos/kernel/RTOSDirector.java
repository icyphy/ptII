
package ptolemy.domains.rtos.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

import java.util.Iterator;

public class RTOSDirector extends DEDirector {

    public RTOSDirector() {
        super();
    }

    public RTOSDirector(Workspace workspace) {
        super(workspace);
    }

    public RTOSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public void setCurrentTime(double newTime) throws IllegalActionException {
        //_currentTime = newTime;
    }

    public void fire() throws IllegalActionException {
        _stopRequested = false;
        Actor actorToFire = _dequeueEvents();
        if (actorToFire == null) {
            // There is nothing more to do.
            if (_debugging) _debug("No more events on the event queue.");
            _noMoreActorsToFire = true;
            return;
        }
        if (_debugging) {
            _debug("Found actor to fire: "
                    + ((NamedObj)actorToFire).getFullName());
        }
        // It is possible that the next event to be processed is on
        // an inside receiver of an output port of an opaque composite
        // actor containing this director.  In this case, we simply
        // return, giving the outside domain a chance to react to
        // event.
        if (actorToFire == getContainer()) {
            return;
        }
        if (((Nameable)actorToFire).getContainer() == null) {
            if (_debugging) _debug(
                    "Actor has no container. Disabling actor.");
            _disableActor(actorToFire);
        } else {
            if (!actorToFire.prefire()) {
                if (_debugging) _debug("Prefire returned false.");
            } else {
                actorToFire.fire();
                if (!actorToFire.postfire()) {
                    if (_debugging) _debug("Postfire returned false:",
                           ((Nameable)actorToFire).getName());
                    // Actor requests that it not be fired again.
                    _disableActor(actorToFire);
                }
            }
        }
    }

    protected void _enqueueEvent(Actor actor, double time)
            throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;

        int depth = _getDepth(actor);
        if(_debugging) _debug("enqueue a pure event: ",
                ((NamedObj)actor).getName(),
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(actor, time, microstep, depth));
    }

    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time) throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;

        

        Actor destination = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destination);
        if(_debugging) _debug("enqueue event: to",
                receiver.getContainer().getName()+ " ("+token.toString()+") ",
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(receiver, token, time, microstep, depth));
    }

    public Receiver newReceiver() {
        if(_debugging) _debug("Creating new DE receiver.");
	return new RTOSReceiver();
    }
}
