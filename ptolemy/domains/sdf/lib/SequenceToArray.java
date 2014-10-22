/* Bundle a sequence of N input tokens into an ArrayToken.

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
package ptolemy.domains.sdf.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.ActorTypeUtil;
import ptolemy.actor.util.ArrayElementTypeFunction;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SequenceToArray

/**
 <p>This actor bundles a specified number of input tokens into a single array
 and broadcasts the resulting array on all output channels.
 The number of tokens to be bundled is specified by the <i>arrayLength</i>
 parameter.
 </p><p>
 This actor is polymorphic. It can accept inputs of any type, as long
 as the type does not change, and will produce an array with elements
 of the corresponding type.
 </p>

 @author Yuhong Xiong
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Green (yuhong)
 @Pt.AcceptedRating Yellow (neuendor)
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
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input_tokenConsumptionRate.setExpression("arrayLength");

        /* Note that the output ports need not be multiports since all output
         * channels carry the same data. Using multiports this way is not
         * recommended, so please do not emulate this design in your own actors.
         * Changing this actor to the recommended design would break existing
         * models, so we leave it as is.
         */
        output.setMultiport(true);
        output.setDefaultWidth(1);

        // Set parameters.
        arrayLength = new PortParameter(this, "arrayLength");
        arrayLength.setExpression("1");

        // Set the output type to be an ArrayType with element types
        // at least as general as the input type.
        output.setTypeAtLeast(ActorTypeUtil.arrayOf(input, arrayLength));
        // For backward type inference.
        input.setTypeAtLeast(new ArrayElementTypeFunction(output));

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The size of the output array.  This is an integer that defaults
     *  to 1.
     */
    public PortParameter arrayLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ensure that the arrayLength parameter is not negative.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == arrayLength) {
            int rate = ((IntToken) arrayLength.getToken()).intValue();

            if (rate < 0) {
                throw new IllegalActionException(this, "Invalid arrayLength: "
                        + rate);
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SequenceToArray newObject = (SequenceToArray) super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ActorTypeUtil.arrayOf(
                    newObject.input, newObject.arrayLength));
            newObject.input.setTypeAtLeast(new ArrayElementTypeFunction(
                    newObject.output));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Consume the inputs and produce the output ArrayToken.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int length = ((IntToken) arrayLength.getToken()).intValue();

        Token[] valueArray = new Token[length];
        System.arraycopy(input.get(0, length), 0, valueArray, 0, length);

        output.broadcast(new ArrayToken(input.getType(), valueArray));
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
    @Override
    public boolean prefire() throws IllegalActionException {
        arrayLength.update();
        int length = ((IntToken) arrayLength.getToken()).intValue();

        if (!input.hasToken(0, length)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }

            return false;
        } else {
            return super.prefire();
        }
    }
}
