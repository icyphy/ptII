/* A cell in a port/parameter/property form.

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

import java.awt.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

/** FormCell defines the behaviour of cells in a column of the form and their translation to MoML.
 * Prototype instances are used to define the column model, and the cells avialable for display in each row.
 * Actiual instnaces define each cell in the form.
 * Derived classes support the disntct column plocies.
 * ControlCell supports the left hand column.
 * KeyCell the main keyword and NameCell the qualifying name. 
 * StringCell and BooleanCell support generic properties.
 * The value of a ControlCell is overloaded to support resolution of the moml and dynamic states.
 *
@author Edward D. Willink
@version $Id$
 */ 
abstract class FormCell extends TableColumn implements Cloneable
{
    /** Construct a form cell with editing enabled. */
    public FormCell(Object cellValue)
	{
		_is_enabled = true;
		_is_settable = true;
		_cell_value = cellValue;
//		setMinWidth(10);
	}
    /** Clone this cell. */
    public Object clone()
    {
        try
        {
            FormCell clonedCell = (FormCell)super.clone();
            clonedCell.setEnabled(_is_enabled);
            return clonedCell;
        }
        catch (CloneNotSupportedException e) { throw new InternalError(); }
    }
    /** Return the value of this cell. */
    public Object getCellValue() { return _cell_value; }
    /** Return the class of the values in this column. */
    public Class getColumnClass() { return _cell_value.getClass(); }
    /** Return the name of the column containing this cell. */
    public abstract String getColumnName();
    /** Configure the cell for use as modelIndex. */
    public void initCell(int modelIndex)
    {
        setHeaderValue(getColumnName());
        setModelIndex(modelIndex);
    }
    /** Return true if there is any difference between this cell and its counterpart in oldRow. */
    public boolean isChanged(FormRow oldRow)
    {
        if (oldRow == null)
            return true;
        FormCell oldCell = oldRow.getCell(getColumnName());
        if (oldCell == null)
            return true;
        if (_cell_value == oldCell._cell_value)     // Faster, and catches null == null.
            return false;
        if (_cell_value == null)
            return true;
        if (oldCell._cell_value == null)
            return true;
        return !_cell_value.equals(oldCell._cell_value);
    }
    /** Return true if this cell is enabled. */
    public boolean isEnabled() { return _is_enabled; }
    /** Return true if this cell is settable within MoML. */
    public boolean isSettable() { return _is_settable; }
    /** Create another cell in the same column as this cell for use on a new row. */
    public abstract FormCell makeCellValue();
    /** Create another cell in the same column as this cell with specified initial value. */
    public abstract FormCell makeCellValue(Object cellValue);
    /** Return the MoML to describe any difference bettween this cell and its counterpart in oldRow. */
    public abstract String momlChanges(FormRow oldRow);
    /** Change the value of this cell. */
    public void setCellValue(Object cellValue) { _cell_value = cellValue; }
    /** Enable/disable (grey-out) this cell. */
    public void setEnabled(boolean isEnabled) { _is_enabled = isEnabled; }
    /** Enable emission toi MoML. */
    public void setSettable(boolean isSettable) { _is_settable = isSettable; }
    /** Return the value of this cell as a string. */
    public String toString() { return _cell_value != null ? _cell_value.toString() : "-null-"; }
// Protected methods
    /** Return a cell renderer for use by disabled cells. */
    protected static TableCellRenderer disabledCellRenderer()
    {
        if (_disabled_cell_renderer == null)
        {
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
            cellRenderer.setEnabled(true);
            cellRenderer.setBackground(Color.lightGray);
            cellRenderer.setForeground(Color.black);
            _disabled_cell_renderer = cellRenderer;
        }
        return _disabled_cell_renderer;
    }
// Private data
    /** Renderer for greyed-out cells. */
    private static TableCellRenderer _disabled_cell_renderer = null;
    /** Object defining the value of this cell. */
    private Object _cell_value;
    /** Enabled/editable (not greyed out) status of cell/column */
    private boolean _is_enabled;
    /** Settable within MoML. */
    private boolean _is_settable;
}

