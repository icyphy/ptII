/* A graphical component displaying matrix contents.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.actor.gui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import ptolemy.data.MatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// MatrixPane

/**
 A graphical component that displays the values in a matrix.
 The data to display is supplied in the form of a MatrixToken.
 The table is currently not editable, although this may be changed
 in the future.  The table is an instance of Java's JTable class,
 which is exposed as a public member.  The rich interface
 of the JTable class can be used to customize the display
 in various ways.

 @author Bart Kienhuis and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class MatrixPane extends JScrollPane {
    /** Construct an empty matrix pane.
     */
    public MatrixPane() {
        super();

        // Create a table.
        table = new JTable();

        // No table header.
        table.setTableHeader(null);

        // Do not adjust column widths automatically, use a scrollbar.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Add the table to the scroll pane.
        setViewportView(table);

        // Inherit the background color from the container.
        table.setBackground(null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the display. */
    public void clear() {
        table.setModel(_emptyTableModel);
    }

    /** Set the matrix to display in the table.
     *  @param matrix The matrix to display in the table.
     */
    public void display(MatrixToken matrix) {
        table.setModel(new MatrixAsTable(matrix));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The table representing the matrix. */
    public JTable table;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Emtpy string. */
    private static Token _emptyStringToken = new StringToken("");

    /** Empty table model. */
    private static EmptyTableModel _emptyTableModel = new EmptyTableModel();

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** This class provides an implementation of the
     *  TableModel interface for viewing matrix tokens.
     *  Each element of the matrix is represented as an instance
     *  of Token, so all matrix types are supported.
     */
    private class MatrixAsTable extends AbstractTableModel {
        /** Construct a table for the specified matrix.
         *  @param matrix The matrix.
         */
        MatrixAsTable(MatrixToken matrix) {
            _matrix = matrix;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Get the column count of the Matrix.
         *  @return the column count.
         */
        public int getColumnCount() {
            return _matrix.getColumnCount();
        }

        /** Get the name of the specified column, which is the column
         *  index as a string.
         *  @return The column index as a string.
         */
        public String getColumnName(int columnIndex) {
            return Integer.toString(columnIndex);
        }

        /** Get the row count of the Matrix.
         *  @return the row count.
         */
        public int getRowCount() {
            return _matrix.getRowCount();
        }

        /** Get the specified entry from the matrix as a Token.
         *  @param row The row number.
         *  @param column The column number.
         *  @return An instance of Token representing the matrix value
         *   at the specified row and columun.
         */
        public Object getValueAt(int row, int column) {
            // There is a bug in JTable, where it happily tries to access
            // rows and columns that are outside of range.
            if ((row >= _matrix.getRowCount())
                    || (column >= _matrix.getColumnCount())) {
                return (_emptyStringToken);
            }

            return (Object) (_matrix.getElementAsToken(row, column)).toString();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private members                   ////

        /** The Matrix for which a Table Model is created. */
        private MatrixToken _matrix = null;
    }

    /** This class provides an implementation of the
     *  TableModel interface representing an empty matrix.
     *  This is used to clear the display.
     */
    private static class EmptyTableModel extends AbstractTableModel {
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Get the column count of the Matrix.
         *  @return Zero.
         */
        public int getColumnCount() {
            return 0;
        }

        /** Get the row count of the Matrix.
         *  @return Zero.
         */
        public int getRowCount() {
            return 0;
        }

        /** Get the specified entry from the matrix as a Token.
         *  @param row The row number.
         *  @param column The column number.
         *  @return An instance of Token representing the matrix value
         *   at the specified row and columun.
         */
        public Object getValueAt(int row, int column) {
            return (_emptyStringToken);
        }
    }
}
