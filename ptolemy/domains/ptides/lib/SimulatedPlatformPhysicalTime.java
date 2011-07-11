/* A timed actor that outputs the simulated physical time .

 Copyright (c) 1998-2011 The Regents of the University of California.
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
package ptolemy.domains.ptides.lib;

import ptolemy.actor.lib.TimedSource;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SimulatedPlatformPhysicalTime

/**
Produce an output token on each firing with a value that is
the simulated physical time, i.e., the time of the containing DE actor.
The output is of type double.

@author Jia Zou
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (jiazou)
@Pt.AcceptedRating
*/
public class SimulatedPlatformPhysicalTime extends TimedSource {
    /** Construct an actor with the given container and name.
    *
    *  @param container The container.
    *  @param name The name of this actor.
    *  @exception IllegalActionException If the actor cannot be contained
    *   by the proposed container.
    *  @exception NameDuplicationException If the container already has an
    *   actor with this name.
    */
    public SimulatedPlatformPhysicalTime(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set the type constraints.
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the simulated physical time to the output, which is the
     *  currentTime of the enclosing DE director.
     *  @exception IllegalActionException If send was unsuccessful.
     */
    public void fire() throws IllegalActionException {
        PtidesBasicDirector director = (PtidesBasicDirector) getDirector();

        output.send(
                0,
                new DoubleToken(
                        (director
                                .getPlatformPhysicalTag(((PtidesBasicDirector) getDirector()).platformTimeClock).timestamp
                                .getDoubleValue())));

        super.fire();
    }

}
