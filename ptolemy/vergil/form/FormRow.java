/* A row of cells in a port/parameter/property form

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

import java.util.Hashtable;
import java.util.Enumeration;

/** A FormRow maintains the cells that could be displayed in a row of the form.
 * The cells are maintained indexed by their column name, so the disolayed cells
 * can be an arbitrarily ordered subset of the available cells.
 *
@author Edward D. Willink
@version $Id$
 */
class FormRow
{
    /* Construct a new blank form row populated by invoking makeCellValue on each element of columnModel. */
    public FormRow(FormColumnModel columnModel)
    {
        int columnCount = columnModel.getColumnCount();
        _cells = new Hashtable(2 * columnCount);
        for (int i = 0; i < columnCount; ++i)
            add(columnModel.getFormColumn(i).makeCellValue());
    }
    /* Construct a new blank form row populated by invoking aColumn.makeCellValue(aCell) for each aCell in aRow,
      * where aColumn and aRow are mutually indexed within the domain of aRow. */
    public FormRow(FormColumnModel columnModel, Object[] aRow)
    {
        int columnCount = columnModel.getColumnCount();
        if (aRow.length != columnCount)
            System.out.println("Expected " + columnCount + " entries in parameter row.");
        _cells = new Hashtable(2 * columnCount);
        for (int i = 0; i < columnCount; ++i)
            add(columnModel.getFormColumn(i).makeCellValue(aRow[i]));
    }
    /** Return a clone of this row, and of each cell in the row. */
    public Object clone()
    {
        Hashtable clonedCells = new Hashtable(2 * _cells.size());
        Enumeration i = _cells.keys();
        while (i.hasMoreElements())
        {
            String hashKey = (String)i.nextElement();
            FormCell aCell = getCell(hashKey);
            clonedCells.put(hashKey, aCell.clone());
        }
        return new FormRow(clonedCells);
    }
    /** Return the cell named aKey. */
    public FormCell getCell(String aKey) { return (FormCell)_cells.get(aKey); }
    /** Return the value of the control cell. */
    public FormRow getControlValue() { return getControl().getControlValue(); }
    /** Return true if oldRow must be changed to become this row (or vice-versa). */
    public boolean isChanged(FormRow oldRow)
    {
        if (oldRow == null)
        {
//            System.out.println(this + " changed because new row.");
            return true;
        }
        Enumeration i = _cells.keys();
        while (i.hasMoreElements())
        {
            String hashKey = (String)i.nextElement();
            FormCell aCell = getCell(hashKey);
            if (aCell.isChanged(oldRow))
            {
//                FormCell oldCell = oldRow.getCell(hashKey);
//                if (oldCell == null)
//                    System.out.println(aCell + " changed because new cell.");
//                else
//                    System.out.println(aCell + " changed from \"" + oldCell.toString() + "\" to.\"" + aCell.toString() + "\"");
                return true;
            }
        }
        return false;
    }
    /** Return true if this row must be deleted to make way for newRow. */
    public boolean isDeleted(FormRow newRow)
    {
        if (newRow == null)
        {
//            System.out.println(this + " deleted because dead row.");
            return true;
        }
        if (newRow.getCell("key").getClass() != getCell("key").getClass())
        {
//            System.out.println(this + " deleted because changed key.");
            return true;
        }
        return false;
//        return (newRow == null) || newRow.getCell("key").isChanged(this);
    }
    /** Return the MoML to change oldRow into this row. */
    public String momlChanges(FormRow oldRow)
    {
        KeyCell keyCell = (KeyCell)getCell("key");
        ControlKey keyWord = keyCell.getCellKey();
        FormCell nameCell = (oldRow != null ? oldRow : this).getCell("name");
        StringBuffer momlText = new StringBuffer();
        momlText.append("\n <" + keyWord.momlKey() + " name=\"" + nameCell);
        FormCell classCell = (oldRow != null ? oldRow : this).getCell("class");
        if (classCell != null)
            momlText.append("\" class=\"" + classCell);
        momlText.append("\">");
        Enumeration i = _cells.keys();
        while (i.hasMoreElements())
        {
            String hashKey = (String)i.nextElement();
            FormCell aCell = getCell(hashKey);
            momlText.append(aCell.momlChanges(oldRow));
        }
        momlText.append("\n </" + keyWord.momlKey() + ">");
        return momlText.toString();
    }
    /** Return the MoML to delete this row. */
    public String momlDelete()
    {
        KeyCell keyCell = (KeyCell)getCell("key");
        ControlKey keyWord = keyCell.getCellKey();
        FormCell nameCell = getCell("name");
        return "\n <" + keyWord.momlDeleteKey() + " name=\"" + nameCell + "\"/>";
    }
    /** Define aValue for the aKey cell. */
    public void setCellValue(String aKey, Object aValue) { getCell(aKey).setCellValue(aValue); }
    /** Define avalue for the control cell. */
    public void setControlValue(FormRow aValue) { getControl().setCellValue(aValue); }
// Private methods and data
    /** Construct a FormRow using the Hashtable of _cells. */
    private FormRow(Hashtable someCells) { _cells = new Hashtable(someCells); }
    /** Add aCell to the table of available cells */
    private void add(FormCell aCell) { _cells.put(aCell.getColumnName(), aCell); }
    /** Return the control cell. */
    private ControlCell getControl() { return (ControlCell)_cells.get(ControlCell.getStaticColumnName()); }
    /* The hashtable of cells available for display. */
    private Hashtable _cells;
}      
