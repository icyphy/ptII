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
/** Read a FixToken and converts it to a DoubleToken. Before the
conversion takes place, the user can set the precision of the FixToken.

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
	precision = new Parameter(this, "precision", new StringToken(""));
        precision.setTypeEquals(BaseType.STRING);              
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    // FIXME: take precision into account.
    /** Precision of the FixPoint that is converted into a double.
     */
    public Parameter precision;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each input and convert the FixToken
     *  into a DoubleToken. The user has the option to change the
     *  precision of the FixToken before it is converted into the
     *  double token.
     * @exception IllegalActionException If there is no director.  
     */
    public void fire() throws IllegalActionException {
	if (input.hasToken(0)) {
    	    FixToken in = (FixToken)input.get(0);
	    DoubleToken result = new DoubleToken(in.doubleValue());    
            output.send(0, result);
        }
    }

    /** Initialize the parameter of the actor.
     *  @exception IllegalActionException If the director throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // FIXME: Need to do the scaling in Quantizer
        _precision = new Precision(precision.getToken().toString());        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Precision _precision = null;
}
