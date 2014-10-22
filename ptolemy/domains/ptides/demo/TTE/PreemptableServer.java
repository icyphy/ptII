/* A server where the send time can be infinity.

@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

package ptolemy.domains.ptides.demo.TTE;

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.domains.de.lib.Server;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A server that has service time infinity until a new service time value is
 * received.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (derler)
 * @Pt.AcceptedRating Red (derler)
 */
public class PreemptableServer extends Server {

    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PreemptableServer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /**
     * Update service time and request a firing if the new service
     * time is not infinity.
     *
     * @exception IllegalActionException Thrown by the port
     *                associated with servicetime or in the fireAt()
     *                method.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        serviceTime.update();
        double serviceTimeValue = ((DoubleToken) serviceTime.getToken())
                .doubleValue();
        Time currentTime = getDirector().getModelTime();

        if (_nextTimeFree.equals(Time.NEGATIVE_INFINITY) && _queue.size() > 0) {
            _nextTimeFree = currentTime.add(serviceTimeValue);
            _fireAt(_nextTimeFree);
        }

        if (_nextTimeFree.equals(Time.POSITIVE_INFINITY)
                && !Double.isInfinite(serviceTimeValue)) {
            _nextTimeFree = currentTime.add(serviceTimeValue);
            _fireAt(_nextTimeFree);
        }

        return !_stopRequested;
    }
}
