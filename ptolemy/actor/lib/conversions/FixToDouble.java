/* Actor that converts a FixToken into a DoubleToken.

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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
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
/** Read a FixToken and converts it to a DoubleToken. Before doing so,
scale the FixToken to the give precision. To fit the new precision, a
rounding error may occur. In that case the value of the FixToken is
determined, depending on the quanitzation mode selected. The following
quantization modes are supported in case an overflow occurs.

<ul> 
<li> mode = 0, <b>Saturate</b>: The FixToken is set,
depending on its sign, equal to the Max or Min value possible
with the new given precision.
<li> mode = 1, <b>Zero Saturate</b>: The FixToken is
set equal to zero.
</ul>

The default quantizer is Saturate and the default value for precision
is "(16/2)".

@author Bart Kienhuis 
@version $Id$
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
	quantizer = new Parameter(this, "quantizer", new IntToken(0));
        quantizer.setTypeEquals(BaseType.INT);              
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** Precision of the FixPoint that is converted into a double.
     */
    public Parameter precision;


    /** Select the mode when rouding the FixPoint to the given Precision.
     */
    public Parameter quantizer;

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
            FixToDouble newobj = (FixToDouble)super.clone(ws);
            newobj.precision = (Parameter)newobj.getAttribute("precision");
            newobj.quantizer = (Parameter)newobj.getAttribute("quantizer");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read at most one token from each input and convert the FixToken
     *  into a DoubleToken. 
     * @exception IllegalActionException If there is no director.  
     */
    public void fire() throws IllegalActionException {
	if (input.hasToken(0)) {
    	    FixToken in = (FixToken)input.get(0);
            // Scale the FixToken to specific precision.
            // If rounding occurs, select which quantizer to use.
            FixToken scaled = in.scaleToPrecision( _precision, _quantizer );
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
        _quantizer = ((IntToken)quantizer.getToken()).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The Precision of the Actor.
    private Precision _precision = null;

    // The quantizer when fitting to the desired precision.
    private int _quantizer = 0;
}
