/* An actor that generates messages according to Poisson process.

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
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEMessageSource
/**
Generate messages according to Poisson process.

@author Xiaojun Liu
@version $Id$
*/
public class DEMessageSource extends DEActor {

    /** Constructor.
     *  @param container The composite actor that this actor belongs to.
     *  @param name The name of this actor.
     *  @param value The value of the output events.
     *  @param lambda The mean of the inter-arrival times.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEMessageSource(TypedCompositeActor container,
            String name, double maxDelay)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new DEIOPort(this, "output", false, true);
        output.setDeclaredType(IntToken.class);
        request = new DEIOPort(this, "request", false, true);
        request.setDeclaredType(Token.class);
        next = new DEIOPort(this, "next", true, false);
        next.setDeclaredType(Token.class);
        _maxDelay = new Parameter(this, "MaxDelay", new DoubleToken(maxDelay));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Schedule the first fire after a random delay between zero and MaxDealy.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _firstFire = true;
        fireAfterDelay(((DoubleToken)_maxDelay.getToken()).doubleValue()*Math.random());
    }

    /** If this is the first fire, output the request token. Otherwise, if current
     *  time agrees with the scheduled message output time, output the message. If
     *  there is a token in port next, then schedule the next message output time.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

        if (_firstFire) {
            request.broadcast(new Token());
            _firstFire = false;
            return;
        }

        double maxDelay = ((DoubleToken)_maxDelay.getToken()).doubleValue();

	if (next.hasToken(0)) {
            next.get(0);
            if (getCurrentTime() < _nextMsgTime) {
                // ignore this
            } else {
                // compute a random delay between zero and MaxDelay.
                double delay = maxDelay*Math.random();
	        fireAfterDelay(delay);
                _nextMsgTime = getCurrentTime() + delay;
            }

System.out.println("DEMessageSource " + this.getFullName() + " next message "
        + "scheduled at " + _nextMsgTime);

        }

        if (Math.abs(getCurrentTime() - _nextMsgTime) < 1e-14) {
            ++_msgNum;
            output.broadcast(new IntToken(_msgNum));
        } else {
            // this refire should be discarded
            return;
        }

    }

    public DEIOPort next;
    public DEIOPort output;
    public DEIOPort request;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the mean inter-arrival time and value
    private Parameter _maxDelay;

    private boolean _firstFire = true;
    private int _msgNum = 0;
    private double _nextMsgTime = -1;
}