/** ControlCell defines the behaviour of cells in the left-hand column of the parameter form.
 * The value of a control cell is a reference to its MoML/Editing counterpart, enabling the rows of the
 * two states to be correlated. New rows can be identified by null values. Deleted rows by the omission of
 * their sibling from the alternate state. This overloaded use of the value is hidden from the display functionality
 * by ensuring that the displayed text is always a space.
Functionality is currently limited to exhibiting a narrow width.
Future functionality may support dragging to reorder rows.
*/ 
class ControlCell extends FormCell
{
    /** Construct a control cell with null sibling linkage, and narrow width. */
    public ControlCell()
    {
        super(null);
        setMaxWidth(10);
        setEnabled(false);
        setCellRenderer(cellRenderer());           
    }  
    /** Return the class of the values in this column. */
    public Class getColumnClass() { return "".getClass(); }
    /** Return the name of the column containing this cell. */
    public String getColumnName() { return _column_name; }
    /** Return the identity of the sibling row. */
    public FormRow getControlValue() { return (FormRow)getCellValue(); }
    /** Return the name of the control cell columne column. */
    public static String getStaticColumnName() { return _column_name; }
    /** Return true if this control cell is not part of the sibling of oldRow. */
    public boolean isChanged(FormRow oldRow) { return oldRow == null; }
    /** Create another cell in the same column as this cell for use on a new row. */
    public FormCell makeCellValue() { return new ControlCell(); }
    /** Create another cell in the same column as this cell with specified initial value. */
    public FormCell makeCellValue(Object cellValue) { return new ControlCell(); }
    /** Return an empty string, since control cell changes form part of the row change. */
    public String momlChanges(FormRow oldRow) { return ""; }
    /** Return a space string as the displayed text for control cells. */
    public String toString() { return " "; }
// Private methods
    /** Return a cell renderer for use by control cells. */
    protected TableCellRenderer cellRenderer()
    {
        if (_cell_renderer == null)
        {
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
            cellRenderer.setEnabled(true);
            cellRenderer.setBackground(Color.lightGray);
            cellRenderer.setForeground(Color.darkGray);
            cellRenderer.setBorder(BorderFactory.createRaisedBevelBorder());    //.bugbug No effect.
            _cell_renderer = cellRenderer;
        }
        return _cell_renderer;
    }
// Private data
    /** Renderer for control cells. */
    private static TableCellRenderer _cell_renderer = null;
    /** The name of the control column. */
    private final static String _column_name = " ";
}

/** NameCell defines the behaviour of the name column of the form.
Unique names of the form _'count' are automatically generated for added columns.
*/ 
class NameCell extends FormCell
{
    /** Construct a name cell with initial cellValue. */
    public NameCell(Object cellValue) { super(cellValue); setEnabled(true); }
    /** Return the name of the column containing this cell. */
    public String getColumnName() { return _column_name; }
    /** Make a new name cell for a new row with auto-generated unique name. */
    public FormCell makeCellValue() { return new NameCell("_" + _counter++); }
    /** Make a new name cell with cellValue as initial context. */
    public FormCell makeCellValue(Object cellValue) { return new NameCell(cellValue); }
    /** Return a rename string, if this is a rename. */
    public String momlChanges(FormRow oldRow)
    {
        if ((oldRow != null) && isChanged(oldRow))
            return "\n  <rename name=\"" + toString() + "\"/>";
        else
            return "";
    }
    /** Enable/disable (grey-out) this cell. */
    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        JTextField textField = new JTextField();
        textField.setEditable(isEnabled);        
        setCellEditor(new DefaultCellEditor(textField));
        setCellRenderer(isEnabled ? null : disabledCellRenderer());            
    }
// Private data
    /** The name of this column. */
    private final static String _column_name = "name";
    /** Counter for auto-generated names. */
    private static int _counter = 0;
}

/** StringCell defines the behaviour of string-valued cells and columns in the form.
*/ 
class StringCell extends FormCell
{
    /** Construct a cell for columnName with initial cellValue. */
    public StringCell(String columnName, Object cellValue)
    {
        super(cellValue);
        _column_name = columnName;
        setEnabled(true);
    }
    /** Return the name of the column containing this cell. */
    public String getColumnName() { return _column_name; }
    /** Create another cell in the same column as this cell for use on a new row. */
//    public FormCell makeCellValue() { return new StringCell(_column_name, ""); }
    public FormCell makeCellValue() { return (FormCell)clone(); }
    /** Create another cell in the same column as this cell with specified initial value. */
    public FormCell makeCellValue(Object cellValue) { return new StringCell(_column_name, cellValue); }
    /** Return the MoML to describe any difference bettween this cell and its counterpart in oldRow. */
    public String momlChanges(FormRow oldRow)
    {
        if (isSettable() && ((oldRow == null) || isChanged(oldRow)))
            return "\n  <property name=\"" + _column_name + "\" value=\"" + toString() + "\"/>";
        else
            return "";
    }
    /** Enable/disable (grey-out) this cell. */
    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        JTextField textField = new JTextField();
        textField.setEditable(isEnabled);        
        setCellEditor(new DefaultCellEditor(textField));
        setCellRenderer(isEnabled ? null : disabledCellRenderer());            
    }
