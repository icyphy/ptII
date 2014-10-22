/* Replace an element in an array with a new value.

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
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayUpdate

/**
 Replace an element in an array with a new value.  This actor reads an array from the
 <i>input</i> port and sends a new array to the <i>output</i>
 port with the specified element replaced by the specified value.
 The type of the output array elements is the greater than or equal to
 the type of the elements of the input array and the replacement value.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class ArrayUpdate extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayUpdate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set parameters.
        index = new PortParameter(this, "index");
        index.setExpression("0");
        new StringAttribute(index.getPort(), "_cardinal")
                .setExpression("SOUTH");
        new Parameter(index.getPort(), "_showName").setExpression("true");

        value = new PortParameter(this, "value");
        value.setExpression("1");
        new StringAttribute(value.getPort(), "_cardinal")
                .setExpression("SOUTH");
        new Parameter(value.getPort(), "_showName").setExpression("true");

        // Set type constraints.
        input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        output.setTypeAtLeast(input);
        output.setTypeAtLeast(ArrayType.arrayOf(value));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The index into the input array at which to set the new value.
     *  This is a non-negative integer that defaults to 0, and is
     *  required to be less than the length of the input array.
     */
    public PortParameter index;

    /** The value to insert into the array at the position given by
     *  <i>index</i>.
     */
    public PortParameter value;

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
        ArrayUpdate newObject = (ArrayUpdate) super.clone(workspace);

        // Set the type constraints.
        newObject.input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        newObject.output.setTypeAtLeast(newObject.input);
        try {
            newObject.output.setTypeAtLeast(ArrayType.arrayOf(newObject.value));
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException(
                    "Failed to set type constraints on cloned actor.");
        }

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
        index.update();
        value.update();

        if (input.hasToken(0)) {
            ArrayToken inputValue = (ArrayToken) input.get(0);
            int indexValue = ((IntToken) index.getToken()).intValue();
            output.send(0, inputValue.update(indexValue, value.getToken()));
        }
    }
}
