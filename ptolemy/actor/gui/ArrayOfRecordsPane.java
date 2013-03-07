/* A graphical component displaying an array of records.

 Copyright (c) 1997-2013 The Regents of the University of California.
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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// ArrayOfRecordsPane

/**
 A graphical component that displays the values in an array of records.
 The data to display is supplied in the form of an ArrayToken.
 The table is currently not editable, although this may be changed
 in the future.  The table is an instance of Java's JTable class,
 which is exposed as a public member.  The rich interface
 of the JTable class can be used to customize the display
 in various ways.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ArrayOfRecordsPane extends JPanel {
    /** Construct an empty table pane.
     */
    public ArrayOfRecordsPane() {
        super();
        table = new SimpleTable();
        // FIXME: As usual with Swing, the scrollpane doesn't appear.
        // But if we don't do this, then the table headers don't appear!
        // Go figure...
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the display. */
    public void clear() {
        table.setModel(_emptyTableModel);
    }

    /** Set the array to display in the table.
     *  This method results in all fields of the records being displayed.
     *  @param array The array of records to display in the table.
     */
    public void display(ArrayToken array) {
        display(array, null);
    }

    /** Set the array to display in the table.
     *  @param array The array of records to display in the table.
     *  @param columns The array of strings giving the column names
     *   to display.
     */
    public void display(ArrayToken array, ArrayToken columns) {
        table.setModel(new ArrayAsTable(array, columns));
        table.setTableHeader(new JTableHeader(table.getColumnModel()));
        _initColumnSizes(table);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The table representing the matrix. */
    public JTable table;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** This method picks good column sizes and sets the preferred
     *  size for the table.
     *  It is adapted from the Java Tutorials from Sun Microsystems.
     */
    private void _initColumnSizes(JTable table) {
        ArrayAsTable model = (ArrayAsTable) table.getModel();
        TableColumn column = null;
        Component component = null;
        TableCellRenderer headerRenderer = table.getTableHeader()
                .getDefaultRenderer();

        int tableWidth = 0;
        int tableHeight = 0;
        for (int i = 0; i < model.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            component = headerRenderer.getTableCellRendererComponent(null,
                    column.getHeaderValue(), false, false, 0, 0);
            int width = component.getPreferredSize().width;
            int columnHeight = 0;
            for (int j = 0; j < model.getRowCount(); j++) {
                component = table.getDefaultRenderer(model.getColumnClass(i))
                        .getTableCellRendererComponent(table,
                                model.getValueAt(j, i), false, false, 0, i);
                int cellWidth = component.getPreferredSize().width;
                if (cellWidth > width) {
                    width = cellWidth;
                }
                columnHeight += component.getPreferredSize().height;
            }
            column.setPreferredWidth(width);
            tableWidth += width;

            if (columnHeight > tableHeight) {
                tableHeight = columnHeight;
            }
        }
        Dimension tableSize = new Dimension(tableWidth, tableHeight);
        table.setPreferredSize(tableSize);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Empty table model. */
    private static EmptyTableModel _emptyTableModel = new EmptyTableModel();

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** This class provides an implementation of the
     *  TableModel interface for viewing an array of records.
     */
    public static class ArrayAsTable extends AbstractTableModel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct a table for the specified array to display
         *  all fields in the records contained by the array.
         *  @param array An array of record tokens to display.
         */
        ArrayAsTable(ArrayToken array) {
            this(array, null);
        }

        /** Construct a table for the specified array to display
         *  the fields given by <i>columns</i> of records in the specified
         *  <i>array</i>.
         *  @param array An array of record tokens to display.
         *  @param columns An array of string tokens giving the names
         *   of fields to display, or null to display all the fields.
         */
        ArrayAsTable(ArrayToken array, ArrayToken columns) {
            _array = array;

            if (columns == null) {
                // Figure out what the column names are.
                for (int i = 0; i < _array.length(); i++) {
                    RecordToken record = (RecordToken) _array.getElement(i);
                    Iterator labels = record.labelSet().iterator();
                    while (labels.hasNext()) {
                        String column = (String) labels.next();
                        if (!_columns.contains(column)) {
                            _columns.add(column);
                        }
                    }
                }
            } else {
                for (int i = 0; i < columns.length(); i++) {
                    StringToken column = (StringToken) columns.getElement(i);
                    _columns.add(column.stringValue());
                }
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Get the column count of the Matrix.
         *  @return the column count.
         */
        public int getColumnCount() {
            return _columns.size();
        }

        /** Get the name of the specified column, which is the column
         *  index as a string.
         *  @param columnIndex The index of the column.
         *  @return The column index as a string.
         */
        public String getColumnName(int columnIndex) {
            if (columnIndex > _columns.size()) {
                return "";
            }
            return _columns.get(columnIndex);
        }

        /** Get the row count of the Matrix.
         *  @return the row count.
         */
        public int getRowCount() {
            return _array.length();
        }

        /** Get the specified entry as a String.
         *  @param row The row number.
         *  @param column The column number.
         *  @return An instance of Token representing the matrix value
         *   at the specified row and column.
         */
        public Object getValueAt(int row, int column) {
            // There is a bug in JTable, where it happily tries to access
            // rows and columns that are outside of range.
            if (row >= _array.length() || column >= _columns.size()) {
                return "";
            }
            Token element = ((RecordToken) _array.getElement(row)).get(_columns
                    .get(column));
            if (element == null) {
                return "";
            }
            // Strip off the extra quotation marks if necessary.
            if (element instanceof StringToken) {
                return ((StringToken) element).stringValue();
            }
            return element.toString();
        }

        /** Remove the specified row from the table.
         *  If the row is out of range, do nothing.
         *  @param row The row to remove, starting with index 0.
         */
        public void removeRow(int row) {
            if (row < _array.length()) {
                // Since tokens are immutable, we have to create a whole new token.
                Token[] newArray = new Token[_array.length() - 1];
                for (int i = 0; i < row; i++) {
                    newArray[i] = _array.getElement(i);
                }
                for (int i = row + 1; i < _array.length(); i++) {
                    newArray[i - 1] = _array.getElement(i);
                }
                try {
                    ArrayToken newToken = new ArrayToken(newArray);
                    _array = newToken;
                    super.fireTableRowsDeleted(row, row);
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private members                   ////

        /** The array for which a Table Model is created. */
        private ArrayToken _array = null;

        /** The column names found in the array. */
        private List<String> _columns = new LinkedList<String>();
    }

    /** This class provides an implementation of the
     *  TableModel interface representing an empty table.
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

        /** Get the specified entry from the matrix as a String.
         *  @param row The row number.
         *  @param column The column number.
         *  @return An empty String.
         */
        public Object getValueAt(int row, int column) {
            return "";
        }
    }

    /** Table panel. */
    private static class SimpleTable extends JTable {
        public SimpleTable() {
            super();
            // Adjust column widths automatically.
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            // FIXME: Don't hardwire the size here ?
            // Sadly, as usual with swing, the preferred size of the table has
            // no effect. Also, the scrollbar does not appear... This is really lame...
            setPreferredScrollableViewportSize(new Dimension(800, 200));
            // This is Java 1.6 specific:
            // table.setFillsViewportHeight(true);
        }
    }
}
