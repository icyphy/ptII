/* An actor that disassemble an ArrayToken to a multiport output.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.util.ArrayOfTypesFunction;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArrayToElements

/**
 <p>An actor that disassembles an ArrayToken to a multiport output.</p>
 <p>On each firing, this actor reads an ArrayToken from the input
 port and send out each element token to each channel of the output
 port. If the width of the output port (say, <i>n</i>) is less than
 the number of elements in the array (say <i>m</i>), then the first
 <i>n</i> elements in the array will be sent, and the remaining
 tokens are discarded. If <i>n</i> is greater than <i>m</i>, then
 the last <i>n-m</i> channels of the output port will never send
 tokens out.</p>
 <p>
 This actor is polymorphic. It can accept ArrayTokens with any element
 type and send out tokens corresponding to that type.
 </p>

 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayToElements extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be
     *   contained by the proposed container.
     *  @exception NameDuplicationException If the container
     *   already has an actor with this name.
     */
    public ArrayToElements(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set type constraints.
        output.setTypeAtLeast(ArrayType.elementType(input));

        output.setMultiport(true);

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class
     *   contains an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayToElements newObject = (ArrayToElements) super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.input));
        } catch (IllegalActionException e) {
            // Should have been caught before.
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** If there is a token at the input, read the ArrayToken
     *  from the input port, and for each channel <i>i</i> of
     *  the output port, send the <i>i</i>-th element of this
     *  array to this channel. Otherwise, do nothing.
     *  @exception IllegalActionException If a runtime
     *   type conflict occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);
            int size = token.length();

            int min = Math.min(size, output.getWidth());

            Token[] elements = token.arrayValue();

            for (int i = 0; i < min; i++) {
                output.send(i, elements[i]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Do not establish the usual default type constraints. Instead, the type
     *  of the output port is constrained to be no less than the type of the
     *  elements of the input array (set in the constructor of this class).
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    /** Add a type constraint for backward type inference that forces
     *  the input to be an array of which the elements have a type
     *  greater than or equal to the output port.
     *  If backward type inference is disabled, this method returns
     *  an empty set.
     *  @see ArrayOfTypesFunction
     *  @return A set of inequalities.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();
        if (isBackwardTypeInferenceEnabled()) {
            result.add(new Inequality(new ArrayOfTypesFunction(output), input
                    .getTypeTerm()));
        }
        return result;
    }
}
