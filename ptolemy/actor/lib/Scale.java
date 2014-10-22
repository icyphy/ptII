/* An actor that outputs a scaled version of the input.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Scale

/**
 Produce an output token on each firing with a value that is
 equal to a scaled version of the input.  The actor is polymorphic
 in that it can support any token type that supports multiplication
 by the <i>factor</i> parameter.  The output
 type is constrained to be at least as general as both the input and the
 <i>factor</i> parameter.
 For data types where multiplication is not commutative (such
 as matrices), whether the factor is multiplied on the left is controlled
 by the <i>scaleOnLeft</i> parameter. Setting the parameter to true means
 that the factor is  multiplied on the left, and the input
 on the right. Otherwise, the factor is multiplied on the right.

 @author Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (yuhong)
 */
public class Scale extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Scale(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        factor = new Parameter(this, "factor");
        factor.setExpression("1");
        scaleOnLeft = new Parameter(this, "scaleOnLeft");
        scaleOnLeft.setExpression("true");

        // set the type constraints.
        output.setTypeAtLeast(input);
        output.setTypeAtLeast(factor);

        // icon
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-30,-20 30,-4 30,4 -30,20\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The factor.
     *  This parameter can contain any scalar token that supports
     *  multiplication.  The default value of this parameter is the
     *  IntToken 1.
     */
    public Parameter factor;

    /** Multiply on the left.
     *  This parameter controls whether the scale factor is multiplied
     *  on the left. The default value is a boolean token of value true.
     *  Setting is to false will multiply the factor on the right.
     */
    public Parameter scaleOnLeft;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Scale newObject = (Scale) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(newObject.factor);
        return newObject;
    }

    /** Compute the product of the input and the <i>factor</i>.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            Token in = input.get(0);
            Token factorToken = factor.getToken();
            Token result;

            if (((BooleanToken) scaleOnLeft.getToken()).booleanValue()) {
                // Scale on the left.
                result = factorToken.multiply(in);
            } else {
                // Scale on the right.
                result = in.multiply(factorToken);
            }
            output.send(0, result);
        }
    }
}
