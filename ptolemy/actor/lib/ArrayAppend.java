/* Append arrays together to form a larger array.

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

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayAppend

/**
 An actor that appends ArrayTokens together.  This actor has a single input
 multiport, and a single output port.  The types on the input and the output
 port must both be the same array type.  During each firing, this actor reads
 up to one ArrayToken from each channel of the input port and creates an
 ArrayToken of the same type on the output port.  If no token is available on
 a particular channel, then there will be no contribution to the output.
 The output is an array of length equal to the sum of the lengths of
 the input arrays (which may be zero if either there are no input
 arrays or the lengths of the input arrays are all zero).

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (celaine)
 @Pt.AcceptedRating Green (cxh)
 */
public class ArrayAppend extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayAppend(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // The input is a multiport.
        input.setMultiport(true);

        // Set type constraints.
        input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        output.setTypeAtLeast(input);
        output.setTypeAtLeast(ArrayType.ARRAY_UNSIZED_BOTTOM);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayAppend newObject = (ArrayAppend) super.clone(workspace);

        // Set the type constraints.
        newObject.input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(ArrayType.ARRAY_UNSIZED_BOTTOM);
        return newObject;
    }

    /** Consume at most one ArrayToken from each channel of the input port
     *  and produce a single ArrayToken on the output
     *  port that contains all of the tokens contained in all of the
     *  arrays read from the input. If all input arrays are empty,
     *  or if there are no input arrays, then output an empty array
     *  of the appropriate type.
     *  @exception IllegalActionException If a runtime type conflict occurs,
     *   or if there are no input channels.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int width = input.getWidth();
        if (width == 0) {
            throw new IllegalActionException(this, "No input channels.");
        }
        // NOTE: Do not use System.arraycopy here because the
        // arrays being appended may be subclasses of ArrayToken,
        // so values have to be accessed via the getElement() method,
        // which is overridden in the subclasses. Use the append()
        // method of ArrayToken instead.
        ArrayToken[] arraysToAppend = new ArrayToken[width];
        int resultWidth = 0;
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                try {
                    ArrayToken arrayToken = (ArrayToken) token;
                    arraysToAppend[i] = arrayToken;
                    resultWidth += arrayToken.length();
                } catch (ClassCastException ex) {
                    throw new IllegalActionException(this, ex, "Cannot cast \""
                            + token + "\" to an ArrayToken");
                }
            }
        }
        if (resultWidth > 0) {
            output.send(0, ArrayToken.append(arraysToAppend));
        } else {
            Type elementType = ((ArrayType) input.getType()).getElementType();
            output.send(0, new ArrayToken(elementType));
        }
    }
}
