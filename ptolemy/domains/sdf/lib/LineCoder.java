/* A line coder, which converts a sequence of booleans into symbols.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.math.SignalProcessing;

//////////////////////////////////////////////////////////////////////////
//// LineCoder
/**
A line coder, which converts a sequence of booleans into symbols.

@author Edward A. Lee
@version $Id$
*/

public class LineCoder extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LineCoder(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.BOOLEAN);

        output = new SDFIOPort(this, "output", false, true);
        // FIXME: Type should be inferred from the code table.
        output.setTypeEquals(BaseType.DOUBLE);

        double[][] deftbl = {{-1.0, 1.0}};
        table = new Parameter(this, "table", new DoubleMatrixToken(deftbl));
        wordlength = new Parameter(this, "wordlength", new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;

    /** The code table.  Its value is a token of type DoubleMatrixToken.
     *  The table contains a row vector (a matrix with only one row)
     *  that provides the symbol values to produce on the output.
     *  The number of columns of this table must be at least
     *  2<sup><i>wordlength</i></sup>, or an exception
     *  will be thrown.  The number of tokens consumed by this actor when
     *  it fires is log<sub>2</sub>(<i>tablesize</i>), where
     *  <i>tablesize</i> is the length of the table.  If all of these
     *  values are <i>false</i>, then the first table entry is produced
     *  as an output.  If only the first one is true, then then second
     *  table value is produced.  In general, the <i>N</i> inputs consumed
     *  are taken to be a binary digit that indexes the table,
     *  where the first input is taken to be the low-order bit of the index.
     *  The default code table has two entries, -1.0
     *  and 1.0, so that input <i>false</i> values are mapped to -1.0,
     *  and input <i>true</i> values are mapped to +1.0.
     */
    public Parameter table;

    // FIXME: This table should be allowed to have any Matrix type.

    /** The word length is the number of boolean inputs that are consumed
     *  to construct an index into the table.  Its value is an IntToken,
     *  with default value one.
     */
    public Parameter wordlength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the public ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            LineCoder newobj = (LineCoder)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");
            newobj.table = (Parameter)newobj.getAttribute("table");
            newobj.wordlength = (Parameter)newobj.getAttribute("wordlength");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Consume the inputs and produce the corresponding symbol.
     *  @exception IllegalActionException Not Thrown.
     */
    public void fire() throws IllegalActionException {
        int tableaddress = 0;
        for (int i = 0; i < _wordlength; i++) {
            boolean data = ((BooleanToken)(input.get(0))).booleanValue();
            if (data) {
                tableaddress |= 1 << i;
            }
        }
        output.send(0, new DoubleToken(_table[tableaddress]));
    }

    /** Set up the consumption constant.
     *  @exception IllegalActionException If the length of the table is not
     *   a power of two.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // FIXME: Handle mutations.
        _wordlength = ((IntToken)(wordlength.getToken())).intValue();
        input.setTokenConsumptionRate(_wordlength);

        DoubleMatrixToken tabletoken = (DoubleMatrixToken)(table.getToken());
        if (tabletoken.getRowCount() != 1) {
            throw new IllegalActionException(this, "Table parameter is " +
                    "required to have exactly one row.");
        }
        // FIXME: Check that table is at least 2^wordlength.
        _table = new double[tabletoken.getColumnCount()];
        for (int i = 0; i < _table.length; i++) {
            _table[i] = tabletoken.getElementAt(0, i);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Local cache of these parameter values.
    private int _wordlength;
    private double[] _table;
}
