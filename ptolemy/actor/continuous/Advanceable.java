/* Interface for actors that control integration step sizes.

 Copyright (c) 1998-2013 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.continuous;

import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Advanceable

/**
 Interface for actors and directors that, unlike native Ptolemy II actors, do not
 proactively notify their director (using fireAt()) of events in the
 future that they might produce, but which may, nevertheless, require
 execution at some future time. An example of such actors are
 functional mockup units (FMUs) conforming with the functional mockup
 interface (FMI) standard version 2.0 or earlier.
 <p>
 A director that advances time and supports such actors should advance
 time for actors that implement this interface before deciding on the
 time advance for other actors.  That is, as part of choosing the step
 size by which to advance time, it should invoke the {@link #advance(Time, int)}
 method of this interface with its proposed time advance. If any such actor
 returns false, then it should query the actor using the methods of the
 base interface to refine the time step.
 <p>
 Note that this mechanism is much weaker than the native Ptolemy II
 method using fireAt().  In particular, this method is advancing time
 into the future <i>without providing inputs into the future</i>. That is,
 the actor is expected to advance time without any knowledge of its inputs
 during the proposed time interval. The actor has to make some assumptions
 about the inputs during that interval, and those assumptions may be later
 contradicted by the actual inputs provided by the environment.
 If such a contradiction occurs, then the actor may need to roll back
 to a previously stored state and redo some portion of the time step.
 As of version 2.0, the FMI standard does not require actors to implement
 such rollback.
 <p>
 Of the existing Ptolemy II directors, the only one that requires rollback in time
 is the ContinuousDirector. Moreover, in Ptolemy II, rollback is implementable
 by every actor that realizes the strict actor semantics, where the fire()
 method does not change the state of the actor (state changes in postfire()).
 However, actors that implement this interface may require rollback under
 <i>any director that advances time</i>.
 <p>
 As a consequence, there is no Ptolemy II director that can guarantee correct
 execution of arbitrary combinations of FMUs, unless the FMUs implement rollback.
 In particular, consider a model contains more than one actor that implements
 this interface. Suppose it has two. Then it has to advance time of one
 before the other.  Suppose it advances time of the first by 1.0 time unit,
 and the advance succeeds.  Then, suppose it tries to advance the other by
 1.0 time unit, and the advance fails. Suppose that second actor suggests
 a time advance of 0.5 time units, and at time <i>t</i> + 0.5, where <i>t</i>
 is current time, it produces an output that is an input to the other
 actor.  The other actor will now have an input that it could not possibly
 have assumed when it advanced its time to <i>t</i> + 1.0, so it will have
 to roll back to time <i>t</i> + 0.5.  If it is not capable of rollback,
 then there is no assurance that its execution is correct.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (eal)
 */
public interface Advanceable extends ContinuousStepSizeController {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Advance to the specified time.
     *  @param time The time to advance.
     *  @param microstep The microstep to advance.
     *  @return True if advancement to the specified time succeeds.
     *  @exception IllegalActionException If an error occurs advancing time.
     */
    public boolean advance(Time time, int microstep)
            throws IllegalActionException;
}
