/* A simple display of labeled values.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
import java.awt.event.*;
import java.util.*;

import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Table
/**
A simple display of labeled values.

@author  Edward A. Lee
@version $Id$
*/
public class Table extends Panel {

    /** Construct a table with no items in it.
     */
    public Table() {
        _grid = new GridBagLayout();
        setLayout(_grid);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // FIXME: Need a clear method, with an optional argument that removes
    // all the entries.

    /** Create a single-line item with the specified name, label, and
     *  default value.  The width will be the default width.
     *  @param name The name used to identify the entry (when calling set).
     *  @param label The label to attach to the item.
     *  @param defvalue The initial value to appear for the item.
     */
    public void line(String name, String label, String defvalue) {
        line(name, label, defvalue, DEFAULT_VALUE_WIDTH);
    }

    /** Create a single-line item with the specified name, label,
     *  default value, and width.
     *  @param name The name used to identify the entry (when calling set).
     *  @param label The label to attach to the entry.
     *  @param defvalue The initial value to appear in the entry box.
     *  @param width The width of the value display in characters.
     */
    public void line(String name, String label, String defvalue, int width) {
        // FIXME: This should really use the jdk 1.2 JTable class
        Label lbl = new Label(label);
        Label valueBox = new Label(defvalue);
        _addPair(lbl, valueBox);
        _items.put(name, valueBox);
    }

    /** Set the displayed value of the item with the given name.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public void set(String name, String value) throws NoSuchElementException {
        Label valueBox = (Label)(_items.get(name));
        if(valueBox == null) {
            throw new NoSuchElementException("No item named \"" +
                    name + " \" in the table.");
        }
        valueBox.setText(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final int DEFAULT_VALUE_WIDTH = 12;

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

    /** Layout control. */
    protected GridBagLayout _grid;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Hashtable _items = new Hashtable();
}
