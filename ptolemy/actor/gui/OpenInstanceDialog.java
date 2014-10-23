/* A top-level dialog window for displaying the state of an instance.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// OpenInstanceDialog

/**
 This class is a non-modal dialog that displays the current state of
 parameters of an instance.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class OpenInstanceDialog extends PtolemyDialog implements ChangeListener {

    /** Construct a dialog that presents the parameters as a table. Each row of the
     *  table corresponds to one parameter. The table shows the expression and its
     *  current evaluated value.
     *  @param tableau The DialogTableau.
     *  @param owner The object that, per the user, appears to be generating the
     *   dialog.
     *  @param target The object whose ports are being configured.
     *  @param configuration The configuration to use to open the help screen
     *   (or null if help is not supported).
     */
    public OpenInstanceDialog(DialogTableau tableau, Frame owner,
            Entity target, Configuration configuration) {
        super("Instance: " + target.getFullName(), tableau, owner, target,
                configuration);

        // Listen for changes that may need to be reflected in the table.
        // FIXME: Should we just selectively listen to each parameter?
        getTarget().addChangeListener(this);

        _parameterTable = new JTable();

        // Initialize which columns will be visible for this target.
        _initColumnNames();

        // Create the TableModel and set certain cell editors and renderers
        _setupTableModel();

        // The height of the window depends on the number of columns.
        // FIXME: How to know the height of each row? 16 is a guess.
        int height = _parameterTableModel.getRowCount() * 16;
        if (height < 64) {
            height = 64;
        } else if (height > 256) {
            height = 256;
        }
        _parameterTable.setPreferredScrollableViewportSize(new Dimension(600,
                height));

        // Initialize the displayed column widths.
        _initColumnSizes();

        // Make the contents of the table scrollable
        setScrollableContents(_parameterTable);

        _parameterTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                int code = event.getKeyCode();
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
                    _cancel();
                }
            }
        });

        pack();
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the table.
     *  This is called when a change has been successfully executed.
     *  @param change The change that has been executed.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
        // The ports of the _target may have changed.
        _setupTableModel();

        // Given a new renderer, need to do this as well.
        _initColumnSizes();
    }

    /** Do nothing.
     *  This is called when a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Do nothing. This dialog doesn't need additional buttons.
     */
    @Override
    protected void _createExtendedButtons(JPanel _buttons) {
    }

    /** Return a URL that points to the help page, which for this
     *  dialog is the expressions documentation.
     *  @return A URL that points to the help page
     */
    @Override
    protected URL _getHelpURL() {
        URL doc = getClass().getClassLoader().getResource(
                "doc/openInstanceHelp.htm");
        return doc;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize which columns will be visible for this target.
     */
    private void _initColumnNames() {
        String[] temp = { ColumnNames.COL_NAME, ColumnNames.COL_EXPRESSION,
                ColumnNames.COL_VALUE };
        // Store the column names as an ArrayList.
        List columnList = Arrays.asList(temp);
        _columnNames = new ArrayList(columnList);
    }

    /** Initialize the displayed column widths.
     */
    private void _initColumnSizes() {
        TableColumn column = null;
        column = _parameterTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(150);
        // column.setMaxWidth(300);
        column = _parameterTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(300);
        column = _parameterTable.getColumnModel().getColumn(2);
        column.setPreferredWidth(300);
    }

    /** Creates and sets the TableModel. Also arranges for some columns
     *  to have their particular renderers and/or editors. This method
     *  will be invoked when the dialog is created, and every time a
     *  change request from above causes the table to change.
     */
    private void _setupTableModel() {
        _parameterTableModel = new ParametersTableModel(getTarget());
        _parameterTable.setModel(_parameterTableModel);
        _parameterTable.setDefaultRenderer(String.class,
                new StringCellRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of names of columns that will be used for this target. */
    private ArrayList _columnNames;

    /** The background color for expert parameters. */
    private static Color _EXPERT_COLOR = new Color(1.0f, 0.9f, 0.9f);

    /** The background color for invisible parameters. */
    private static Color _INVISIBLE_COLOR = new Color(0.9f, 0.9f, 0.9f);

    /** The target parameter list. */
    private List _parameters;

    /** Table of parameters. */
    JTable _parameterTable;

    /** The table model. */
    ParametersTableModel _parameterTableModel = null;

    /** Strings that are available for the column names. */
    private static class ColumnNames {
        public final static String COL_NAME = "Name";

        public final static String COL_EXPRESSION = "Expression";

        public final static String COL_VALUE = "Value";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The table model.
     */
    private class ParametersTableModel extends AbstractTableModel {

        /** Create a table model for the specified target.
         *  @param target The target.
         */
        public ParametersTableModel(NamedObj target) {
            _parameters = target.attributeList(Settable.class);
        }

        /** Return the number of columns.
         *  @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount() {
            return _columnNames.size();
        }

        /** Return the number of rows.
         *  @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount() {
            return _parameters.size();
        }

        /** Return the column header name.
         *  @see javax.swing.table.TableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int col) {
            return (String) _columnNames.get(col);
        }

        /** Return the value at a particular row and column.
         *  @param row The row number.
         *  @param col The column number.
         *  @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int col) {
            Settable parameter = (Settable) _parameters.get(row);
            if (ColumnNames.COL_NAME.equals(getColumnName(col))) {
                return parameter.getDisplayName();
            } else if (ColumnNames.COL_EXPRESSION.equals(getColumnName(col))) {
                return parameter.getExpression();
            } else {
                return parameter.getValueAsString();
            }
        }

        /** Set the value at a particular row and column, which does nothing
         *  because this table is not editable.
         *  @param value The new value.
         *  @param row The row number.
         *  @param col The column number.
         *  @see javax.swing.table.TableModel#setValueAt(Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
        }

        /** Get the Java Class associated with a column param column.
         *  @param col The column number.
         *  @return String.class.
         *  @see javax.swing.table.TableModel#getColumnClass(int)
         */
        @Override
        public Class getColumnClass(int col) {
            return String.class;
        }

        /** Return false. This table is not editable.
         *  @param row The row number.
         *  @param col The column number.
         *  @return False.
         *  @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /** Default renderer for table cells.
     */
    private class StringCellRenderer extends JLabel implements
    TableCellRenderer {
        public StringCellRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            setOpaque(true);
            setText((String) value);
            // The color depends on the properties of the parameter.
            Settable parameter = (Settable) _parameters.get(row);
            if (parameter.getVisibility() == Settable.EXPERT) {
                setBackground(_EXPERT_COLOR);
            } else if (parameter.getVisibility() == Settable.NONE) {
                setBackground(_INVISIBLE_COLOR);
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }
}
