/* An JTable Cell editor for Ptolemy II Types
Copyright (c) 2005 The Regents of the University of California.
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

import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.IllegalActionException;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 A JTable Cell editor for Ptolemy II types.
 
<p>Based on IntegerEditor from
http://java.sun.com/docs/books/tutorial/uiswing/components/example-1dot4/IntegerEditor.java

   @author Christopher Brooks, Sun Microsystems
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
 */
public class TypeCellEditor extends DefaultCellEditor {

    /** Construct a JTable Cell editor for Ptolemy II types.
     *  @param comboBox The combo box that provides type choices.
     */   
    public TypeCellEditor(final JComboBox comboBox) {
        super(comboBox);
        _comboBox = (JComboBox)getComponent();

        // React when the user presses Enter while the editor is
        // active.  (Tab is handled as specified by
        // JFormattedTextField's focusLostBehavior property.)
        comboBox.getInputMap().put(KeyStroke.getKeyStroke(
                                        KeyEvent.VK_ENTER, 0),
                                        "check");
        comboBox.getActionMap().put("check", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
		if (!isValid((String)(comboBox.getSelectedItem()))) {
                    userSaysRevert();
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {
        JComboBox comboBox =
            (JComboBox)super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        _oldValue = comboBox.getSelectedItem();
        comboBox.setSelectedItem(value);
        return comboBox;
    }

    /** Ensure that the value is a valid Ptolemy II type.
     *  @returns The string value of the selected item in the combobox.
     */
    public Object getCellEditorValue() {
        // FIXME: do we need to get comboBox like this each time?
        JComboBox comboBox = (JComboBox)getComponent();
        Object o = comboBox.getSelectedItem();
        return o.toString();
    }

    /** Override to check whether the edit is valid,
     * setting the value if it is and complaining if
     * it isn't.  If it's OK for the editor to go
     * away, we need to invoke the superclass's version 
     * of this method so that everything gets cleaned up.
     */
    public boolean stopCellEditing() {
        // FIXME: do we need to get comboBox like this each time?
        JComboBox comboBox = (JComboBox)getComponent();
        //if (comboBox.isEditValid()) {
        if (!isValid((String)(comboBox.getSelectedItem()))) {
            if (!userSaysRevert()) { //user wants to edit
	        return false; //don't let the editor go away
	    } 
        }
        return super.stopCellEditing();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the cellValue is a valid Ptolemy II type.
     *  @param cellValue The possible Ptolemy II type.
     *  @return true if cellValue is a valid Ptolemy II type.
     */
    public boolean isValid(String cellValue) {
        try {
            if (cellValue.equals("")) {
                return true;
            }
            
            ASTPtRootNode tree = _typeParser.generateParseTree(cellValue);
            Token result = _parseTreeEvaluator
                .evaluateParseTree(tree, null);
        } catch (IllegalActionException e) {
            _message = e.getMessage();
            return false;
        }
        
        return true;
    }

    /** 
     * Lets the user know that the text they entered is 
     * bad. Returns true if the user elects to revert to
     * the last good value.  Otherwise, returns false, 
     * indicating that the user wants to continue editing.
     */
    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        //_comboBox.selectAll();
        Object[] options = {"Edit",
                            "Revert"};
        int answer = JOptionPane.showOptionDialog(
            SwingUtilities.getWindowAncestor(_comboBox),
            "The type entered is not a valid type. "
            + "Try a type like \"double\"."
            + "The error message was: " + _message
            + "\nYou can either continue editing "
            + "or revert to the last valid value.",
            "Invalid Text Entered",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[1]);
	    
        if (answer == 1) { //Revert!
            _comboBox.setSelectedItem(_oldValue);
	    return true;
        }
	return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Used to validate the type. */
    private static PtParser _typeParser = new PtParser();

    /** Used to validate the type. */
    private static ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();

    /** The combo box. */
    private JComboBox _comboBox;

    /** The error message. */
    private String _message;

    /** Old value of the combo box. */
    private Object _oldValue;
}
