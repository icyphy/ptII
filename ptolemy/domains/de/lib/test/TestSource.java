/* A source for testing iterations and microsteps.
 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.Director;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
This actor fires itself five times at each time instant,
then repeats the cycle one time unit later.  It outputs a ramp,
starting at zero and incrementing by one each time.
If there is an input, it adds the input to the ramp output.
This actor is designed to test two features of the DE scheduler.
First, that an iteration processes all events of a given time
stamp.  And second, that self-scheduling events are processed
in the proper order, with proper priorities.  To do these
tests, connect two of these actors in cascade.
*/
public class TestSource extends TypedAtomicActor {

    public TestSource(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    public TypedIOPort input;
    public TypedIOPort output;

    // NOTE: No clone() method, so don't clone this.

    public void fire() throws IllegalActionException {
        double increment = 0.0;
        if (input.getWidth() > 0 && input.hasToken(0)) {
            DoubleToken in = (DoubleToken)input.get(0);
            increment = in.doubleValue();
        }
        output.broadcast(new DoubleToken(value + increment));
        value += 1.0;
        Director director = getDirector();
        double time = director.getCurrentTime();
        count++;
        if (count >= 5) {
            director.fireAt(this, time + 1.0);
            count = 0;
        } else {
            director.fireAt(this, time);
        }
    }

    public void initialize() throws IllegalActionException {
        value = 0.0;
        count = 0;
        Director director = getDirector();
        director.fireAt(this, director.getCurrentTime());
    }

    private double value = 0.0;
    private int count = 0;
}
