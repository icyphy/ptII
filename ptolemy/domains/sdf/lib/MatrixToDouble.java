/* An actor that converts a matrix to sequence of output tokens.

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
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////
/// MatrixToDouble
/**
This actor converts a matrix input sequence of output tokens.
The input must be a DoubleMatrixToken and the output will be a sequence
of instances of DoubleToken.  The number of outputs produced
on each firing is the number of elements in the matrix.
This is assumed to equal the product <i>rows</i> times <i>columns</i>,
although this is not enforced unless the actor is under the control
of an instance of SDFDirector.  The SDF director requires this information
to construct its schedule. The first row is produced first, then
the second row, then the third, etc.

<p>Note that this actor is not likely to work in the CT domain, use
actor.lib.VectorDisassembler instead.

@author Edward A. Lee
@deprecated Use MatrixToSequence instead.
@version $Id$
@since Ptolemy II 2.0
*/

public class MatrixToDouble extends SDFConverter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixToDouble(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        rows = new Parameter(this, "rows");
        rows.setExpression("2");
        rows.setTypeEquals(BaseType.INT);
        
        columns = new Parameter(this, "columns");
        columns.setExpression("2");
        columns.setTypeEquals(BaseType.INT);

        output.setTypeEquals(BaseType.DOUBLE);
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);

        output_tokenProductionRate.setExpression("rows * columns");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of columns.  It is of type integer and has a default
     *  value of 2.  It must be greater than zero. This information is
     *  only used if the actor is under the control of an SDFDirector.
     */
    public Parameter columns;

    /** The number of rows.  It is of type integer and has a default
     *  value of 2.  It must be greater than zero. This information is
     *  only used if the actor is under the control of an SDFDirector.
     */
    public Parameter rows;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is <i>rows</i> or <i>columns</i>, then
     *  set the rate of the output port, and invalidate
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

    /** Consume a matrix input and produce consecutive output tokens.
     *  @exception IllegalActionException If there is no director, or if
     *   the director is an SDFDirector and the number of rows and columns
     *   of the input matrix does not match the declared parameter values.
     */
    public final void fire() throws IllegalActionException  {
        super.fire();
        DoubleMatrixToken matrix = (DoubleMatrixToken)input.get(0);
        int inputRows = matrix.getRowCount();
        int inputColumns = matrix.getColumnCount();
        // If the director is an SDFDirector, check the dimensions
        // of the matrix.
        Director director = getDirector();
        if (director instanceof SDFDirector) {
            if (inputRows * inputColumns != _rows * _columns) {
                throw new IllegalActionException(this,
                        "Received a matrix whose dimension does not "
                        + "match the declared dimensions.");
            }
        }
        int totalSize = inputRows * inputColumns;
        DoubleToken[] result = new DoubleToken[totalSize];
        int k = 0;
        for (int i = 0; i < inputRows; i++) {
            for (int j = 0; j < inputColumns; j++) {
                result[k++] = (DoubleToken)matrix.getElementAsToken(i,j);
            }
        }
        output.send(0, result, totalSize);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The number of rows.
    private int _rows;

    // The number of columns.
    private int _columns;
}
