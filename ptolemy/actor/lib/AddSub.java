/* A polymorphic adder/subtractor.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// AddSub
/**
A polymorphic adder/subtractor.
This adder has two input ports, both of which are multiports,
and an output port, which is not.
The types on the ports are undeclared and will be resolved by
the type resolution mechanism. Data that arrives on the
input port named "plus" will be added, and data that arrives
on the input port named "minus" will be subtracted.
<p>
This actor is not strict. That is, it does not require that each input
channel have a token upon firing. It will add or subtract the available
tokens at the inputs and ignore the channels that do not have tokens.
If no input tokens are available at all, then no output is produced.
<p>
Currently, the type system is quite liberal about the resolved
types it will permit at the inputs. In particular, it may permit the
"plus" and "minus" inputs to resolve to types that cannot in fact
be subtracted.  In these cases, a run-time error will occur.
In the future, we hope that the type system will intercept such errors.

@author Yuhong Xiong and Edward A. Lee
@version $Id$
*/

public class AddSub extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AddSub(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	plus = new TypedIOPort(this, "plus", true, false);
	plus.setMultiport(true);
	minus = new TypedIOPort(this, "minus", true, false);
	minus.setMultiport(true);
	output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////

    public TypedIOPort plus = null;
    public TypedIOPort minus = null;
    public TypedIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            AddSub newobj = (AddSub)super.clone(ws);
            newobj.plus = (TypedIOPort)newobj.getPort("plus");
            newobj.minus = (TypedIOPort)newobj.getPort("minus");
            newobj.output = (TypedIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** If there is at least one token in the input ports, add the
     *  tokens on all the channels of the plus port, subtract the
     *  tokens on the minus port, and send
     *  the result to the output port. At most one token is read
     *  from each channel, so if more than one token is pending, the
     *  rest are left for future firings.  If none of the input
     *  channels has a token, do nothing.  If none of the plus channels
     *  have tokens, then the tokens on the minus channels are subtracted
     *  from a zero token of the same type as the first token enountered
     *  on the minus channels.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    public void fire() throws IllegalActionException {
	Token sum = null;
	for (int i = 0; i < plus.getWidth(); i++) {
	    if (plus.hasToken(i)) {
		if (sum == null) {
		    sum = plus.get(i);
		} else {
		    sum = sum.add(plus.get(i));
		}
	    }
	}
	for (int i = 0; i < minus.getWidth(); i++) {
	    if (minus.hasToken(i)) {
                Token in = minus.get(i);
		if (sum == null) {
		    sum = in.zero();
		} else {
		    sum = sum.subtract(in);
		}
	    }
	}

	if (sum != null) {
	    output.broadcast(sum);
	}
    }
}

