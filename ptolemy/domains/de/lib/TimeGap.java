/* Measure the time interval between input events.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TimeGap

/**
 This actor measures the time interval between arrivals of successive
 input tokens.
 Beginning with the second input arrival, the measurement is produced
 at the output.  The output is always a DoubleToken.
 @see WaitingTime

 @author Jie Liu, Edward A Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class TimeGap extends DETransformer {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimeGap(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Beginning with the second input, produce an output that is
     *  the elapsed time since the previous input is received.  When the
     *  first input is received, nothing is produced.  This method
     *  consumes at most one token from the input each time it is fired.
     *  @exception IllegalActionException If get() or send() throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // Consume an input.
        if (input.hasToken(0)) {
            input.get(0);
        }

        Time currentTime = getDirector().getModelTime();

        if (_previousTime.compareTo(Time.NEGATIVE_INFINITY) != 0) {
            DoubleToken outToken = new DoubleToken(currentTime.subtract(
                    _previousTime).getDoubleValue());
            output.send(0, outToken);
        }
    }

    /** Set the previous event time to negative infinity.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _previousTime = Time.NEGATIVE_INFINITY;
        super.initialize();
    }

    /** Record the time when the current input arrives.
     *  @return True to continue firing.
     *  @exception IllegalActionException If there is no director or thrown
     *  in the super class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _previousTime = getDirector().getModelTime();
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The time when the previous input arrives.
    private Time _previousTime;
}
