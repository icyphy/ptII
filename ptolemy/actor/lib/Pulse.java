/* An rectangular pulse source.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Pulse
/**
Produce a constant for some specified number of
firings, followed by zeros. Any type of token may be specified
for the constant.  The value of the zero tokens
is determined by the zero() method of the token class.

@author Edward A. Lee
@version $Id$
*/

public class Pulse extends TypedAtomicActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Pulse(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        width = new Parameter(this, "width", new IntToken(1));
        value = new Parameter(this, "value");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The value produced by the pulse when it is not zero.
     *  If this parameter is not specified by the time initialize() is
     *  called, then it will be set to a DoubleToken with value 1.0.
     */
    public Parameter value;

    /** The width of the pulse, in number of samples.
     *  This parameter contains an IntToken, with default value 1.
     */
    public Parameter width;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

// FIXME: Clone method.

    /** Evaluate the value of the pulse
     *  and set the declared type of the output port.
     *  If no value has been specified, then set it to a DoubleToken
     *  with value 1.0.
     *  @exception IllegalActionException If there is no director.
     */
    // FIXME: Mutations are not supported.
    public void initialize() throws IllegalActionException {
        Token _valuetoken = value.getToken();
        if (_valuetoken == null) {
            _valuetoken = new DoubleToken(1.0);
            value.setToken(_valuetoken);
        }
        output.setDeclaredType(value.getType());
        _zero = _valuetoken.zero();

        _width = ((IntToken)(width.getToken())).intValue();
    }

    /** Output the value if the count of firings is less than the width,
     *  otherwise output a zero token with the same type as the value.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (_firecount < _width) {
            output.broadcast(value.getToken());
        } else {
            output.broadcast(_zero);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token _valuetoken;
    private int _firecount = 0;
    private int _width;
    private Token _zero;
}

