/* A timed actor that outputs the current time .

 Copyright (c) 1998-2001 The Regents of the University of California.
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

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

//////////////////////////////////////////////////////////////////////////
//// CurrentTime
/**
Produce an output token on each firing with a value that is
the current time. The output is of Type Double.

@author Jie Liu
@version $Id$
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

    /** Send the current value of the state of this actor to the output.
     *  @exception IllegalActionException If send() throws it.
     */
    public void fire() throws IllegalActionException {

        // For domain polymorphism getCurrentTime(channel_number) has
        // to be called before get(channel_number)

	// Interestingly, this method is very different from how
	// actor.gui.TimedPlotter does some thing very similar.

	// Edward wrote:
	// "The only domain in which [the] two versions of getCurrentTime
        // are different is in DT... getCurrentTime() on the director
        // returns the "start of iteration" time, whereas getCurrentTime()
        // on the channel returns the time of the current sample.
        // I think the getCurrentTime() actor should probably be calling
        // it on the channel..."

        double currentTime;
        if (trigger.getWidth() > 0) {
            currentTime = trigger.getCurrentTime(0);
        } else {
            Director director = getDirector();
            currentTime = director.getCurrentTime();
        }
        super.fire();
        output.send(0, new DoubleToken(currentTime));

    }
}
