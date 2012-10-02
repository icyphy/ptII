/* Basic Preemptive EDF Ptides director that allows preemption, and uses EDF to determine whether preemption should occur.

@Copyright (c) 2008-2011 The Regents of the University of California.
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

import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This director implements preemptive PTIDES scheduling algorithm, and uses
 *  model timestamps to determine whether we should preempt executing events.
 *  Notice this does not implement EDF because the event queue is ordered in timestamp
 *  order but not deadline order. Also only the first event from the event queue
 *  is analyzed for safe to process.
 *
 *  @author Slobodan Matic, Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */
public class PtidesBasicPreemptiveDirector extends PtidesBasicDirector {

    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesBasicPreemptiveDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return whether we want to preempt the currently executing actor
     *  and instead execute the earliest event on the event queue.
     *  @return whether we want to preempt the executing event.
     *  @exception IllegalActionException
     */
    protected boolean _preemptExecutingActor() throws IllegalActionException {
        PtidesEvent event = (PtidesEvent) _eventQueue.get();
        if (event.isPureEvent()) {
            if (_debugging) {
                _debug("We decided to preempt the current "
                        + "executing event with a pure event at "
                        + event.actor());
            }
            // FIXME: for now, if the event is pure event, always preempt whatever that is
            // processing. This is because a pure event is caused by an input event, and
            // if the input event already processed, so should the pure event.
            return true;
        }
        PtidesEvent lastEvent = ((List<PtidesEvent>) _currentlyExecutingStack
                .peek().contents).get(0);
        // If last event has smaller or equal the timestamp of the new event,
        // do not preempt.
        if (lastEvent.compareTo(event) <= 0) {
            return false;
        }
        if (_debugging) {
            _debug("We decided to preempt the current " + "executing event: "
                    + lastEvent.toString() + " with another event: "
                    + event.toString()
                    + ". This preemption happened at platform execution "
                    + "physical time "
                    + getPlatformPhysicalTag(executionTimeClock).timestamp
                    + "."
                    + getPlatformPhysicalTag(executionTimeClock).microstep);
        }
        return true;
    }
}
