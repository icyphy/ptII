/* A data model for cells in a port/parameter/property form

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (Ed.Willink@rrl.co.uk)
@AcceptedRating Red (Ed.Willink@rrl.co.uk)
*/

package ptolemy.vergil.form;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/** The FormTableModel maintains the information displayed in the form.
 *  A pair of vectors of rows are maintained. One maintaining the prevailing MML state,
 *  and another representing a new dynamic yet to be applied state.
 *
@author Edward D. Willink
@version $Id$
 */
class FormTableModel extends AbstractTableModel
{
    /** Construct an empty model */
    public FormTableModel() { super(); }
    /** Add a blank MoML row to the model */
    public FormRow addMomlRow() { FormRow theRow = new FormRow(_column_model); _moml_rows.add(theRow); return theRow; }
    /** Add another MoML row to the model */
    public FormRow addMomlRow(Object[] aRow) { FormRow theRow = new FormRow(_column_model, aRow); _moml_rows.add(theRow); return theRow; }
    /** Add another edited row to the model. */
    public void addEditingRow()
    {
        int newRow = _editing_rows.size();
        _editing_rows.add(new FormRow(_column_model));
        fireTableRowsInserted(newRow, newRow);
    }
    /** Erase all rows preparatory to defining replacements. */
    public void eraseMomlRows() { _moml_rows.clear(); }    
    /** Return the class of columnIndex. */
    public Class getColumnClass(int columnIndex) { return _column_model.getFormColumn(columnIndex).getColumnClass(); }
    /** Return the number of columns in the columnModel. */
    public int getColumnCount() { return _column_model.getColumnCount(); }
    /** Return the name of columnIndex. */
    public String getColumnName(int columnIndex)
        { return _column_model.getFormColumn(columnIndex).getColumnName().toString(); }
    /** Return the number of (editing) rows. */
    public int getRowCount() { return _editing_rows.size(); }
    /** Return the (editing) value at rowIndex, columnIndex. */
    public Object getValueAt(int rowIndex, int columnIndex)
        { return getEditingRow(rowIndex).getCell(getColumnName(columnIndex)); }
    /** Return the editabilitity of rowIndex, columnIndex. */
    public boolean isCellEditable(int rowIndex, int columnIndex) { return true; }
    /** Return the MoML to convert the _moml_rows into the _editing_rows. */
    public Vector momlChanges()
    {
        boolean reorderNecessary = false;
        Vector momlTexts = new Vector();
        FormRow newRow = null;
        FormRow oldRow = null;
        for (int i = 0; i < _moml_rows.size(); i++)         // First get rid of what cannot change. */
        {
            oldRow = getMomlRow(i);
            newRow = oldRow.getControlValue();
            if (!_editing_rows.contains(newRow) || oldRow.isDeleted(newRow))
                momlTexts.add(oldRow.momlDelete());
        }
        for (int i = 0; i < _editing_rows.size(); i++)      // Then add/update what can. */
        {
            newRow = getEditingRow(i);
            oldRow = newRow.getControlValue();
            if (newRow.isChanged(oldRow))
                momlTexts.add(newRow.momlChanges((oldRow == null) || oldRow.isDeleted(newRow) ? null : oldRow));
        }
        return momlTexts;
    }
    /** Remove the (editing) rowIndex. */
    public void removeEditingRow(int rowIndex)
    {
        _editing_rows.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
    /** Restore the editing context to that defined by the MoML rows (lose edits). */
    public void restoreMomlRows()
    {
        _editing_rows.clear();
        for (int i = 0; i < _moml_rows.size(); i++)
        {
            FormRow oldRow = getMomlRow(i);
            FormRow newRow = (FormRow)oldRow.clone();
            newRow.setControlValue(oldRow);
            oldRow.setControlValue(newRow);
            _editing_rows.add(newRow);
            FormRow newOldRow = newRow.getControlValue();
            FormRow oldNewRow = oldRow.getControlValue();
            if (newOldRow != oldRow)
                System.out.println("Bad set newRow control value.");
            if (oldNewRow != newRow)
                System.out.println("Bad set oldRow control value.");
        }
        fireTableDataChanged();
    } 
    /** Apply the editing context to the MoML rows (commit edits). */
    public void saveEditingRows()
    {
        _moml_rows.clear();
        _moml_rows.addAll(_editing_rows);
        restoreMomlRows();
    } 
    /** Specify the (MoML) column model. */
    public void setColumnModel(FormColumnModel columnModel) { _column_model = columnModel; }
    /** Change the (editing) value at rowIndex, columnIndex to aValue.toString(). */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
//        System.out.println("Put " + aValue.toString() + " at " + rowIndex + "," + columnIndex);
        getEditingRow(rowIndex).setCellValue(getColumnName(columnIndex), aValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
// Protected methods
    /** Return the editing row at rowIndex. */
    protected FormRow getEditingRow(int rowIndex) { return (FormRow)_editing_rows.get(rowIndex); }
    /** Return the MoML row at rowIndex. */
    protected FormRow getMomlRow(int rowIndex) { return (FormRow)_moml_rows.get(rowIndex); }
// Private methods and data
    /** The rows corresponding to the prevailing MoML state. */
    private Vector _moml_rows = new Vector();
    /** The rows corresponding to the displayed state. */
    private Vector _editing_rows = new Vector();
    private FormColumnModel _column_model;
}
