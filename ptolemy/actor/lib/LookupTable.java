/* A lookup table that outputs internally stored data given an index Parameter.

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
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// LookupTable

/**
 Output to the <i>output</i> port the value in the array of tokens
 specified by the <i>table</i> parameter at the index specified by the
 <i>input</i> port.  The index must be an integer.  If the index is out
 of range, no token produced.

 <p>LookupTable is different from ArrayElement in that in
 ArrayElement, the array is read in as input, and the index is a parameter,
 In LookupTable, the array is a parameter, and the index is read
 in as an input.

 <p>Note that there are three similar actors here
 <dl>
 <dt>LookupTable
 <dd>array is a parameter, index is a port.
 <dt>ArrayElement
 <dd>array is a port, index is a parameter
 <dt>Array<i>XXX</i> (not yet developed)
 <dd>array and index are both ports
 </dl>

 @see ArrayElement
 @author Paul Whitaker, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class LookupTable extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LookupTable(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set parameters.
        table = new Parameter(this, "table");
        table.setExpression("{0, 1}");

        // Set type constraints.
        input.setTypeEquals(BaseType.INT);
        table.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        ArrayType tableType = (ArrayType) table.getType();
        InequalityTerm elemTerm = tableType.getElementTypeTerm();
        output.setTypeAtLeast(elemTerm);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The table array that we look up elements in.  This parameter
     *  is an array with default value {0, 1}.
     */
    public Parameter table;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets up the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LookupTable newObject = (LookupTable) (super.clone(workspace));

        // One of the Parameters contains ArrayTokens, so we need
        // to handle cloning it.
        ArrayType tableType = (ArrayType) newObject.table.getType();
        InequalityTerm elemTerm = tableType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elemTerm);

        return newObject;
    }

    /** Consume at most one token from the input port and produce
     *  the element at the index specified by this token from the
     *  table array on the output port.  If there is no token
     *  on the input or the token is out of range, then no output
     *  is produced.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) table.getToken();

            if (token != null) {
                int indexValue = ((IntToken) input.get(0)).intValue();

                if ((indexValue >= 0) && (indexValue < token.length())) {
                    output.broadcast(token.getElement(indexValue));
                }
            }
        }
    }
}
