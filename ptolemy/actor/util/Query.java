/* Query dialog.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

// FIXME: This is not the right package for this.

package ptolemy.actor.util;

import java.awt.*;
import java.util.Hashtable;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// Query
/**
Create a query with various types of entry boxes and controls.

@author  Edward A. Lee
@version $Id$
*/
public class Query extends Panel {

    /** Construct a panel with no queries in it.
     */
    public Query() {
        _grid = new GridBagLayout();
        setLayout(_grid);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the current value in the entry with the given name
     *  and return as a boolean.  If the value of the entry is not
     *  a boolean, then throw an exception.  A boolean entry is created
     *  with the onoff() method.
     *  @return The value currently in the entry as a boolean.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception NumberFormatException If the value of the entry cannot
     *   be converted to a boolean.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public boolean booleanValue(String name)
             throws NoSuchElementException, NumberFormatException
    {
        Checkbox result = (Checkbox)(_entries.get(name));
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
                    name + " \" in the query box.");
        }
        return (new Boolean(result.getState())).booleanValue();
    }

    /** Get the current value in the entry with the given name
     *  and return as an integer.  If the value of the entry is not
     *  an integer, then throw an exception.
     *  @return The value currently in the entry as an integer.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception NumberFormatException If the value of the entry cannot
     *   be converted to an integer.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public int intValue(String name)
             throws NoSuchElementException, NumberFormatException
    {
        TextField result = (TextField)(_entries.get(name));
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
                    name + " \" in the query box.");
        }
        return (new Integer(result.getText())).intValue();
    }

    /** Create a single-line entry box with the specified name, label, and
     *  default value.  The width will be the default width.
     *  @param name The name used to identify the entry (when calling get).
     *  @param label The label to attach to the entry.
     *  @param defvalue Default value to appear in the entry box.
     */
    public void line(String name, String label, String defvalue) {
        line(name, label, defvalue, DEFAULT_ENTRY_WIDTH);
    }

    /** Create a single-line entry box with the specified name, label,
     *  default value, and width.
     *  @param name The name used to identify the entry (when calling get).
     *  @param label The label to attach to the entry.
     *  @param defvalue Default value to appear in the entry box.
     *  @param width The width of the entry box in characters.
     */
    public void line(String name, String label, String defvalue, int width) {
        Label lbl = new Label(label);
        TextField entrybox = new TextField(defvalue, width);
        _addPair(lbl, entrybox);
        _entries.put(name, entrybox);
    }

    /** Create an on-off check box.
     *  @param name The name used to identify the entry (when calling get).
     *  @param label The label to attach to the entry.
     *  @param defvalue Default value (true for on).
     */
    public void onoff(String name, String label, boolean defvalue) {
        Label lbl = new Label(label);
        Checkbox checkbox = new Checkbox();
        checkbox.setState(defvalue);
        _addPair(lbl, checkbox);
        _entries.put(name, checkbox);
    }

    /** Get the current value in the entry with the given name,
     *  and return as a String.  All entry types support this.
     *  @return The value currently in the entry as a String.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public String stringValue(String name) throws NoSuchElementException {
        TextField result = (TextField)(_entries.get(name));
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
                    name + " \" in the query box.");
        }
        return result.getText();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final int DEFAULT_ENTRY_WIDTH = 12;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a label and a widget to the panel.
     *  @param label The label.
     *  @param widget The interactive entry to the right of the label.
     */
    protected void _addPair(Label label, Component widget) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        _grid.setConstraints(label, constraints);
        add(label);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        _grid.setConstraints(widget, constraints);
        add(widget);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected GridBagLayout _grid;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Hashtable _entries = new Hashtable();
}
