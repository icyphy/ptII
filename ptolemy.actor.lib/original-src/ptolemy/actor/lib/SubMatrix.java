/* Extract a submatrix from an input matrix.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SubMatrix

/**
 This actor extract a submatrix from an input matrix.
 For example, if the input matrix is
 <pre>
   1  2  3
   4  5  6
   7  8  9
 </pre>
 then the parameters <i>row</i> = 1,
 <i>column</i> = 0,
 <i>rowSpan</i> = 2, and
 <i>columnSpan</i> = 1,
 yield the following submatrix
 <pre>
   4
   7
 </pre>
 If the submatrix specified is not entirely
 contained by the input matrix, or if the resulting submatrix
 is empty, then this actor will throw an exception.

 @author Edward Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class SubMatrix extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SubMatrix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        row = new PortParameter(this, "row");
        row.setTypeEquals(BaseType.INT);
        row.setExpression("0");

        column = new PortParameter(this, "column");
        column.setTypeEquals(BaseType.INT);
        column.setExpression("0");

        rowSpan = new PortParameter(this, "rowSpan");
        rowSpan.setTypeEquals(BaseType.INT);
        rowSpan.setExpression("1");

        columnSpan = new PortParameter(this, "columnSpan");
        columnSpan.setTypeEquals(BaseType.INT);
        columnSpan.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The starting column number. This is an integer that
     *  defaults to 0.
     */
    public PortParameter column;

    /** The span of columns. This is an integer that defaults
     *  to 1.
     */
    public PortParameter columnSpan;

    /** The starting row number. This is an integer that
     *  defaults to 0.
     */
    public PortParameter row;

    /** The span of rows. This is an integer that defaults
     *  to 1.
     */
    public PortParameter rowSpan;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the input matrix and produce the output matrix.
     *  @exception IllegalActionException If the submatrix is
     *   empty (e.g., row and column are out of range).
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (!input.hasToken(0)) {
            return;
        }
        row.update();
        column.update();
        rowSpan.update();
        columnSpan.update();
        int columnValue = ((IntToken) column.getToken()).intValue();
        int rowValue = ((IntToken) row.getToken()).intValue();
        int columnSpanValue = ((IntToken) columnSpan.getToken()).intValue();
        int rowSpanValue = ((IntToken) rowSpan.getToken()).intValue();

        // FIXME: We are not enforcing that the input is a matrix.
        // How to do this?
        MatrixToken inputValue = (MatrixToken) input.get(0);
        output.send(0, inputValue.crop(rowValue, columnValue, rowSpanValue,
                columnSpanValue));
    }
}
