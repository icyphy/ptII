/* An actor that models a channel with random delay and drop.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.sc.demo.ABP;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEChannel
/**
The channel delays the input messages randomly, and drops messages with
a probability. It does not change the order of messages.

@author Xiaojun Liu
@version $Id$
*/
public class DEChannel extends DEActor {

    /** Constructor.
     *  @param name The name of this actor.
     *  @param drop The message drop probability.
     *  @param maxDelay The maximum delay of messages.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEChannel(TypedCompositeActor container, String name,
                double dropRate, double maxDelay, double minDelay)
                throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output = new DEIOPort(this, "output", false, true);
        output.setDeclaredType(IntToken.class);
        input = new DEIOPort(this, "input", true, false);
        input.setDeclaredType(IntToken.class);
        _dropRate = new Parameter(this, "DropRate", new DoubleToken(dropRate));
        _maxDelay = new Parameter(this, "MaxDelay", new DoubleToken(maxDelay));
        _minDelay = new Parameter(this, "MinDelay", new DoubleToken(minDelay));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Accept the input packet, and drop it randomly. If the packet is
     *  not dropped, then put it in a queue. If the current time agrees
     *  with a scheduled packet output time, then output a packet, set
     *  the next packet output time.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

        if (input.hasToken(0)) {
            if (Math.random() < ((DoubleToken)_dropRate.getToken()).doubleValue()) {
                // drop the message
                input.get(0);
            } else {
                // put input message into queue
                _msgs.insertLast(input.get(0));
                if (_msgs.size() == 1) {
                    // schedule output time
                    double minDelay = ((DoubleToken)_minDelay.getToken()).doubleValue();
                    double maxDelay = ((DoubleToken)_maxDelay.getToken()).doubleValue();
                    double delay = minDelay + (maxDelay - minDelay)*Math.random();
                    _nextOutTime = getCurrentTime() + delay;
                    fireAfterDelay(delay);
                }
            }

/* REMOVE */
System.out.println("DEChannel " + this.getFullName() + " get input message at "
        + getCurrentTime());

        }

        if(Math.abs(getCurrentTime() - _nextOutTime) < 1e-14) {
            // send out a message
            IntToken msg = (IntToken)_msgs.take();
            output.broadcast(msg);

/* REMOVE */
System.out.println("DEChannel " + this.getFullName() + " sends message at "
        + getCurrentTime());

            if (_msgs.size() > 0) {
                // schedule output time
                double minDelay = ((DoubleToken)_minDelay.getToken()).doubleValue(
);
                double maxDelay = ((DoubleToken)_maxDelay.getToken()).doubleValue(
);
                double delay = minDelay + (maxDelay - minDelay)*Math.random();
                _nextOutTime = getCurrentTime() + delay;
                fireAfterDelay(delay);
            }
        }

    }

    /** Initialize the channel. Clear the message list, reset next output
     *  time.
     *  @exception IllegalActionException If the initialize() of the parent
     *   class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _msgs.clear();
        _nextOutTime = -1.0;
    }

    public DEIOPort input;
    public DEIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _msgs = new LinkedList();

    private double _nextOutTime = -1.0;

    private Parameter _dropRate;
    private Parameter _maxDelay;
    private Parameter _minDelay;

}
