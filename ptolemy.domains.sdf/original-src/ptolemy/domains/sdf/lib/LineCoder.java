/* A line coder, which converts a sequence of booleans into symbols.

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
package ptolemy.domains.sdf.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// LineCoder

/**
 A line coder, which converts a sequence of booleans into symbols.

 @author Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class LineCoder extends SDFTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LineCoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.BOOLEAN);

        table = new Parameter(this, "table");
        table.setExpression("{-1.0, 1.0}");
        attributeChanged(table);

        wordLength = new Parameter(this, "wordLength", new IntToken(1));
        wordLength.setTypeEquals(BaseType.INT);

        // Type constraints.
        output.setTypeAtLeast(ArrayType.elementType(table));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The code table.  Its value is a token of type ArrayToken.
     *  The array provides the symbol values to produce on the output.
     *  The number of values in this array must be at least
     *  2<sup><i>wordLength</i></sup>, or an exception
     *  will be thrown.  The number of tokens consumed by this actor when
     *  it fires is log<sub>2</sub>(<i>tableSize</i>), where
     *  <i>tableSize</i> is the length of the table.  If all of these
     *  values are <i>false</i>, then the first array entry is produced
     *  as an output.  If only the first one is true, then then second
     *  array value is produced.  In general, the <i>N</i> inputs consumed
     *  are taken to be a binary digit that indexes the array,
     *  where the first input is taken to be the low-order bit of the array.
     *  The default code table has two entries, -1.0
     *  and 1.0, so that input <i>false</i> values are mapped to -1.0,
     *  and input <i>true</i> values are mapped to +1.0.
     */
    public Parameter table;

    /** The word length is the number of boolean inputs that are consumed
     *  to construct an index into the table.  Its value is an IntToken,
     *  with default value one.
     */
    public Parameter wordLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LineCoder newObject = (LineCoder) super.clone(workspace);

        // set the type constraints
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.table));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        return newObject;
    }

    /** Consume the inputs and produce the corresponding symbol.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int tableAddress = 0;
        Token[] tokens = input.get(0, _wordLength);

        for (int i = 0; i < _wordLength; i++) {
            boolean data = ((BooleanToken) tokens[i]).booleanValue();

            if (data) {
                tableAddress |= 1 << i;
            }
        }

        output.send(0, _table[tableAddress]);
    }

    /** Set up the consumption constant.
     *  @exception IllegalActionException If the length of the table is not
     *   a power of two.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _wordLength = ((IntToken) wordLength.getToken()).intValue();

        // Set the token consumption rate.
        input_tokenConsumptionRate.setToken(new IntToken(_wordLength));

        ArrayToken tableToken = (ArrayToken) table.getToken();
        int size = (int) Math.pow(2, _wordLength);

        if (tableToken.length() < size) {
            throw new IllegalActionException(this, "Table parameter must "
                    + "have at least " + size + " entries, but only has "
                    + tableToken.length());
        }

        _table = tableToken.arrayValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Local cache of these parameter values.
    private int _wordLength;

    private Token[] _table;
}
