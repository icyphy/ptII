/* Generate discrete events at prespecified time instants.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.lib.Clock;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// DiscreteClock

/**
 This actor is a version of the Clock actor specialized for use in the
 continuous domain. It produces a periodic signal, a sequence of
 discrete events at  regularly spaced intervals, and at times
 between these events the output is absent. It differs from
 the Clock actor (its base class) only in  that when current time
 matches an event time, it does not produce an output until
 superdense time index one. That is, at index 0, its output is absent.
 If it produces multiple events at the same time (this can occur
 if <i>offsets</i> has repeated entries), then the outputs are
 produced at successive indexes that begin with one.
 See the base class documentation for a description of the behavior.
 <p>
 To produce a continuous-time clock signal, which is present
 at all times, use this DiscreteClock actor followed by a ZeroOrderHold.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class DiscreteClock extends Clock {

    // This actor only generates predictable events and that is why it does not
    // implement the ContinuousStepSizeControlActor interface. This actor requests a
    // refiring at its initialize method to produce events. During its postfire
    // method, it requests further firings to produce more events if necessary.

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public DiscreteClock(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////

    /** Override the base class to initialize the index.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it.
     */
    public synchronized void initialize() throws IllegalActionException {
        super.initialize();
        _nextOutputIndex = 1;
    }

    /** Override the base class to keep track of the index.
     *  @return False if the specified number of cycles has been reached,
     *   and true otherwise.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing, or if an offset value exceeds the period.
     */
    public boolean postfire() throws IllegalActionException {
        int previousPhase = _phase;
        boolean result = super.postfire();
        if (_outputProduced && (_offsets[previousPhase] == _offsets[_phase])) {
            // Duplicate offsets allow production of multiple events
            // at one time.
            _nextOutputIndex++;
        } else {
            _nextOutputIndex = 1;
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Return true if the current time is the right time for an output.
     *  @return True if the current time matches the _nextOutputTime.
     */
    protected boolean _isTimeForOutput() {
        FixedPointDirector director = (FixedPointDirector) getDirector();
        boolean rightIndex = _nextOutputIndex == director.getIndex();
        Time currentTime = director.getModelTime();
        return rightIndex && _tentativeNextOutputTime.equals(currentTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The following are all transient because they need not be cloned.
    // Either the clone method or the initialize() method sets them.

    /** The index of when the output should be emitted. */
    private transient int _nextOutputIndex;
}
