/* Read ArrayTokens and send their elements to the output.

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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ArrayToSequence
/**
This actor reads an array at the input and writes the array elements
as a sequence to the output. The parameter <i>arrayLength</i> can be
used to specify the length of arrays that the actor will accept.
If the <i>enforceArrayLength</i> parameter true, then if an input
array does not match <i>arrayLength</i>, the fire() method will throw
an exception.  This feature is important in domains, such as SDF,
that do static scheduling based on production and consumption
rates.  For other domains, such as DE and PN, the <i>enforceArrayLength</i>
parameter can be set to false, in which case the <i>arrayLength</i>
parameter will be ignored.
<p>
This actor is polymorphic. It can accept ArrayTokens with any element
type and send out tokens corresponding to that type.
<p>

@author Yuhong Xiong
@version $Id$
@since Ptolemy II 0.4
*/

public class ArrayToSequence extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayToSequence(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Set type constraints.
        input.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        ArrayType inputType = (ArrayType)input.getType();
        InequalityTerm elementTerm = inputType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm);

        // Set parameters.
        arrayLength = new Parameter(this, "arrayLength");
        arrayLength.setExpression("1");
        enforceArrayLength = new Parameter(this, "enforceArrayLength");
        enforceArrayLength.setExpression("true");
        enforceArrayLength.setTypeEquals(BaseType.BOOLEAN);

        output_tokenProductionRate.setExpression("arrayLength");

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The size of the input array.  This is an integer that defaults
     *  to 1.
     */
    public Parameter arrayLength;

    /** If true, then enforce the <i>arrayLength</i> parameter by
     *  throwing an exception if it is violated. This is a boolean
     *  that defaults to true.
     */
    public Parameter enforceArrayLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>arrayLength</i> parameter, then
     *  set the production rate of the output port, and invalidate
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

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ArrayToSequence newObject = (ArrayToSequence)(super.clone(workspace));

        // set the type constraints
        ArrayType inputType = (ArrayType)newObject.input.getType();
        InequalityTerm elementTerm = inputType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm);
        return newObject;
    }

    /** Consume the input ArrayToken and produce the outputs.
     *  @exception IllegalActionException If a runtime type conflict occurs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ArrayToken token = (ArrayToken)input.get(0);
        int rate = ((IntToken)arrayLength.getToken()).intValue();
        boolean enforce = ((BooleanToken)enforceArrayLength.getToken())
            .booleanValue();
        if (enforce && token.length() != rate) {
            throw new IllegalActionException(this, "The " +
                    "number of elements in the input ArrayToken (" +
                    token.length() + ") is not the same as the arrayLength " +
                    "parameter (" + rate + ").");
        }

        Token[] elements = token.arrayValue();
        output.send(0, elements, elements.length);
    }

    /** Return the type constraint that the type of the output port is no
     *  less than the type of the elements of the input array.
     *  @return A list of inequalities.
     */
    public List typeConstraintList() {
        // Override the base class implementation to not use the default
        // constraints.
        return output.typeConstraintList();
    }
}
