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
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayAccumulate

/**
 An actor that accumulates input arrays into a growing array that
 includes the contents of all input arrays.  Upon firing, this actor reads
 an input array, appends it to the accumulating array, and outputs
 the new array. The length of the output array grows by the size
 of the input array on each firing.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class ArrayAccumulate extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayAccumulate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
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
        ArrayAccumulate newObject = (ArrayAccumulate) super.clone(workspace);

        // Set the type constraints.
        newObject.input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(ArrayType.ARRAY_UNSIZED_BOTTOM);
        newObject._arrays = new ArrayToken[2];
        return newObject;
    }

    /** Consume at most one ArrayToken from the input, append it
     *  to the accumulating token, and produce the accumulated result
     *  on the output.
     *  @exception IllegalActionException If a runtime type conflict occurs,
     *   or if there are no input channels.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // NOTE: Do not use System.arraycopy here because the
        // arrays being appended may be subclasses of ArrayToken,
        // so values have to be accessed via the getElement() method,
        // which is overridden in the subclasses. Use the append()
        // method of ArrayToken instead.
        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);
            if (_accumulating == null) {
                _tentativeAccumulating = token;
                output.send(0, token);
            } else {
                _arrays[0] = _accumulating;
                _arrays[1] = token;
                _tentativeAccumulating = ArrayToken.append(_arrays);
                output.send(0, ArrayToken.append(_arrays));
            }
        } else {
            Type elementType = ((ArrayType) output.getType()).getElementType();
            output.send(0, new ArrayToken(elementType));
        }
    }

    /** Initialize this actor to have an empty accumulating array.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _accumulating = null;
        _tentativeAccumulating = null;
    }

    /** Record the accumulating array and return true.
     *  @return True.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        _accumulating = _tentativeAccumulating;
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The accumulating array. */
    private ArrayToken _accumulating;

    /** An array to use (repeatedly) to append arrays. */
    private ArrayToken[] _arrays = new ArrayToken[2];

    /** The tentative accumulating array. */
    private ArrayToken _tentativeAccumulating;
}
