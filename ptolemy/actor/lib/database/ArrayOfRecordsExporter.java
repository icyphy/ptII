/* Export an array of records that arrives on the input port to file.

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
package ptolemy.actor.lib.database;

import java.util.Iterator;

import ptolemy.actor.lib.io.LineWriter;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// ArrayOfRecordsExporter

/**
 Export an array of tokens to a file.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayOfRecordsExporter extends LineWriter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayOfRecordsExporter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Clear type constraint set by base class.
        input.setTypeEquals(BaseType.UNKNOWN);
        input.setMultiport(false);
        // Force the type to contain at least a record.
        input.setTypeAtMost(new ArrayType(RecordType.EMPTY_RECORD));

        records = new Parameter(this, "records");
        // Declare that it must be an array of records.
        records.setTypeAtMost(new ArrayType(RecordType.EMPTY_RECORD));
        records.setToken(new ArrayToken(RecordType.EMPTY_RECORD));

        Variable ALL = new Variable(this, "ALL");
        ALL.setVisibility(Settable.NONE);
        Token emptyStringArray = new ArrayToken(BaseType.STRING);
        ALL.setToken(emptyStringArray);

        columns = new Parameter(this, "columns");
        columns.setTypeEquals(new ArrayType(BaseType.STRING));
        columns.setExpression("ALL");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The columns to write.
     *  This is an array of strings specifying the column
     *  names to write, and the order in which they are
     *  written. It defaults to ALL, which indicates
     *  that all fields should be written. In this case,
     *  the columns are organized alphabetically.
     */
    public Parameter columns;

    /** Parameter to store the array of records read at the input.
     *  This is an array of records that is by default empty.
     */
    public Parameter records;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write the specified token to the current writer.
     *  This is protected so that derived classes can modify the
     *  format in which the token is written.
     *  @param token The token to write.
     *  @exception IllegalActionException If the input token cannot be read.
     */
    @Override
    protected void _writeToken(Token token) throws IllegalActionException {
        ArrayToken castToken = (ArrayToken) token;
        ArrayToken columnsValue = (ArrayToken) columns.getToken();
        for (int row = 0; row < castToken.length(); row++) {
            RecordToken record = (RecordToken) castToken.getElement(row);
            if (columnsValue.length() == 0) {
                // Display all columns.
                // FIXME: This really requires every record to be identical!
                // If we are in the first row, display the column names first.
                if (row == 0) {
                    Iterator labelSet = record.labelSet().iterator();
                    int column = 0;
                    while (labelSet.hasNext()) {
                        String columnName = (String) labelSet.next();
                        if (column > 0) {
                            // FIXME: Parameter for separator
                            _writer.print(",");
                        }
                        _writer.print(columnName);
                        column++;
                    }
                    _writer.print("\n");
                }
                Iterator labelSet = record.labelSet().iterator();
                int column = 0;
                while (labelSet.hasNext()) {
                    String columnName = (String) labelSet.next();
                    if (column > 0) {
                        // FIXME: Parameter for separator
                        _writer.print(",");
                    }
                    _writer.print(record.get(columnName));
                    column++;
                }
                _writer.print("\n");
            } else {
                // Display specified fields.
                /* FIXME
                for (int j = 0; j < columnsValue.length(); j++) {
                    if (j >= numColumns) {
                        break;
                    }
                    String column = ((StringToken)fieldsValue.getElement(j)).stringValue();
                    tableElement[i][j] = _labelFigure((RecordToken)row, column);
                    Rectangle2D bounds = tableElement[i][j].getBounds();
                    double width = bounds.getWidth();
                    if (width > columnWidth[j]) {
                        columnWidth[j] = width;
                    }
                    double height = bounds.getHeight();
                    if (height > rowHeight) {
                        rowHeight = height;
                    }
                }
                 */
            }
        }
    }
}
