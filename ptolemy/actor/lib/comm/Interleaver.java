/* Interleave a sequence of binary bits.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Interleaver
/**
This actor interleaves a sequence of binary bits.
It reads <i>rows</i> times <i>columns</i> bits from the input port and
inserts them into an integer matrix with <i>rows</i> rows and <i>columns</i>
columns. The first row is filled first, then the second row, etc.
The bits are then sent to the output in the order of columns. The first
columns is sent first, then the second column, etc.
<p>
@author Rachel Zhou
@version $Id$
*/

public class Interleaver extends Transformer {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Interleaver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        rows = new Parameter(this, "rows");
        rows.setTypeEquals(BaseType.INT);
        rows.setExpression("2");

        columns = new Parameter(this, "columns");
        columns.setTypeEquals(BaseType.INT);
        columns.setExpression("2");

        // Declare data types.
        input.setTypeEquals(BaseType.INT);
        _inputRate = new Parameter(input, "tokenConsumptionRate",
                new IntToken(1));
        output.setTypeEquals(BaseType.INT);
        _outputRate = new Parameter(output, "tokenProductionRate",
                new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of rows. It is of type integer and has a
     *  default value of 2. It must be greater than zero.
     */
    public Parameter rows;

    /** The number of columns. It is of type integer and has a
     *  default value of 2. It must be greater than zero.
     */
    public Parameter columns;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>rows</i> or <i>columns</i>,
     *  then set the rate of the input port and output port.
     *  @exception IllegalActionException If the parameters is not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rows || attribute == columns) {
            _rowsValue = ((IntToken)rows.getToken()).intValue();
            _columnsValue = ((IntToken)columns.getToken()).intValue();
            if (_columnsValue < 1 || _rowsValue < 1) {
                throw new IllegalActionException(this,
                        "Number of rows and columns must be positive.");
            }
            _matrixSize = _rowsValue * _columnsValue;
            _inputRate.setToken(new IntToken(_matrixSize));
            _outputRate.setToken(new IntToken(_matrixSize));
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume consecutive input bits and produce them in
     *  the interleaved order.
     */
    public void fire() throws IllegalActionException {
        Token[] inputToken = (Token[])input.get(0, _matrixSize);
        IntToken[] result = new IntToken[_matrixSize];
        for (int i = 0; i < _rowsValue; i++) {
            for (int j = 0; j< _columnsValue; j++) {
                IntToken out = (IntToken)inputToken[i * _columnsValue + j];
                if (out.intValue() != 0 && out.intValue() != 1) {
                    throw new IllegalActionException(this,
                            "Input must be either 0 or 1.");
                }
                result[j * _rowsValue + i] = out;
            }
        }
        output.broadcast(result, result.length);
    }

    //////////////////////////////////////////////////////////////
    ////           private parameters and variables           ////

    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // The number of rows.
    private int _rowsValue;

    // The number of columns.
    private int _columnsValue;

    // Number of elements of the interleaving matrix.
    private int _matrixSize;
}