// Private data
    /** The name of this column. */
    private final String _column_name;
}

/** BooleanCell defines the behaviour of a true/false-valued cells and columns in the form.
 *.bugbug Displays as text and only does false to true transitions.
*/ 
class BooleanCell extends FormCell
{
    /** Construct a cell for columnName with initial cellValue. */
    public BooleanCell(String columnName, Object cellValue)
    {
        super(cellValue);
        _column_name = columnName;
        setEnabled(true);
        setCellRenderer(cellRenderer()); // No renderer causes FormFrame.widestCellInColumn to crash.
    }
    /** Return the name of the column containing this cell. */
    public String getColumnName() { return _column_name; }
    /** Create another cell in the same column as this cell for use on a new row. */
    public FormCell makeCellValue() { return new BooleanCell(_column_name, Boolean.FALSE); }
    /** Create another cell in the same column as this cell with specified initial value. */
    public FormCell makeCellValue(Object cellValue) { return new BooleanCell(_column_name, cellValue); }
    /** Return the MoML to describe any difference bettween this cell and its counterpart in oldRow. */
    public String momlChanges(FormRow oldRow)
    {
        if (isChanged(oldRow))
            return "\n  <property name=\"" + _column_name + "\" value=\"" + toString() + "\"/>";
        else
            return "";
    }
    /** Enable/disable (grey-out) this cell. */
    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        JCheckBox checkBox = new JCheckBox();
//        checkBox.setEditable(isEnabled);        
        setCellEditor(new DefaultCellEditor(checkBox));
    }
// Private methods
    /** Return a cell renderer for use by boolean cells. */
    protected static TableCellRenderer cellRenderer()
    {
        if (_cell_renderer == null)
        {
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
            cellRenderer.setEnabled(true);
            _cell_renderer = cellRenderer;
        }
        return _cell_renderer;
    }
// Private data
    /** Renderer for boolean cells. */
    private static TableCellRenderer _cell_renderer = null;
    /** The name of this column. */
    private final String _column_name;
}

/** KeyCell defines the behaviour of the syntax keyword cells and column in the form.
*/ 
class KeyCell extends FormCell
{
    /** Construct a keyword cell, using the set of keywords in keyModel and initial state of cellValue. */
    public KeyCell(FormKeyModel keyModel, Object cellValue) { super(cellValue); key_model = keyModel; setEnabled(true); }
    /** Return the syntax defining keyword. */
    public ControlKey getCellKey() { return key_model.get(toString()); }
    /** Return the name of the column containing this cell. */
    public String getColumnName() { return _column_name; }
    /** Create another cell in the same column as this cell for use on a new row. */
    public FormCell makeCellValue() { return (FormCell)clone(); }
    /** Create another cell in the same column as this cell with specified initial value. */
    public FormCell makeCellValue(Object cellValue) { return new KeyCell(key_model, cellValue); }
    /** Return an empty string, since key cell changes form part of the row change. */
    public String momlChanges(FormRow oldRow)
    {
        ControlKey oldKey = oldRow != null ? ((KeyCell)oldRow.getCell(_column_name)).getCellKey() : null;
        return getCellKey().momlChanges(oldKey);
    }
    /** Enable/disable (grey-out) this cell. */
    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        JComboBox comboBox = key_model.createComboBox();
//        comboBox.setEditable(isEnabled);        
        setCellEditor(new DefaultCellEditor(comboBox));
        setCellRenderer(isEnabled ? null : disabledCellRenderer());            
    }
// Private data
    /** The name of this column. */
    private final static String _column_name = "key";
    /** The name of this column. */
    private final FormKeyModel key_model;
}
