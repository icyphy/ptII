/* A top-level dialog window for editing Unit constraints.

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
@Pt.ProposedRating Yellow (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.net.URL;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import ptolemy.data.unit.UnitAttribute;
import ptolemy.data.unit.UnitEquation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// UnitConstraintsDialog
/**
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitConstraintsDialog
    extends PtolemyDialog
    implements ChangeListener {

    /**
     * Construct a dialog that presents Unit constraints as a table. Each row
     * of the table corresponds to one constraint. The user modifies the table
     * to specify changes in the equations.
     * <p>
     * This dialog is is not modal. In particular, changes can be undone by
     * clicking Edit->Undo, and the help screen can be manipulated while this
     * dialog exists. The dialog is placed relative to the owner.
     *
     * @param owner
     *            The object that, per the user, appears to be generating the
     *            dialog.
     * @param target
     *            The object whose ports are being configured.
     * @param configuration
     *            The configuration to use to open the help screen (or null if
     *            help is not supported).
     */
    public UnitConstraintsDialog(
        DialogTableau tableau,
        Frame owner,
        Entity target,
        Configuration configuration) {
        super(
            "Configure units for " + target.getName(),
            tableau,
            owner,
            target,
            configuration);
        Vector _constraintExpression = new Vector();
        UnitAttribute _unitConstraints =
            (UnitAttribute) target.getAttribute("_unitConstraints");
        if (_unitConstraints != null) {
            Vector _constraintExpressions = _unitConstraints.getUnitEquations();
            for (int i = 0; i < _constraintExpressions.size(); i++) {
                UnitEquation uEq =
                    (UnitEquation) (_constraintExpressions.get(i));
                String commonDesc = uEq.commonExpression();
                System.out.println("_constraint " + commonDesc);
                _constraintExpression.add(commonDesc);
            }
        }
        _unitsTable = new JTable();
        _unitsTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        setScrollableContents(_unitsTable);
        ListSelectionModel rowSM = _unitsTable.getSelectionModel();
        rowSM.addListSelectionListener(_rowSelectionListener);
        // Create the TableModel and set certain cell editors and renderers
        _setupTableModel(_constraintExpression);
        _unitsTable.setTableHeader(null);
        pack();
        setVisible(true);
    }
    // The table model for the table.
    class UnitsTableModel extends AbstractTableModel {

        Vector _unitsDataTable;

        public UnitsTableModel(Vector unitsExpression) {
            _unitsDataTable = new Vector(unitsExpression);
        }

        public int getRowCount() {
            return _unitsDataTable.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int i) {
            return "Constraint";
        }

        public Object getValueAt(int row, int col) {
            return _unitsDataTable.elementAt(row);
        }

        public void addNewConstraint() {
            _unitsDataTable.add("");
            // Now tell the GUI so that it can update itself.
            fireTableRowsInserted(getRowCount(), getRowCount());
        }

        public boolean isCellEditable(int arg0, int arg1) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            _unitsDataTable.set(row, value);
            _enableApplyButton(true);
            _setDirty(true);
        }

        public String toString() {
            StringBuffer retv = new StringBuffer("");
            if (_unitsDataTable.size() > 0) {
                retv.append((String) (_unitsDataTable.elementAt(0)));
                for (int i = 1; i < _unitsDataTable.size(); i++) {
                    retv.append(";" + (String) (_unitsDataTable.elementAt(i)));
                }
            }
            return retv.toString();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see ptolemy.kernel.util.ChangeListener#changeExecuted(ChangeRequest)
     */
    public void changeExecuted(ChangeRequest change) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see ptolemy.kernel.util.ChangeListener#changeFailed(ChangeRequest,
     *      Exception)
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see ptolemy.actor.gui.PtolemyDialog#_apply()
     */
    protected void _apply() {
        String expr = _unitsTableModel.toString();
        String moml =
            "<property name=\"_unitConstraints\" "
                + "class = \"ptolemy.data.unit.UnitAttribute\" "
                + "value = \""
                + expr
                + "\"/>";

        MoMLChangeRequest request =
            new MoMLChangeRequest(this, getTarget(), moml, null);
        request.setUndoable(true);
        // NOTE: There is no need to listen for completion
        // or errors in this change request, since, in theory,
        // it will just work. Will someone report the error
        // if one occurs? I hope so...
        getTarget().requestChange(request);
    }

    /*
     * (non-Javadoc)
     *
     * @see ptolemy.actor.gui.PtolemyDialog#_createExtendedButtons()
     */
    protected void _createExtendedButtons(JPanel _buttons) {
        _commitButton = new JButton("Commit");
        _buttons.add(_commitButton);
        _applyButton = new JButton("Apply");
        _buttons.add(_applyButton);
        _addButton = new JButton("Add");
        _buttons.add(_addButton);
        _removeButton = new JButton("Remove           ");
        _removeButton.setEnabled(false);
        _buttons.add(_removeButton);
    }

    protected void _enableApplyButton(boolean e) {
        _applyButton.setEnabled(e);
    }

    protected URL _getHelpURL() {
        URL helpURL =
            getClass().getClassLoader().getResource(
                "ptolemy/actor/gui/doc/unitConstraintsDialog.htm");
        return helpURL;
    }

    // The button semantics are
    // Add - Add a new port.
    protected void _processButtonPress(String button) {
        if (button.equals("Add")) {
            _unitsTableModel.addNewConstraint();
        } else if (button.equals("Apply")) {
            _apply();
        } else {
            super._processButtonPress(button);
        }
    }

    private void _setupTableModel(Vector constraintExpressions) {
        _unitsTableModel = new UnitsTableModel(constraintExpressions);
        _unitsTable.setModel(_unitsTableModel);
        TableColumn _constraintColumn =
            ((TableColumn) (_unitsTable.getColumnModel().getColumn(0)));
        JTextField textField = new JTextField();
        final DefaultCellEditor constraintEditor =
            new DefaultCellEditor(textField);
        constraintEditor.setClickCountToStart(1);
        _constraintColumn.setCellEditor(constraintEditor);
        //        textField.addFocusListener(new FocusAdapter() {
        //            public void focusLost(FocusEvent fe) {
        //                constraintEditor.stopCellEditing();
        //            }
        //        });
        _enableApplyButton(false);
    }
    ///////////////////////////////////////////////////////////////////
    //// private variables ////
    private JButton _addButton, _applyButton, _commitButton, _removeButton;
    private ListSelectionListener _rowSelectionListener =
        new ListSelectionListener() {

            ///////////////////////////////////////////////////////////////////
        //// inner class ////
    public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            //Ignore extra messages.
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
                _removeButton.setText("Remove");
                _removeButton.setEnabled(false);
                _selectedRow = -1;
            } else {
                _selectedRow = lsm.getMinSelectionIndex();
                _removeButton.setEnabled(true);
            }
        }
    };
    private int _selectedRow = -1;
    private JTable _unitsTable;
    private UnitsTableModel _unitsTableModel;
}
