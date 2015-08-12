/* Removes element occurrences from an array.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayRemoveElement
/**
 Remove occurrences of a specified element from an array.
 This actor reads an array from the <i>array</i> input port and
 an element from the <i>element</i> port-parameter and
 removes all occurances that match the element from the array.
 The output may be an empty array, in which case it will have
 the same type as the input.

 @author Efrat Jaeger and Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0.1
 */

public class ArrayRemoveElement extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayRemoveElement(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        array = new TypedIOPort(this, "array", true, false);
        output = new TypedIOPort(this, "output", false, true);

        // Set parameters.
        element = new PortParameter(this, "element");
        new Parameter(element.getPort(), "_showName", BooleanToken.TRUE);

        array.setTypeAtLeast(ArrayType.arrayOf(element));
        output.setTypeAtLeast(array);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Input array. The type of this port is at least an array
     *  of the type of the <i>element</i> port.
     */
    public TypedIOPort array;

    /** The resulting output array. Note that the output will
     *  be a new array with the same type as the input array.
     */
    public TypedIOPort output;

    /** The element to be removed.
     */
    public PortParameter element;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayRemoveElement newObject = (ArrayRemoveElement) super
                .clone(workspace);
        try {
            newObject.array
            .setTypeAtLeast(ArrayType.arrayOf(newObject.element));
            newObject.output.setTypeAtLeast(newObject.array);
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException("Clone failed: " + e);
        }
        return newObject;
    }

    /** If there is an <i>array</i> input, consume it and create a new
     *  array that contains all elements of the input that are not equal
     *  to the value given by the <i>element</i> port-parameter.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // NOTE: This has be outside the if because we need to ensure
        // that if an element token is provided that it is consumed even
        // if there is no input token.
        element.update();
        Token elementToken = element.getToken();
        if (array.hasToken(0)) {
            ArrayToken inputArray = (ArrayToken) array.get(0);
            Token[] outputElements = new Token[inputArray.length()];
            int outputSize = 0;
            for (int i = 0; i < inputArray.length(); i++) {
                Token inputElement = inputArray.getElement(i);
                if (!elementToken.equals(inputElement)) {
                    outputElements[outputSize] = inputElement;
                    outputSize++;
                }
            }
            Token result = null;
            if (outputSize > 0) {
                result = new ArrayToken(outputElements, outputSize);
            } else {
                result = new ArrayToken(inputArray.getElementType());
            }
            output.broadcast(result);
        }
    }

    /** Clear port parameter value.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        element.setExpression("");
    }
}
