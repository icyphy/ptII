/* Measure the time between input events.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TimeGap
/**
This actor measures the time between arrivals of successive input tokens.
Beginning with the second input arrival, the measurement is produced
at the output.  The output is always a DoubleToken.
@see WaitingTime

@author Jie Liu and Edward A Lee
@version $Id$
@since Ptolemy II 1.0
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
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Beginning with the second iteration, produce an output that is
     *  the elapsed time since the previous iteration.  On the first
     *  iteration, nothing is produced.  This method consumes at most
     *  one token from the input each time it is fired.
     *  @exception IllegalActionException If get() or send() throws it.
     */
    public void fire() throws IllegalActionException {
        // Consume an input.
        if (input.hasToken(0)) {
            input.get(0);
        }
        double currentTime = getDirector().getCurrentTime();
        if (_previousTime != Double.NEGATIVE_INFINITY) {
            DoubleToken outToken =
                new DoubleToken(currentTime-_previousTime);
            output.send(0, outToken);
        }
    }

    /** Record the time of the current iteration for use in the next.
     *  @return True to continue firing.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _previousTime = getDirector().getCurrentTime();
        return super.postfire();
    }

    /** Set the previous event time to -1.
     */
    public void initialize() throws IllegalActionException {
        _previousTime = Double.NEGATIVE_INFINITY;
        super.initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time observed in the previous iteration.
    private double _previousTime = Double.NEGATIVE_INFINITY;
}
