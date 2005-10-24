/* A polymorphic adder/subtractor for use with ECSL

Copyright (c) 2004-2005 The Regents of the University of California.
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

package vendors.ecsl_dp.ptolemy;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Sum
/**
   A polymorphic adder/subtractor for use with ECSL.  This adder has
   one input multiport and one output port that is not a multiport.
   The types on the input port and the output port default to double.
   Data that arrives on the input port is added or subtracted to
   depending on the value of the <i>Inputs</i> parameter.  For
   example, if the <i>Inputs</i> parameter is "|+-", then the first
   input port will be added and the second input port will be
   subtracted.

   @author Christopher Brooks, Based on AddSubtract by Yuhong Xiong and Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/

public class ECSLSum extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ECSLSum(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);

        // FIXME: hide minus and plus ports
        //plus.setTypeEquals(BaseType.DOUBLE);
        //minus.setTypeEquals(BaseType.DOUBLE);

        Inputs = new StringParameter(this, "Inputs");

        // FIXME: Expect two connections, which is connected to the
        // plus port, the other to the minus port

        Inputs.setExpression("|+-");

        IconShape = new StringParameter(this, "IconShape");
        IconShape.setExpression("UNKNOWN");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The string that describes which ports are connected to
     *  plus and which are connected to minus.  "|+-" means the
     *  the first connection to the input port is to be connected
     *  to the plus port of the parent and the second connection
     *  to the input port is to be connected to the minus port.
     */
    public StringParameter Inputs;

    /** The shape of the Icon.  Currently ignored.  The default value
     *  is the string "UNKNOWN";
     */
    public StringParameter IconShape;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read token(s) from the input port and add or subtract them
     *  depending on the value the <i>Inputs</i> parameter.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens or if the output is a multiport.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (output.getWidth() > 1) {
            throw new IllegalActionException("Output widths greater than "
                    + "1 not yet supported");
        }

        Token sum = null;
        String inputsValue = Inputs.getExpression();

        // We stop looping when we run out of characters in the
        // InputValue parameter or when we run out of multiports
        // FIXME: should we throw an exception if there are more or less
        // + or - characters than there are multiports?

        for (int valueIndex = 0, multiportIndex = 0;
             (valueIndex < inputsValue.length()
                     && multiportIndex < input.getWidth());
             valueIndex++) {
            char plusOrMinus = inputsValue.charAt(valueIndex);
            switch (plusOrMinus) {
            case '|':
                // FIXME: The '|' is ignored
                break;
            case '+':
                // This if clause is taken from AddSubtract
                System.out.println(getName() + ": got + " + valueIndex + " " + multiportIndex);
                if (input.hasToken(multiportIndex)) {
                    if (sum == null) {
                        sum = input.get(multiportIndex);
                    } else {
                        sum = sum.add(input.get(multiportIndex));
                    }
                }
                multiportIndex++;
                break;
            case '-':
                System.out.println(getName() + ": got - " + valueIndex + " " + multiportIndex);
                // This if clause is taken from AddSubtract
                if (input.hasToken(multiportIndex)) {
                    Token in = input.get(multiportIndex);
                    if (sum == null) {
                        sum = in.zero();
                    }
                    sum = sum.subtract(in);
                }
                multiportIndex++;
                break;
            default:
                throw new IllegalActionException(this, "The value of the "
                        + " Input parameter is \"" + inputsValue
                        + "\" which contains a character '"
                        + plusOrMinus + "', which is not understood. "
                        + "Only '|', '+' and '-' are permitted");
            }
        }

        if (sum != null) {
            output.send(0, sum);
        }
    }
}
