/* An actor that delays the input for a certain real time.

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

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// RealTimeDelay
/**
An actor that delays the inputs for a certain duration of real time.
The delay only happens in postfire(). In the fire() stage of execution,
the inputs are directly transfered to outputs.
If the width of the input port is less than
that of the output port, the tokens in the extra channels
are lost.

@author Jie Liu
@version $Id$
*/

public class RealTimeDelay extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RealTimeDelay(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        delay = new Parameter(this, "delay",
                new LongToken(0));
	delay.setTypeEquals(BaseType.LONG);
	// Data type polymorphic, multiports.
        input.setMultiport(true);
        output.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The delay amount, in milliseconds
     *  This parameter must contain a LongToken.
     *  The default value of this parameter is 0, meaning no delay.
     */
    public Parameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        RealTimeDelay newobj = (RealTimeDelay)super.clone(ws);
        newobj.delay = (Parameter)newobj.getAttribute("delay");
        return newobj;
    }

    /** Output the inputs direcly.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        _transferTokens();
    }

    /** Output the inputs with a real time delay, specified by
     *  the parameter, delay. Always return true.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        try {
            long delayTime = ((LongToken)delay.getToken()).longValue();
            if(_debugging) _debug(getName() + "Wait for" +
                    delayTime + "milliseconds.");
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            // Ignore...
        }
        _transferTokens();
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Transfer tokens from inputs to outputs, one token from
     * each channel. If the width of the input port is less than
     * that of the output port, the tokens in the extra channels
     * are lost.
     */
    private void  _transferTokens() throws IllegalActionException {
        for (int i = 0; i < input.getWidth(); i++) {
            if(input.hasToken(i)) {
                Token inToken = input.get(i);
                if( i < output.getWidth()) {
                    output.send(i, inToken);
                }
            }
        }
    }
}
