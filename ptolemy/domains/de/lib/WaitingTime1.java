/* Measure the time that events at one input have to wait for events at another.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.lib.SequenceActor;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// WaitingTime1
/**
This is a single port version of the WaitingTime actor.
The output is the waiting time between the last two input events.
The output is always a DoubleToken.

@author Lukito Muliadi, Edward A Lee
@version $Id$
*/
public class WaitingTime1 extends DETransformer {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WaitingTime1(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create the ports
        
        output.setTypeEquals(BaseType.DOUBLE);
        input.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        WaitingTime1 newObject = (WaitingTime1)super.clone(ws);
        newObject.output.setTypeEquals(BaseType.DOUBLE);
        newObject.input.setTypeEquals(BaseType.GENERAL);
        return newObject;
    }



    /** If this firing is triggered by an event at <i>waitee</i>, then output
     *  the waiting time for each prior event arrival at <i>waiter</i>
     *  since the last arrival of waitee.  If there is no event at
     *  <i>waitee</i>, then record the time of arrival of the events
     *  at <i>waiter</i>, and produce no output.
     *  @exception IllegalActionException If get() or send() throws it.
     */
    public void fire() throws IllegalActionException {

        while(input.hasToken(0)) {
            input.get(0);
        }
        double currentTime = ((DEDirector)getDirector()).getCurrentTime();
        if (_previousTime != -1.0) {
            DoubleToken outToken =
                new DoubleToken(currentTime-_previousTime);
            output.send(0, outToken);
        }
        _previousTime = currentTime;
    }

    /** Set the previous event time to -1.
     */
    public void initialize() throws IllegalActionException {
        _previousTime = -1.0;
        super.initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _previousTime = -1.0;
}
