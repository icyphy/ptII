/* A polymorphic multiplier/divider.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide
/**
A polymorphic multiplier and/or divider.
This adder has two input ports, both of which are multiports,
and one output port, which is not.
The types on the ports are undeclared and will be resolved by
the type resolution mechanism. Data that arrives on the
input port named <i>multiply</i> will be multiplied, and data that arrives
on the input port named <i>divide</i> will be divided.
Any token type supporting multiplication and division can be used.
In most domains, either input port can be left unconnected.
Thus, to get a simple multiplier (with no division), just leave the
<i>divide</i> input unconnected.
<p>
Currently, the type system is quite liberal about the resolved
types it will permit at the inputs. In particular, it may permit the
<i>multiply</i> and <i>divide</i> inputs to resolve to types that cannot
in fact be multiplied or divided.  In these cases, a run-time error will occur.
In the future, we hope that the type system will intercept such errors
before run time.
<p>
This actor is not strict. That is, it does not require that each input
channel have a token upon firing. It will multiply or divide available
tokens at the inputs and ignore the channels that do not have tokens.
It consumes at most one input token from each port.
If no input tokens are available on the <i>multiply</i> inputs,
then a numerator of one is assumed for the division operations.
The "one" is obtained by calling the one() method of the first
token seen at the <i>divide</i> input.
If no input tokens are available at all, then no output is produced.

@author Edward A. Lee
@version $Id$
*/

public class MultiplyDivide extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MultiplyDivide(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	multiply = new TypedIOPort(this, "multiply", true, false);
	multiply.setMultiport(true);
	divide = new TypedIOPort(this, "divide", true, false);
	divide.setMultiport(true);
	output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens to be divided.  This is a multiport, and its
     *  type is inferred from the connections.
     */
    public TypedIOPort divide;

    /** Output port.  The type is inferred from the connections.
     */
    public TypedIOPort output;

    /** Input for tokens to be multiplied.  This is a multiport, and its
     *  type is inferred from the connections.
     */
    public TypedIOPort multiply;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        MultiplyDivide newobj = (MultiplyDivide)super.clone(ws);
        newobj.multiply = (TypedIOPort)newobj.getPort("multiply");
        newobj.divide = (TypedIOPort)newobj.getPort("divide");
        newobj.output = (TypedIOPort)newobj.getPort("output");
        return newobj;
    }

    /** If there is at least one token on the input ports, multiply
     *  tokens from the <i>multiply</i> port, divide by
     *  tokens from the <i>divide</i> port,
     *  and send the result to the output port. At most one token is read
     *  from each channel, so if more than one token is pending, the
     *  rest are left for future firings.  If none of the input
     *  channels has a token, do nothing.  If none of the multiply channels
     *  have tokens, then the tokens on the divide channels are divided into
     *  a one token of the same type as the denominator.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if multiplication and division are not supported by the
     *   available tokens.
     */
    public void fire() throws IllegalActionException {
	Token numerator = null;
	for (int i = 0; i < multiply.getWidth(); i++) {
	    if (multiply.hasToken(i)) {
		if (numerator == null) {
		    numerator = multiply.get(i);
		} else {
		    numerator = numerator.multiply(multiply.get(i));
		}
	    }
	}
        Token denominator = null;
	for (int i = 0; i < divide.getWidth(); i++) {
	    if (divide.hasToken(i)) {
		if (denominator == null) {
		    denominator = divide.get(i);
		} else {
		    denominator = denominator.multiply(divide.get(i));
		}
	    }
        }
        if (numerator == null) {
            if (denominator == null) {
                return;
            }
            numerator = denominator.one();
        }
        if (denominator != null) {
            numerator = numerator.divide(denominator);
	}
        output.send(0, numerator);
    }
}
