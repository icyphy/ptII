/* A form to edit ports, parameters and properties.

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

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;

/** FormFrame provides a non-modal spreadsheet-like editor for the ports/parameters/properties of an entity.
 * The form is first constructed with a title, the the editing model is defined by setColumnModel() whose argument
 * comprises prototypes from which cells in each column are created. addRow() or addRows() may then be used to populate a
 * starting MoML state. Once pack and setVisible have been invoke the form is active and maintains a second editing state
 * that may be applied back to the MoML state under user control. Application invokes the _executeMoml() call-back so that a
 * derived class may dispatch the results to some suitable MoML interpreter.
@author Edward D. Willink
@version $Id$
*/
public class FormFrame extends JFrame
{
    /** Construct a default form editor. */
    public FormFrame() { super(); createComponents(); }
    /** Construct a form editor with aTitle. */
    public FormFrame(String aTitle) { super(aTitle); createComponents(); }
    /** Append a default row to the data model. The row is returned for manipulation by the caller. */
    public FormRow addRow() { return _table_model.addMomlRow(); }    
    /** Append dataRow to the data model.
     * The string-valued elements of dataRow must match the columnModel in number and sequence. */
    public FormRow addRow(Object[] dataRow) { return _table_model.addMomlRow(dataRow); }    
    /** Erase all rows preparatory to defining replacements. */
    public void eraseRows() { _table_model.eraseMomlRows(); }    
    /** Append dataRows to the data model.
     * The string-valued elements of each element of dataRows must match the columnModel in number and sequence. */
    public void addRows(Object[][] dataRows)
    {
        for (int i = 0; i < dataRows.length; i++)
            _table_model.addMomlRow(dataRows[i]);
//        _table_model.restoreMomlRows();
    }  
    /** Define the saved contents as the current contents before sizing. */
    public void pack()
    {
        _table_model.restoreMomlRows();
        TableColumnModel columnModel = _table.getColumnModel();
        Enumeration e = columnModel.getColumns();
        while (e.hasMoreElements())
        {
            TableColumn tableColumn = (TableColumn)e.nextElement();
            int minWidth = 10; 
            int cellWidth = widestCellInColumn(tableColumn); 
            if (cellWidth > minWidth)
                minWidth = cellWidth;
            int headerWidth = columnHeaderWidth(tableColumn);
            if (headerWidth > minWidth)
                minWidth = headerWidth;
            tableColumn.setMinWidth(minWidth);
        }
        super.pack();
    }  
    /** Specify the columnModel. */
    public void setColumnModel(FormColumnModel columnModel)
    {
        _table_model.setColumnModel(columnModel);
        _table.setModel(_table_model);
        _table.setColumnModel(columnModel);
        _table.getTableHeader().setReorderingAllowed(false);
    }
    /** Specify the starting size of the scrool table. */
    public void setPreferredScrollableViewportSize(Dimension aDimension) { _table.setPreferredScrollableViewportSize(aDimension); }
    
// Protected action methods.
    /** GUI call-back adds a blank row to the editing state of the model. */  
    protected void addEditingRow() { _table_model.addEditingRow(); }
    /** GUI call-back copies the editing state to the MoML state. */  
    protected void applyChanges()
    {
        if (_table.isEditing())
            _table.getCellEditor().stopCellEditing();
        Vector momlTexts = _table_model.momlChanges();
        if (momlTexts.size() == 0)
            ;
        else if (momlTexts.size() == 1)
            _executeMoml(momlTexts.get(0).toString());
        else
        {
            StringBuffer momlText = new StringBuffer("<group>");
            Enumeration i = momlTexts.elements();
            while (i.hasMoreElements())
                momlText.append(i.nextElement().toString());
            momlText.append("\n<group/>");
            _executeMoml(momlText.toString());
        }
//        _table_model.saveEditingRows();
    }        
    /** GUI call-back reinitialises the editing state from the MoML state (loses edits). */  
    protected void cancelChanges()
    {
        if (_table.isEditing())
            _table.getCellEditor().stopCellEditing();
        _table_model.restoreMomlRows();
    }
    /** Call-back to execute the momlText. Default implementation just prints momlText. */
    protected void _executeMoml(String momlText)
    {    
        if (momlText.length() > 0)
            System.out.println(momlText);
        else
            System.out.println("Nothing changed");
    }
    /** GUI call-back to select the row at the current position and activate the pop-up menu, */  
    protected void popUp(MouseEvent e)
    {
        if (_table.isEditing())
            _table.getCellEditor(_table.getEditingRow(), _table.getEditingColumn()).stopCellEditing();        
        _table.changeSelection(_table.rowAtPoint(e.getPoint()), 0, false, false);
        if (_popup == null)
            _popup = createPopUpMenu();
        _popup.show(e.getComponent(), e.getX(), e.getY());
    }
    /** GUI call-back to remove the selected editing row. */  
    protected void removeRow()
    {
//        System.out.println("Remove row " + _table.getSelectedRow());
        _table_model.removeEditingRow(_table.getSelectedRow());
    }

// Protected construction methods.
    /* GUI construction method to return the AddRow button. */    
    protected Component createAddRowButton()
    {
        JButton button = new JButton("Add Row");
        button.setMnemonic('R');
        button.addActionListener(new ActionListener() {  public void actionPerformed(ActionEvent e) { addEditingRow(); } });
        button.setToolTipText("Add an additional row to the form");
        return button;
    }        
    /* GUI construction method to return the Apply button. */    
    protected Component createApplyButton()
    {
        JButton button = new JButton("Apply");
        button.setMnemonic('A');
        button.addActionListener(new ActionListener() {  public void actionPerformed(ActionEvent e) { applyChanges(); } });
        button.setToolTipText("Apply changes and keep window open");
        return button;
    }    
    /* GUI construction method to return the Cancel button. */    
    protected Component createCancelButton()
    {
        JButton button = new JButton("Cancel");
        button.setMnemonic('C');
        button.addActionListener(new ActionListener() {  public void actionPerformed(ActionEvent e) { cancelChanges(); dispose(); } });
        button.setToolTipText("Lose changes and close window ");
        return button;
    }         
    /* GUI construction method to return the Ok button. */    
    protected Component createOkButton()
    {
        JButton button = new JButton("Ok");
        button.setMnemonic('O');
        button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { applyChanges(); dispose(); } });
        button.setToolTipText("Apply changes and close window");
        return button;
    }      
    /* GUI construction method to return the Undo button. */    
    protected Component createUndoButton()
    {
        JButton button = new JButton("Undo");
        button.setMnemonic('U');
        button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { cancelChanges(); } });
        button.setToolTipText("Lose changes and keep window open");
        return button;
    } 
    
    /* GUI construction method to return the row of buttons. */    
    protected Component createButtons()
    {
        JPanel buttonRow = new JPanel();
        buttonRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createAddRowButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createOkButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createCancelButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createUndoButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(createApplyButton());
        buttonRow.add(Box.createHorizontalGlue());
        return buttonRow;
    }     
    /* GUI construction method to return the pane of scroll-table plus buttons. */    
    protected void createComponents()
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(createScrollTable());
        pane.add(createButtons());
        getContentPane().add(pane, BorderLayout.CENTER);
    }    
    /* GUI construction method to return the pop-up menu. */    
    protected JPopupMenu createPopUpMenu()
    {
        JPopupMenu popUp = new JPopupMenu();
        JMenuItem deleteRow = new JMenuItem("Delete Row");
        deleteRow.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { removeRow(); } });
        popUp.add(deleteRow);
        return popUp;
    }    
    /* GUI construction method to return the scroll-table. */    
    protected Component createScrollTable()
    {
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        _table.setPreferredScrollableViewportSize(new Dimension(500,100));
        _table.addMouseListener(new PopupListener() { public void showPopup(MouseEvent e) { popUp(e); } });
        return new JScrollPane(_table);
    }

