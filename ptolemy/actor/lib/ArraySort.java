/* An actor that sorts the elements of an array.

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import java.util.ArrayList;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ArraySort
/**
Sort the elements of an input array.  This actor reads an array from the 
<i>input</i> port and sends a sorted array to the <i>output</i> port.
The input array is required to contain either strings or non-complex
scalars, or a type error will occur. The output array will have the
same type as the input and can can be sorted in either ascending or
descending order. Duplicate entries can be optionally removed.

@author Mark Oliver, Edward A. Lee
@version $ID: ArraySort.java,v1.0 2003/07/09
@since Ptolemy II 3.1
*/

public class ArraySort extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArraySort(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set Type Constraints.
        input.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        output.setTypeAtLeast(input);

        // NOTE: Consider constraining input element types.
        // This is a bit complicated to do, however.

        // Set Parameters.
        allowDuplicates = new Parameter(this, "allowDuplicates");
        allowDuplicates.setExpression("true");
        allowDuplicates.setTypeEquals(BaseType.BOOLEAN);

        ascending = new Parameter(this, "ascending");
        ascending.setExpression("true");
        ascending.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Tells the actor whether or not to remove duplicate elements.
     *  This is a boolean that defaults to true.
     */
    public Parameter allowDuplicates;

    /** The sort order attribute.  This tells the actor whether to
     *  sort the elements in ascending or descending order. It is a
     *  boolean that defaults to true, which means ascending order.
     */
    public Parameter ascending;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one array from the input port and produce
     *  a sorted array on the <i>output</i> port.  
     *  If there is no token on the input, then no output is produced.
     *  If the input is an empty array, then the same empty array token
     *  is produced on the output.
     *  @exception IllegalActionException If there is no director, or
     *   if sorting is not supported for the input array.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);
            if (token.length() == 0) {
                output.send(0, token);
                return;
            }
            boolean ascendingValue =
                ((BooleanToken) ascending.getToken()).booleanValue();
            ArrayToken result = null;
            try {
                if (ascendingValue) {
                    result = UtilityFunctions.sort(token);
                } else {
                    result = UtilityFunctions.sortDescending(token);
                }
            } catch (ClassCastException ex) {
                // The call to sort() above throws ClassCastException if
                // sorting is not supported.  This is not ideal, so we
                // remap this to IllegalActionException.
                throw new IllegalActionException(this, ex.getMessage());
            }
            boolean allowDuplicatesValue =
                ((BooleanToken) allowDuplicates.getToken()).booleanValue();
            if (!allowDuplicatesValue) {
                // Strip out duplicates.
                ArrayList list = new ArrayList();
                Token previous = result.getElement(0);
                list.add(previous);
                for (int i = 1; i < result.length(); i++) {
                    Token next = result.getElement(i);
                    if (!next.isEqualTo(previous).booleanValue()) {
                        list.add(next);
                        previous = next;
                    }
                }
                // Dummy array to give the run-time type to toArray().
                Token[] dummy = new Token[0];
                result = new ArrayToken((Token[])list.toArray(dummy));
            }
            output.send(0, result);
        }
    }
}
