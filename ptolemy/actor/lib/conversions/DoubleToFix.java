/* Actor that converts a DoubleToken into a FixToken.

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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu) */

package ptolemy.actor.lib.conversions;

import ptolemy.math.Quantizer;
import ptolemy.math.Precision;
import ptolemy.actor.*;
import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// DoubleToFix
/**

Read a DoubleToken and converts it to a FixToken with a given
precision.  In fitting the double into a fix point, the following
quantization modes are supported.

<ul>
<li> mode = 0, <b>Rounding</b>:.
<li> mode = 1, <b>Truncate</b>:.
</ul>

The default quantizer is rounding and the default value for precision
is "(16/2)".

@author Bart Kienhuis
@version $Id$
*/

public class DoubleToFix extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoubleToFix(TypedCompositeActor container, String name)
	throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
	output.setTypeEquals(BaseType.FIX);

	precision =
            new Parameter(this, "precision", new StringToken("(16/2)"));
        precision.setTypeEquals(BaseType.STRING);

	quantizer = new Parameter(this, "quantizer", new IntToken(0));
        quantizer.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The precision of the Fix point */
    public Parameter precision;

    /** The quantizer used to convert a double into a fix point. */
    public Parameter quantizer;

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
        try {
            DoubleToFix newobj = (DoubleToFix)super.clone(ws);
            newobj.precision = (Parameter)newobj.getAttribute("precision");
            newobj.quantizer = (Parameter)newobj.getAttribute("quantizer");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read at most one token from each input and convert the Token
     *  value in a FixToken with a given precision.
     *
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        FixToken result = null;
	if (input.hasToken(0)) {
    	    DoubleToken in = (DoubleToken)input.get(0);
            switch( _quantizer ) {
            case 0:
                result = new FixToken(
                        Quantizer.round(in.doubleValue(), _precision) );
                break;
            case 1:
                result = new FixToken(
                        Quantizer.truncate(in.doubleValue(), _precision) );
                break;
            default:
                throw new IllegalActionException(
                        "Selected a unknown quantizer mode for DoubleToFix");
            }
            output.send(0, result);
        }
    }

    /** Initialize the parameter of the actor.
     *  @exception IllegalActionException If the director throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _precision = new Precision(precision.getToken().toString());
        _quantizer = ((IntToken)quantizer.getToken()).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The precision of the FixToken.
    private Precision _precision = null;

    // The mode of Quantization.
    private int _quantizer = 0;
}
