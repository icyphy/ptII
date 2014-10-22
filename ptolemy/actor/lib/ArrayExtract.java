/* Extract a subarray from an array.

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

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayExtract

/**
 Extract a subarray from an array.  This actor reads an array from the
 <i>input</i> port and sends a subarray to the <i>output</i>
 port, possibly padded with zeros. The segment of the input array
 starting at <i>sourcePosition</i> with length <i>extractLength</i> is
 copied to the output array, starting at <i>destinationPosition</i>.
 The total length of the output array is <i>outputArrayLength</i>.
 Any of its entries that are not supplied by the input have value
 zero (of the same type as the entries in the input array).
 With the default values of the parameters, only the first element
 of the input array is copied to the output array, which has length one.
 The output type is the same as the input type.

 @author Edward A. Lee, Elaine Cheong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (celaine)
 @Pt.AcceptedRating Green (cxh)
 */
public class ArrayExtract extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayExtract(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set type constraints.
        input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        output.setTypeAtLeast(input);
        // FIXME: correct type constraint for length
        output.setTypeAtLeast(ArrayType.ARRAY_UNSIZED_BOTTOM);

        // Set parameters.
        sourcePosition = new PortParameter(this, "sourcePosition");
        sourcePosition.setExpression("0");
        new StringAttribute(sourcePosition.getPort(), "_cardinal")
                .setExpression("SOUTH");
        new Parameter(sourcePosition.getPort(), "_showName")
                .setExpression("true");
        extractLength = new PortParameter(this, "extractLength");
        extractLength.setExpression("1");
        new StringAttribute(extractLength.getPort(), "_cardinal")
                .setExpression("SOUTH");
        new Parameter(extractLength.getPort(), "_showName")
                .setExpression("true");
        destinationPosition = new PortParameter(this, "destinationPosition");
        destinationPosition.setExpression("0");
        new StringAttribute(destinationPosition.getPort(), "_cardinal")
                .setExpression("SOUTH");
        new Parameter(destinationPosition.getPort(), "_showName")
                .setExpression("true");
        outputArrayLength = new PortParameter(this, "outputArrayLength");
        outputArrayLength.setExpression("1");
        new StringAttribute(outputArrayLength.getPort(), "_cardinal")
                .setExpression("SOUTH");
        new Parameter(outputArrayLength.getPort(), "_showName")
                .setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The index into the input array at which to start copying.
     *  This is a non-negative integer that defaults to 0, and is
     *  required to be less than the length of the input array.
     */
    public PortParameter sourcePosition;

    /** The length of the segment of the input array that is copied
     *  to the output. This is a non-negative integer that defaults
     *  to 1. The sum of it and the <i>sourcePosition</i> is
     *  required to be less than or equal to the length of the input array.
     */
    public PortParameter extractLength;

    /** The index into the output array at which to start copying.
     *  This is a non-negative integer that defaults to 0, and is
     *  required to be less than the length of the output array.
     */
    public PortParameter destinationPosition;

    /** The total length of the output array.
     *  This is a non-negative integer that defaults to 1.  It is
     *  required to be at least <i>destinationPosition</i> plus
     *  <i>extractLength</i>.
     */
    public PortParameter outputArrayLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets up the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayExtract newObject = (ArrayExtract) super.clone(workspace);

        // Set the type constraints.
        newObject.input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(ArrayType.ARRAY_UNSIZED_BOTTOM);

        return newObject;
    }

    /** Consume one array from the input port and send a subarray to
     *  the output port, padding the subarray with zeros if necessary.
     *  @exception IllegalActionException If any parameter value
     *   is out of range.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        sourcePosition.update();
        extractLength.update();
        destinationPosition.update();
        outputArrayLength.update();

        if (input.hasToken(0)) {
            ArrayToken inputValue = (ArrayToken) input.get(0);
            Type inputElementType = inputValue.getElementType();
            Token[] inputArray = inputValue.arrayValue();
            int sourcePositionValue = ((IntToken) sourcePosition.getToken())
                    .intValue();
            int extractLengthValue = ((IntToken) extractLength.getToken())
                    .intValue();
            int destinationPositionValue = ((IntToken) destinationPosition
                    .getToken()).intValue();
            int outputArrayLengthValue = ((IntToken) outputArrayLength
                    .getToken()).intValue();

            try {
                Token[] outputArray = new Token[outputArrayLengthValue];

                Token zero = inputArray[0].zero();

                for (int i = 0; i < destinationPositionValue; i++) {
                    outputArray[i] = zero;
                }
                int j = sourcePositionValue;
                for (int i = destinationPositionValue; i < destinationPositionValue
                        + extractLengthValue; i++) {
                    outputArray[i] = inputValue.getElement(j++);
                }
                for (int i = destinationPositionValue + extractLengthValue; i < outputArrayLengthValue; i++) {
                    outputArray[i] = zero;
                }

                output.send(0, new ArrayToken(inputElementType, outputArray));
            } catch (IndexOutOfBoundsException ex) {
                throw new IllegalActionException(this,
                        "Parameter values out of range for the array supplied."
                                + "inputArray has length " + inputArray.length);
            }
        }
    }
}
