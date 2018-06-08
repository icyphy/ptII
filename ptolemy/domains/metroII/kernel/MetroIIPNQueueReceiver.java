/* MetroIIPNQueueReceiver adapts token transfer to MetroII semantics.

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.domains.metroII.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MetroIIPNQueueReceiver

/**
 * <p>
 * MetroIIPNQueueReceiver adapts token transfer to MetroII semantics. Each get()
 * or put() is associated with two MetroII events. The data token transfer will
 * not occur until the associated MetroII events are NOTIFIED.
 * </p>
 *
 * <p>
 * The implementation is obsolete and needs to be updated.
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 */
public class MetroIIPNQueueReceiver extends PNQueueReceiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Returns the director in charge.
     *
     * @return the director in charge.
     */
    @Override
    public MetroIIPNDirector getDirector() {
        return _localDirector;
    }

    /**
     * Gets a token from this receiver. If the receiver is empty then block until
     * a token becomes available. If this receiver is terminated during the
     * execution of this method, then throw a TerminateProcessException.
     *
     * The method will not return until the 'xxx.get.end' MetroII event is
     * NOTIFIED.
     *
     * @return The token contained by this receiver.
     */
    @Override
    public Token get() {
        Token token = super.get();
        try {
            _localDirector.proposeMetroIIEvent(".get.end");
        } catch (InterruptedException e) {
            _terminate = true;
        }
        if (_terminate) {
            throw new TerminateProcessException(
                    "Interrupted when proposing MetroII events.");
        }
        return token;
    }

    /**
     * Puts a token on the queue contained in this receiver. The 'put' will not
     * occur until the associated MetroII event '*.put.begin' is NOTIFIED.
     *
     * @param token
     *            The token to be put in the receiver, or null to not put
     *            anything.
     * @exception NoRoomException
     *                If during initialization, capacity cannot be increased
     *                enough to accommodate initial tokens.
     */
    @Override
    public void put(Token token) {
        try {
            _localDirector.proposeMetroIIEvent(".put.begin");
        } catch (InterruptedException e) {
            _terminate = true;
        }
        if (_terminate) {
            throw new TerminateProcessException(
                    "Interrupted when proposing MetroII events.");
        }

        super.put(token);
    }

    /**
     * Sets the container. This overrides the base class to record the director.
     *
     * @param port
     *            The container.
     * @exception IllegalActionException
     *                If the container is not of an appropriate subclass of
     *                IOPort, or if the container's director is not an instance
     *                of PNDirector.
     */
    @Override
    public void setContainer(IOPort port) throws IllegalActionException {
        super.setContainer(port);
        if (port == null) {
            _localDirector = null;
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

            _localDirector = (MetroIIPNDirector) director;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * The director in charge of this receiver.
     */
    protected MetroIIPNDirector _localDirector;

}
