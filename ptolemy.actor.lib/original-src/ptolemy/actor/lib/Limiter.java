/* An actor that limits the input to a specified range.

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

import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Limiter

/**
 Produce an output token on each firing with a value that is
 equal to the input if the input lies between the <i>bottom</i> and
 <i>top</i> parameters.  Otherwise, if the input is greater than <i>top</i>,
 output <i>top</i>.  If the input is less than <i>bottom</i>, output
 <i>bottom</i>.  This actor operates on scalar types only.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (yuhong)
 */
public class Limiter extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Limiter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        bottom = new Parameter(this, "bottom");
        bottom.setExpression("0.0");

        top = new Parameter(this, "top");
        top.setExpression("1.0");

        input.setTypeAtMost(BaseType.SCALAR);
        input.setTypeAtLeast(top);
        input.setTypeAtLeast(bottom);

        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The bottom of the limiting range.  This is a scalar with default
     *  value 0.0.
     */
    public Parameter bottom;

    /** The top of the limiting range.  This is a scalar with default
     *  value 1.0.
     */
    public Parameter top;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new instance of Sleep.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Limiter newObject = (Limiter) super.clone(workspace);
        newObject.input.setTypeAtMost(BaseType.SCALAR);
        newObject.input.setTypeAtLeast(newObject.top);
        newObject.input.setTypeAtLeast(newObject.bottom);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Compute the output and send it to the output port. If there is
     *  no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ScalarToken in = (ScalarToken) input.get(0);
            if (in.isLessThan((ScalarToken) bottom.getToken()).booleanValue()) {
                output.send(0, bottom.getToken());
            } else if (in.isGreaterThan((ScalarToken) top.getToken())
                    .booleanValue()) {
                output.send(0, top.getToken());
            } else {
                output.send(0, in);
            }
        }
    }
}
