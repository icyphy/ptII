package ptolemy.domains.metroII.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.util.IllegalActionException;

public class MetroIIPNQueueReceiver extends PNQueueReceiver {

    /** The director in charge of this receiver. */
    protected MetroIIPNDirector _director;

    public MetroIIPNDirector getDirector() {
        return _director;
    }

    public Token get() {
        Token t = super.get();
        try {
            _director.proposeMetroIIEvent(".get.end");
        } catch (InterruptedException e) {
            _terminate = true;
        }
        if (_terminate) {
            throw new TerminateProcessException("Interrupted when proposing MetroII events.");
        }
        return t;
    }

    public void put(Token token) {
        try {
            _director.proposeMetroIIEvent(".put.begin");
        } catch (InterruptedException e) {
            _terminate = true;
        }
        if (_terminate) {
            throw new TerminateProcessException("Interrupted when proposing MetroII events.");
        }

        super.put(token);
    }

    public void setContainer(IOPort port) throws IllegalActionException {
        super.setContainer(port);
        if (port == null) {
            _director = null;
        } else {
            Actor actor = (Actor) port.getContainer();
            Director director;

            // For a composite actor,
            // the receiver type of an input port is decided by
            // the executive director.
            // While the receiver type of an output is decided by the director.
            // NOTE: getExecutiveDirector() and getDirector() yield the same
            // result for actors that do not contain directors.
            if (port.isInput()) {
                director = actor.getExecutiveDirector();
            } else {
                director = actor.getDirector();
            }

            if (!(director instanceof MetroIIPNDirector)) {
                throw new IllegalActionException(port,
                        "Cannot use an instance of PNQueueReceiver "
                                + "since the director is not a PNDirector.");
            }

            _director = (MetroIIPNDirector) director;
        }
    }

}
