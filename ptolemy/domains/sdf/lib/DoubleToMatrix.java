/* An actor that converts a sequence of input tokens to a matrix.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////
/// DoubleToMatrix
/**
This actor converts a sequence of input tokens to a matrix.
The actor reads <i>rows</i> times <i>columns</i> inputs and
inserts their values into a double matrix with <i>rows</i> rows
and <i>columns</i> columns. The first row is filled first, then
the second row, then the third, etc.

<p>Note that this actor is not likely to work in the CT domain, use
actor.lib.VectorAssembler instead.

@author Edward A. Lee
@deprecated Use SequenceToMatrix instead.
@version $Id$
@since Ptolemy II 2.0
*/

public class DoubleToMatrix extends SDFConverter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoubleToMatrix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        rows = new Parameter(this, "rows");
        rows.setExpression("2");
        rows.setTypeEquals(BaseType.INT);
        columns = new Parameter(this, "columns");
        columns.setExpression("2");
        columns.setTypeEquals(BaseType.INT);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);

        input_tokenConsumptionRate.setExpression("rows * columns");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of columns.  It is of type integer and has a default
     *  value of 2.  It must be greater than zero.
     */
    public Parameter columns;

    /** The number of rows.  It is of type integer and has a
     *  default value of 2.  It must be greater than zero.
     */
    public Parameter rows;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is <i>rows</i> or <i>columns</i>, then
     *  set the rate of the <i>input</i> port, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rows || attribute == columns) {
            _rows = ((IntToken)rows.getToken()).intValue();
            _columns = ((IntToken)columns.getToken()).intValue();
            if (_rows <= 0 || _columns <= 0) {
                throw new IllegalActionException(this,
                        "Number of rows and columns is required to be positive.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume consecutive input tokens and produce the output matrix.
     *  @exception IllegalActionException If there is no director.
     */
    public final void fire() throws IllegalActionException  {
        super.fire();
        double[][] result = new double[_rows][_columns];
        for (int i = 0; i < _rows; i++) {
            Token[] row = input.get(0, _columns);
            for (int j = 0; j < _columns; j++) {
                result[i][j] = ((DoubleToken)row[j]).doubleValue();
            }
        }
        output.send(0, new DoubleMatrixToken(
                result, DoubleMatrixToken.DO_NOT_COPY));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The number of rows.
    private int _rows;

    // The number of columns.
    private int _columns;
}
