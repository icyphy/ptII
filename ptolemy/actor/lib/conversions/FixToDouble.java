/* Actor that converts a FixToken into a DoubleToken.

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

@ProposedRating Yellow (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu) */

package ptolemy.actor.lib.conversions;

import ptolemy.actor.*;
import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.math.Precision;

//////////////////////////////////////////////////////////////////////////
//// FixToDouble
/**  

This actor converts a FixToken into a DoubleToken. This conversion is
explicitly provided since there exists not a lossless conversion
between these two types in the type lattice and thus the type system
cannot resolve this conversion automatically.

<P>

The actor reads in a single FixToken from its input port and converts
it to a DoubleToken. Before doing the conversion, it scales the
FixToken to a give precision. To fit into this new precision, a
rounding or overflow error may occur. In case of an overflow, the
value of the FixToken is determined, depending on the overflow mode
selected. The following overflow modes are supported in case an
overflow occurs.

<ul>

<li> mode = 0, <b>Saturate</b>: The FixToken is set, depending on its
sign, equal to the Maximum or Minimum value possible with the new
given precision.

<li> mode = 1, <b>Zero Saturate</b>: The FixToken is set equal to
zero.

</ul>

For this actor, the default overflow mode is set to Saturate and the
default precision is "(16/2)", which means that a FixToken is fit into
16 bits of which 2 bits represent the integer part.

Parameter <i>precision</i> is of type StringToken and parameter
<i>overflow</i> is of type IntToken.

@author Bart Kienhuis
@version $Id$
@see ptolemy.data.FixToken
@see ptolemy.data.Precision
*/

public class FixToDouble extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixToDouble(TypedCompositeActor container, String name)
	throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.FIX);
        output.setTypeEquals(BaseType.DOUBLE);

        // Set the Parameter
	precision = new Parameter(this, "precision", new StringToken("16/2"));
        precision.setTypeEquals(BaseType.STRING);

        // Set the Parameter
	overflow = new Parameter(this, "overflow", new IntToken(0));
        overflow.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** Precision of the FixPoint that is converted into a double.
     */
    public Parameter precision;


    /** Overflow mode used when fitting the FixPoint into the given
     *  Precision.
     */
    public Parameter overflow;

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
        FixToDouble newobj = (FixToDouble)super.clone(ws);
        newobj.precision = (Parameter)newobj.getAttribute("precision");
        newobj.overflow = (Parameter)newobj.getAttribute("overflow");
        return newobj;
    }

    /** Read at most one token from each input the convert the
     *  FixToken into a DoubleToken. The FixToken is howver first
     *  scaled to the desired precision, which may lead to rounding or
     *  overflow errors.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	if (input.hasToken(0)) {
    	    FixToken in = (FixToken)input.get(0);
            // Scale the FixToken to specific precision. If rounding
            // occurs, select which overflow mode to use.
            FixToken scaled = in.scaleToPrecision( _precision, _overflow );
	    DoubleToken result = new DoubleToken( scaled.convertToDouble() );
            output.send(0, result);
        }
    }

    /** Initialize the parameter of the actor.
     *  @exception IllegalActionException If the director throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _precision = new Precision(((StringToken)precision.getToken()).
                toString());
        _overflow = ((IntToken)overflow.getToken()).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The Precision of the Actor.
    private Precision _precision = null;

    // The overflow mode used when fitting to the desired precision.
    private int _overflow = 0;
}
