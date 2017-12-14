/* Determine whether an array contains an element.

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
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayContains
/**
 Determine whether an element is contained in an array.
 This actor reads an array from the <i>array</i>
 input port and an element from the <i>element</i>
 port parameter and outputs true if the element is contained by
 the array.

 @author Efrat Jaeger and Edward A. Lee
 @version $Id$
 */

public class ArrayContains extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayContains(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        array = new TypedIOPort(this, "array", true, false);
        output = new TypedIOPort(this, "output", false, true);

        // Set parameters.
        element = new PortParameter(this, "element");
        new Parameter(element.getPort(), "_showName", BooleanToken.TRUE);

        // set type constraints.
        array.setTypeAtLeast(ArrayType.arrayOf(element));
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Input array. The type of this port is at least an array
     *  of the type of the <i>element</i> port.
     */
    public TypedIOPort array;

    /** Boolean output specifying whether the element is contained in
     *         the array.
     */
    public TypedIOPort output;

    /** The element to test for presence in the array.
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
        ArrayContains newObject = (ArrayContains) super.clone(workspace);
        try {
            newObject.array
                    .setTypeAtLeast(ArrayType.arrayOf(newObject.element));
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException("Clone failed: " + e);
        }
        return newObject;
    }

    /** If there is an array input, then check to see whether it
     *  contains the element given by the <i>element</i> port-parameter,
     *  and output true or false accordingly.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // NOTE: This has be outside the if because we need to ensure
        // that if an element token is provided that it is consumed even
        // if there is no array token.
        element.update();
        Token elementToken = element.getToken();
        if (array.hasToken(0)) {
            ArrayToken token = (ArrayToken) array.get(0);
            boolean contained = false;
            for (int i = 0; i < token.length(); i++) {
                BooleanToken bt = elementToken.isEqualTo(token.getElement(i));
                if (bt.booleanValue()) {
                    contained = true;
                    break;
                }
            }
            output.broadcast(new BooleanToken(contained));
        }
    }
}
