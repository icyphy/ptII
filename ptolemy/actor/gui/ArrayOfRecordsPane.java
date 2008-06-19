/* A graphical component displaying an array of records.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
import java.awt.GridLayout;
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

//////////////////////////////////////////////////////////////////////////
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
 @version $Id: ArrayOfRecordsPane.java 47561 2007-12-16 07:29:50Z cxh $
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ArrayOfRecordsPane extends JPanel {
    /** Construct an empty table pane.
     */
    public ArrayOfRecordsPane() {
        super();
        add(new SimpleTable());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the display. */
    public void clear() {
        table.setModel(_emptyTableModel);
    }

    /** Set the array to display in the table.
     *  @param array The array of records to display in the table.
     */
    public void display(ArrayToken array) {
        table.setModel(new ArrayAsTable(array));
        table.setTableHeader(new JTableHeader(table.getColumnModel()));
        _initColumnSizes(table);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The table representing the matrix. */
    public JTable table;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** This method picks good column sizes. As usual with Swing,
     *  what it does is completely ignored...
     *  It is adapted from the Java Tutorials from Sun Microsystems.
     */
    private void _initColumnSizes(JTable table) {
        ArrayAsTable model = (ArrayAsTable)table.getModel();
        TableColumn column = null;
        Component component = null;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < model.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            component = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            int width = component.getPreferredSize().width;
            for (int j = 0; j < model.getRowCount(); j++) {
                component = table.getDefaultRenderer(
                        model.getColumnClass(i)).
                        getTableCellRendererComponent(
                                table, model.getValueAt(j, i),
                                false, false, 0, i);
                int cellWidth = component.getPreferredSize().width;
                if (cellWidth > width) {
                    width = cellWidth;
                }
            }
            column.setPreferredWidth(width);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Empty string. */
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
    private static class ArrayAsTable extends AbstractTableModel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        
        /** Construct a table for the specified array.
         *  @param matrix The matrix.
         */
        ArrayAsTable(ArrayToken array) {
            _array = array;
            
            // Figure out what the column names are.
            for(int i = 0; i < _array.length(); i++) {
                RecordToken record = (RecordToken)_array.getElement(i);
                Iterator labels = record.labelSet().iterator();
                while (labels.hasNext()) {
                    String column = (String)labels.next();
                    if (!_columns.contains(column)) {
                        _columns.add(column);
                    }
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

        /** Get the specified entry from the matrix as a Token.
         *  @param row The row number.
         *  @param column The column number.
         *  @return An instance of Token representing the matrix value
         *   at the specified row and column.
         */
        public Object getValueAt(int row, int column) {
            // There is a bug in JTable, where it happily tries to access
            // rows and columns that are outside of range.
            if ((row >= _array.length())
                    || (column >= _columns.size())) {
                return (_emptyStringToken);
            }
            Token element = ((RecordToken)_array.getElement(row)).get(_columns.get(column));
            if (element == null) {
                return (_emptyStringToken);
            }
            // Strip off the extra quotation marks if necessary.
            if(element instanceof StringToken) {
                return ((StringToken)element).stringValue();
            }
            return element.toString();
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
    
    /** Table panel. */
    private class SimpleTable extends JPanel {
        public SimpleTable() {
            super(new GridLayout(1,0));
            table = new JTable();
            // Adjust column widths automatically.
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            // FIXME: Don't hardwire the size here ?
            table.setPreferredScrollableViewportSize(new Dimension(500, 300));
            // This is Java 1.6 specific:
            //table.setFillsViewportHeight(true);

            //Create the scroll pane and add the table to it.
            JScrollPane scrollPane = new JScrollPane(table);

            //Add the scroll pane to this panel.
            add(scrollPane);
        }
    }
}
