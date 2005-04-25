/* Append arrays together to form a larger array.

Copyright (c) 1998-2005 The Regents of the University of California.
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
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// ArrayAppend

/**
   An actor that appends ArrayTokens together.  This actor has a single input
   multiport, and a single output port.  The types on the input and the output
   port must both be the same array type.  During each firing, this actor reads
   up to one ArrayToken from each channel of the input port and creates an
   ArrayToken of the same type on the output port.  If no token is available on
   a particular channel, then there will be no contribution to the output.

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
        input.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        output.setTypeAtLeast(input);
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayAppend newObject = (ArrayAppend) (super.clone(workspace));

        // Set the type constraints.
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Consume at most one ArrayToken from each channel of the input port
     *  and produce a single ArrayToken on the output
     *  port that contains all of the tokens contained in all of the
     *  arrays read from the input.
     *  @exception IllegalActionException If a runtime type conflict occurs.
     */
    public void fire() throws IllegalActionException {
        // NOTE: This is efficient for 2 or 3 input channels, but for
        // many channels it ends up copying each array twice.
        Token[] array = null;

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                ArrayToken token = (ArrayToken) input.get(i);

                if (array == null) {
                    array = token.arrayValue();
                } else {
                    Token[] newArray = new Token[array.length + token.length()];
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    System.arraycopy(token.arrayValue(), 0, newArray,
                            array.length, token.length());
                    array = newArray;
                }
            }
        }

        output.send(0, new ArrayToken(array));
    }
}
