/* An actor that outputs the average of the inputs so far.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// Average
/**
Output the average of the inputs so far.
One output is produced each time the actor fires.
The inputs and outputs can be any token type that
supports addition and division by an integer.  The output type is 
constrained to be the same as the input type.
<p>
Note that the type system will fail to catch some errors. Static type
checking may result in a resolved type that does not support addition
and division.  In this case, a run-time error will occur.
<p>
NOTE: It would be nice if this actor had a reset input.

@author Edward A. Lee
@version $Id$
*/

public class Average extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Average(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output.setTypeSameAs(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Average newobj = (Average)super.clone(ws);
        newobj.output.setTypeSameAs(newobj.input);
        return newobj;
    }

    /** Consume exactly one token from the the input
     *  and compute the average of the input tokens so far. Send the
     *  result to the output.  It is assumed that there is at least
     *  one input token available, or a NoTokenException will be thrown.
     *  This is a runtime exception, so it need not be declared explicitly.
     *  If the fire method
     *  is invoked multiple times in one iteration, then only the
     *  input read on the last invocation in the iteration will affect
     *  future averages.  Inputs that are read earlier in the iteration
     *  are forgotten.
     *  @exception IllegalActionException If the right arithmetic operations
     *   are not supported by the supplied tokens.
     */
    public void fire() throws IllegalActionException {
        try {
            Token in = input.get(0);
            if (_accum == null) {
                _accum = in.zero();
            }
            _latestSum = _accum.add(in);
            Token out = _latestSum.divide(new IntToken(_count));
            output.broadcast(out);
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }

    /** Reset the count of inputs.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = 1;
        _accum = null;
    }

    /** Record the most recent input as part of the running average.
     *  Do nothing if there is no input.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _accum = _latestSum;
        _count++;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Token _accum;
    private int _count = 1;
    private Token _latestSum;
}

