/* A polymorphic real to decibel converter.

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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.conversions;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;

import ptolemy.math.SignalProcessing.*;
import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.actor.*;

import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// dB

/**

@author Bart Kienhuis
@version $Id$
*/

public class dB extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public dB(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        try {
            dB newobj = (dB)super.clone(ws);
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read a token from the input and convert the number into a
     *  decibel representation, which is defined to be
     *  20*log<sub>10</sub>(<em>z</em>), where z is the incoming
     *  value. If the input does not contain any tokens, do
     *  noting. Note that if the input data represents power, which is
     *  proportional to a magnitude squared, then this should be
     *  divided by two to get 10*log<sub>10</sub>(<em>z</em>).
     *  @exception IllegalActionException If there is no director, or
     *  if multiplication and division are not supported by the
     *  available tokens.
     */
    public void fire() throws IllegalActionException {
        if ( input.hasToken(0) ) {
            DoubleToken in = (DoubleToken) input.get(0);
            double number = in.doubleValue();
            double outNumber;
            if ( number <= 0.0 ) { 
                outNumber = -100;
            } else {
                outNumber = ptolemy.math.SignalProcessing.decibel( number );
                if ( outNumber < -100 ) {
                    outNumber = -100;
                }
            }
            output.send(0, new DoubleToken( outNumber ));
        }
    }

}
