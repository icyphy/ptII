/* A timed actor that outputs the current time .

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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.Director;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CurrentTime
/**
Produce an output token on each firing with a value that is
the current time. The output is of type double.

@author Jie Liu and Edward A. Lee
@version $Id$
@since Ptolemy II 0.3
*/

public class CurrentTime extends TimedSource {

    /** Construct an actor with the given container and name.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CurrentTime(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // set the type constraints.
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the current time to the output.  If there are trigger inputs,
     *  then the current time is the minimum of the times of each of the
     *  input tokens (currently, these can be different only in the DT
     *  domain).  Otherwise, current time is that reported by the director.
     *  @exception IllegalActionException If send() throws it.
     */
    public void fire() throws IllegalActionException {
        // For domain polymorphism getCurrentTime(channel_number) has
        // to be called before get(channel_number).  Currently,
        // the only domain in which the two versions of getCurrentTime
        // are different is in DT... getCurrentTime() on the director
        // returns the "start of iteration" time, whereas getCurrentTime()
        // on the channel returns the time of the current sample.
        double currentTime = Double.MAX_VALUE;
        if (trigger.getWidth() > 0) {
            // Trigger port is connected.
            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.hasToken(i)) {
                    currentTime = Math.min(
                            trigger.getCurrentTime(i), currentTime);
                    // Do not consume the token... It will be consumed
                    // in the superclass fire().
                    // trigger.get(i);
                }
            }
        }
        // If current time is still the max value, then we were not
        // successful inferring current time from the inputs. Get
        // if from the director.
        if (currentTime == Double.MAX_VALUE) {
            Director director = getDirector();
            currentTime = director.getCurrentTime();
        }
        super.fire();
        output.send(0, new DoubleToken(currentTime));
    }
}
