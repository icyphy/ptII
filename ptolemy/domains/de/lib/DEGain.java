/* An actor that outputs monotonically increasing values.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEGain
/**

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEGain extends TypedAtomicActor {

    /** Constructor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The initial output event value.
     *  @param step The step size by which to increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEGain(TypedCompositeActor container, String name,
            double gain)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // set the parameters.
        _gain = new Parameter(this, "gain", new DoubleToken(gain));

        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);

        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        input.setDeclaredType(DoubleToken.class);
    }

    /** Construct a DEGain with the specified container, name, initial
     *  value and step size. The initial value and step size are
     *  represented by String expressions which will be evaluated
     *  by the corresponding Parameters.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The expression for the initial output event value.
     *  @param step The expression for the step size by which to
     *   increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEGain(TypedCompositeActor container, String name,
            String gain)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // set the parameters.
        _gain = new Parameter(this, "gain");
	_gain.setExpression(gain);

        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);

        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        input.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Produce the output.
     *  FIXME: better exception tags needed.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException {
        // output the state token.
        if (input.hasToken(0)) {
            double gain = ((DoubleToken)_gain.getToken()).doubleValue();
            double inval = ((DoubleToken)input.get(0)).doubleValue();
            output.broadcast(new DoubleToken(inval * gain));

        } else {
            throw new InternalErrorException("Scheduling error! DEGain" + 
                    "fired with no input tokens.");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                          public variables                 ////


    // the ports.
    public TypedIOPort output;
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private Parameter _gain;


}
