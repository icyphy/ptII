/* Produce the index of the largest of the inputs.

 Copyright (c) 2000 The Regents of the University of California.
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
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// MaxIndex
/**
Produce the index of the largest of the inputs.
This actor has one input port, which is multiport of type double
and one output port, which is not a multiport, and has type int.
The tokens on each channel of the input port will be
compared and an IntToken giving the channel number of the largest
one will be output.

@author Jeff Tsay and Edward A. Lee
@version $Id$
*/

public class MaxIndex extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MaxIndex(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        input.setMultiport(true);
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each channel of the input port
     *  and produce the channel number of the largest one.
     *  If none of the input channels has a token, do nothing.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        double maxValue = Double.NEGATIVE_INFINITY;
        int maxIndex = -1;
        boolean foundFirst = false;
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                double val = ((DoubleToken) input.get(i)).doubleValue();
                if (foundFirst) {
                    if (maxValue < val) {
                        maxValue = val;
                        maxIndex = i;
                    }
                } else {
                    maxValue = val;
                    maxIndex = i;
                    foundFirst = true;
                }
            }
        }
        if (foundFirst) {
            output.send(0, new IntToken(maxIndex));       
        }
    }
}
