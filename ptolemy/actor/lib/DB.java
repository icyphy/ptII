/* Convert to dB.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;

import ptolemy.math.SignalProcessing;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;

import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// DB

/**
Produce a token that is the value of the input in decibels.
That is, if the input is <i>z</i>, then the output is
<i>k</i>*log<sub>10</sub>(<em>z</em>).
The constant <i>k</i> depends on the value of the <i>inputIsPower</i>
parameter.  If that parameter is <i>true</i>, then <i>k</i> = 10.
Otherwise (the default) <i>k</i> = 20.
Normally, you would set <i>inputIsPower</i> to <i>true</i> if
the input is the square of a signal, and to false otherwise.
<p>
The output is never smaller than the value of the <i>min</i> parameter.
This makes it easier to plot by limiting the range of output values.
If the input is zero or negative, then the output is the
value of the <i>min</i> parameter.
<p>
The input and output both have type double.

@author Bart Kienhuis and Edward A. Lee
@version $Id$
*/

public class DB extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DB(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        inputIsPower =
            new Parameter(this, "inputIsPower", new BooleanToken(false));
        inputIsPower.setTypeEquals(BaseType.BOOLEAN);
        min =
            new Parameter(this, "min", new DoubleToken(-100.0));
        min.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** If the input is proportional to power, then set this to true. */
    public Parameter inputIsPower;

    /** The minimum value of the output. */
    public Parameter min;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        DB newobj = (DB)super.clone(ws);
        newobj.inputIsPower = (Parameter)newobj.getAttribute("inputIsPower");
        newobj.min = (Parameter)newobj.getAttribute("min");
        return newobj;
    }

    /** Read a token from the input and convert its value into a
     *  decibel representation. If the input does not contain any tokens,
     *  do nothing.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if ( input.hasToken(0) ) {
            DoubleToken in = (DoubleToken) input.get(0);
            double number = in.doubleValue();
            double minValue = ((DoubleToken)min.getToken()).doubleValue();
            double outNumber;
            if ( number <= 0.0 ) {
                outNumber = minValue;
            } else {
                outNumber = ptolemy.math.SignalProcessing.decibel( number );
                if (((BooleanToken)inputIsPower.getToken()).booleanValue()) {
System.out.println("dividing by 2.0");
                    outNumber /= 2.0;
                }
                if ( outNumber < minValue ) {
                    outNumber = minValue;
                }
            }
            output.send(0, new DoubleToken( outNumber ));
        }
    }
}
