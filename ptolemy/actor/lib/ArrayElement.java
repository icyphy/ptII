/* Extract an element from an array.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Green (celaine@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ArrayElement
/**
Extract an element from an array.  This actor reads an array from the
<i>input</i> port and sends one of its elements to the <i>output</i>
port.  The element that is extracted is determined by the
<i>index</i> parameter.  It is required that 0 &lt;= <i>index</i> &lt;
<i>N</i>, where <i>N</i> is the length of the input array, or
an exception will be thrown by the fire() method.

@see LookupTable
@see RecordDisassembler
@author Edward A. Lee, Elaine Cheong
@version $Id$
*/

public class ArrayElement extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayElement(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	// Set type constraints.
	input.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
	ArrayType inputType = (ArrayType)input.getType();
	InequalityTerm elementTerm = inputType.getElementTypeTerm();
	output.setTypeAtLeast(elementTerm);

        // Set parameters.
        index = new Parameter(this, "index");
        index.setExpression("0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The index into the input array.  This is an integer that
     *  defaults to 0, and is required to be less than or equal to the
     *  length of the input array.
     */
    public Parameter index;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets up the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
	    throws CloneNotSupportedException {
        ArrayElement newObject = (ArrayElement)(super.clone(workspace));

        // Set the type constraints.
        ArrayType inputType = (ArrayType)newObject.input.getType();
        InequalityTerm elementTerm = inputType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm);

        return newObject;
    }

    /** Consume at most one array from the input port and produce
     *  one of its elements on the output port.  If there is no token
     *  on the input, then no output is produced.
     *  @exception IllegalActionException If the <i>index</i> parameter
     *   is out of range.
     */
    public void fire() throws IllegalActionException {
        if(input.hasToken(0)) {
            ArrayToken token = (ArrayToken)input.get(0);
            int indexValue = ((IntToken)index.getToken()).intValue();
            if (indexValue < 0 || indexValue >= token.length()) {
                throw new IllegalActionException(this,
                        "index " + indexValue + " is out of range for the input "
                        + "array, which has length " + token.length());
            }
            output.send(0, token.getElement(indexValue));
        }
    }
}