// Private methods
    /** Return the width necessary to display tableColumn header nicely. */
    private int columnHeaderWidth(TableColumn tableColumn)
    {
        TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
        if (headerRenderer == null)
            return 1;
        Object headerValue = tableColumn.getHeaderValue();
        Component cellComponent = headerRenderer.getTableCellRendererComponent(_table, headerValue, false, false, 0, 0);
        return cellComponent.getPreferredSize().width;
    }
    /** Return the width necessary to display tableColumn cells nicely. */
    private int widestCellInColumn(TableColumn tableColumn)
    {
        int columnIndex = tableColumn.getModelIndex();
        int maxWidth = 0;
        for (int rowIndex = _table.getRowCount(); rowIndex-- > 0; )
        {
            TableCellRenderer cellRenderer = _table.getCellRenderer(rowIndex, columnIndex);
            if (cellRenderer == null)
                System.out.println("Expected non-null cellRenderer at " + rowIndex + "," + columnIndex);
            else
            {
                Object cellValue = _table.getValueAt(rowIndex, columnIndex);
                Component cellComponent = cellRenderer.getTableCellRendererComponent(_table, cellValue, false, false, rowIndex, columnIndex);
                int cellWidth = cellComponent.getPreferredSize().width;
                if (cellWidth > maxWidth)
                    maxWidth = cellWidth;
            }
        } 
        return maxWidth;
    }

// Private data
    /** The data model for the _table widget. */
    private FormTableModel _table_model = new FormTableModel();
    /** The _table widget. */
    private JTable _table = new JTable();
    /** Cache for the pop-up menu once created. */
    private JPopupMenu _popup = null;
}