/* Bundle a sequence of N input tokens into an ArrayToken.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SequenceToArray
/**
This actor bundles a specified number of input tokens into a single array.
The number of tokens to be bundled is specified by the <i>arrayLength</i>
parameter.
<p>
This actor is polymorphic. It can accept inputs of any type, as long
as the type does not change, and will produce an array with elements
of the corresponding type.
<p>

@author Yuhong Xiong
@version $Id$
@since Ptolemy II 0.4
*/

public class SequenceToArray extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceToArray(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input_tokenConsumptionRate.setExpression("arrayLength");
        
        // set the output type to be an ArrayType.
        output.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        // Set parameters.
        arrayLength = new PortParameter(this, "arrayLength");
        arrayLength.setExpression("1");

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The size of the output array.  This is an integer that defaults
     *  to 1.
     */
    public PortParameter arrayLength;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>arrayLength</i> parameter, then
     *  set the consumption rate of the input port, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == arrayLength) {
            int rate = ((IntToken)arrayLength.getToken()).intValue();
            if (rate < 0) {
                throw new IllegalActionException(this,
                        "Invalid arrayLength: " + rate);
            }
        
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume the inputs and produce the output ArrayToken.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        arrayLength.update();
        int length = ((IntToken)arrayLength.getToken()).intValue();
        Token[] valueArray = input.get(0, length);

        output.send(0, new ArrayToken(valueArray));
    }

    /** Return true if the input port has enough tokens for this actor to
     *  fire. The number of tokens required is determined by the
     *  value of the <i>arrayLength</i> parameter.
     *  @return boolean True if there are enough tokens at the input port
     *   for this actor to fire.
     *  @exception IllegalActionException If the hasToken() query to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int, int)
     */
    public boolean prefire() throws IllegalActionException {
        int length = ((IntToken)arrayLength.getToken()).intValue();
        if (!input.hasToken(0, length)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        } else {
            return super.prefire();
        }
    }

    /** Return the type constraint that the type of the elements of the
     *  output array is no less than the type of the input port.
     *  @return A list of inequalities.
     */
    public List typeConstraintList() {
        ArrayType outArrType = (ArrayType)output.getType();
        InequalityTerm elementTerm = outArrType.getElementTypeTerm();
        Inequality ineq = new Inequality(input.getTypeTerm(), elementTerm);

        List result = new LinkedList();
        result.add(ineq);
        return result;
    }
}
