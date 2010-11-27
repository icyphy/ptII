/* Top level Ptides director to simulate physical environment.

@Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.domains.ptides.kernel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This director manipulates physical time on each of the PTIDES platforms.
 *  @see ptolemy.domains.de.kernel.DEDirector
 *
 *  @author Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class PtidesTopLevelDirector extends DEDirector {
    /** Construct a PtidesTopLevelDirector with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesTopLevelDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Uses the fireAt() method of the DE Director, compensating the fireAt
     *  time by subtracting the platform synchronization error associated with
     *  the PTIDES actor.
     *  @param actor  an Actor object.
     *  @param time   a Time object.
     *  @exception IllegalActionException if the super method throws it.
     *  @return Time of fireAt.
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        if (actor.getDirector() instanceof PtidesBasicDirector) {
            return super.fireAt(actor,
                    time.subtract(_ptidesPlatformSyncError.get(actor)));
        } else {
            return super.fireAt(actor, time);
        }
    }

    /** Return a simulated physical time, which is the current time plus the
     *  synchronization error of that particular PTIDES actor.
     *  @param ptidesActor  an Actor object.
     *  @return A time object that contains the value of the current time plus
     *  the synchronization error specific to the PTIDES actor.
     */
    public Time getSimulatedPhysicalTime(Actor ptidesActor) {
        return getModelTime().add(_ptidesPlatformSyncError.get(ptidesActor));
    }

    /** Preinitialize the PTIDES top level director. This will store all the actors
     *  as well as the synchronization error of each of the actors.
     */
    public void preinitialize() throws IllegalActionException {
        _ptidesPlatformSyncError = new HashMap<Actor, Double>();
        for (Actor actor : (List<Actor>) ((CompositeActor) getContainer())
                .deepEntityList()) {
            Director director = actor.getDirector();
            if (director instanceof PtidesBasicDirector) {
                _ptidesPlatformSyncError.put(actor, Double
                        .valueOf(((PtidesBasicDirector) director)
                                .getAssumedSynchronizationErrorBound()));
            }
        }
        super.preinitialize();
    }

    private Map<Actor, Double> _ptidesPlatformSyncError;
}
