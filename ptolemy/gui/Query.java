/* Query dialog.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.IllegalArgumentException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.ChangeListener;

//////////////////////////////////////////////////////////////////////////
//// Query
/**
Create a query with various types of entry boxes and controls.  Each type
of entry box has a colon and space appended to the end of its label, to
ensure uniformity.
Here is one example of creating a query with a radio button:
    query = new Query();
    getContentPane().add(query);
    String[] options = {"water", "soda", "juice", "none"};
    query.addRadioButtons("radio", "Radio buttons", options, "water");

@author  Edward A. Lee, Manda Sutijono
@version $Id$
*/
public class Query extends JPanel {

    /** Construct a panel with no entries in it.
     */
    public Query() {
        // FIXME: The layout isn't quite right... When setTextWidth() is
        // called with a larger number, more space also gets allocated
        // to the labels, which is not correct...
        _grid = new GridBagLayout();
        _constraints = new GridBagConstraints();
        _constraints.fill = GridBagConstraints.HORIZONTAL;
        _constraints.weightx = 1.0;
        _constraints.anchor = GridBagConstraints.NORTHWEST;
        setLayout(_grid);
        // It's not clear whether the following has any real significance...
        setOpaque(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an on-off check box.
     *  @param name The name used to identify the entry (when calling get).
     *  @param label The label to attach to the entry.
     *  @param defaultValue The default value.
     */
    public void addCheckBox(String name, String label, boolean defaultValue) {
        JLabel lbl = new JLabel(label + ": ");
        lbl.setBackground(_background);
        JRadioButton checkbox = new JRadioButton();
        checkbox.setBackground(_background);
        checkbox.setOpaque(false);
        checkbox.setSelected(defaultValue);
        _addPair(name, lbl, checkbox);
        // Add the listener last so that there is no notification
        // of the first value.
        checkbox.addItemListener(new QueryItemListener(name));
    }

    /** Create a choice menu.
     *  @param name The name used to identify the entry (when calling get).
     *  @param label The label to attach to the entry.
     *  @param values The list of possible choices.
     *  @param defaultChoice Default choice (true for on).
     */
    public void addChoice(String name, String label,
            String[] values, String defaultChoice) {
        JLabel lbl = new JLabel(label + ": ");
        lbl.setBackground(_background);
        JComboBox combobox = new JComboBox(values);
        combobox.setEditable(false);
        combobox.setBackground(Color.white);
        combobox.setSelectedItem(defaultChoice);
        _addPair(name, lbl, combobox);
        // Add the listener last so that there is no notification
        // of the first value.
        combobox.addItemListener(new QueryItemListener(name));
    }

    /** Create a simple one-line text display, a non-editable value that
     *  is set externally using the setDisplay() method.
     *  @param name The name used to identify the entry (when calling get).
     *  @param label The label to attach to the entry.
     *  @param theValue Default string to display.
     */
    public void addDisplay(String name, String label, String theValue) {
        JLabel lbl = new JLabel(label + ": ");
        lbl.setBackground(_background);
        // NOTE: JLabel would be a reasonable choice here, but at
        // least in the current version of swing, JLabel.setText() does
        // not work.
        JTextArea displayField = new JTextArea(theValue, 1, 10);
        displayField.setBackground(_background);
        _addPair(name, lbl, displayField);
    }

    /** Create a single-line entry box with the specified name, label, and
     *  default value.  To control the width of the box, call setTextWidth()
     *  first.
     *  @param name The name used to identify the entry (when accessing
     *   the entry).
     *  @param label The label to attach to the entry.
     *  @param defaultValue Default value to appear in the entry box.
     */
    public void addLine(String name, String label, String defaultValue) {
        JLabel lbl = new JLabel(label + ": ");
        lbl.setBackground(_background);
        JTextField entryBox = new JTextField(defaultValue, _width);
        entryBox.setBackground(Color.white);
        _addPair(name, lbl, entryBox);
        // Add the listener last so that there is no notification
        // of the first value.
        entryBox.addActionListener(new QueryActionListener(name));
        // Add a listener for loss of focus.  When the entry gains
        // and then loses focus, listeners are notified of an update.
        entryBox.addFocusListener(new QueryFocusListener(name));
    }

    /** Add a listener.  The changed() method of the listener will be
     *  called when any of the entries is changed.  Note that "line"
     *  entries only trigger this call when Return is pressed, or
     *  when the entry gains and then loses the keyboard focus.
     *  Notice that the currently selected line loses focus when the
     *  panel is destroyed, so notification of any changes that
     *  have been made will be done at that time.  That notification
     *  will occur in the UI thread, and may be later than expected.
     *  If the listener has already been added, then do nothing.
     *  @param listener The listener to add.
     */
    public void addQueryListener(QueryListener listener) {
        if(_listeners == null) _listeners = new Vector();
        if(_listeners.contains(listener)) return;
        _listeners.add(listener);
    }

    /** Create a bank of radio buttons.  A radio button provides a list of
     *  choices, only one of which may be chosen at a time.
     *  @param name The name used to identify the entry (when calling get).
     *  @param label The label to attach to the entry.
     *  @param values The list of possible choices.
     *  @param defaultValue Default value.
     */
    public void addRadioButtons(String name, String label,
            String[] values, String defaultValue) {
        JLabel lbl = new JLabel(label + ": ");
        lbl.setBackground(_background);
        FlowLayout flow = new FlowLayout();
        flow.setAlignment(FlowLayout.LEFT);
        Panel buttonPanel = new Panel(flow);
        ButtonGroup group = new ButtonGroup();
        QueryActionListener listener = new QueryActionListener(name);

        // Regrettably, ButtonGroup provides no method to find out
        // which button is selected, so we have to go through a
        // song and dance here...
        JRadioButton[] buttons = new JRadioButton[values.length];
        for (int i = 0; i < values.length; i++) {
            JRadioButton checkbox = new JRadioButton(values[i]);
            buttons[i] = checkbox;
            checkbox.setBackground(_background);
            // The following (essentially) undocumented method does nothing...
            // checkbox.setContentAreaFilled(true);
            checkbox.setOpaque(false);
            if (values[i].equals(defaultValue)) {
                checkbox.setSelected(true);
            }
            group.add(checkbox);
            buttonPanel.add(checkbox);
            // Add the listener last so that there is no notification
            // of the first value.
            checkbox.addActionListener(listener);
        }
        _addPair(name, lbl, buttonPanel);
        // Override what is stored in _addPair().
        _entries.put(name, buttons);
    }

    /** Create a slider with the specified name, label, default
     *  value, maximum, and minimum.
     *  @param name The name used to identify the slider.
     *  @param label The label to attach to the slider.
     *  @param defaultValue Initial position of slider.
     *  @param maximum Maximum value of slider.
     *  @param minimum Minimum value of slider.
     *  @exception IllegalArgumentException If the desired default value
     *   is not between the minimum and maximum.
     */
    public void addSlider(String name, String label, int defaultValue,
            int minimum, int maximum)
            throws IllegalArgumentException {
        JLabel lbl = new JLabel(label + ": ");
        if (minimum > maximum) {
            int temp = minimum;
            minimum = maximum;
            maximum = temp;
        }
        if ((defaultValue > maximum) || (defaultValue < minimum)) {
            throw new IllegalArgumentException("Desired default " +
            "value \"" + defaultValue + "\" does not fall " +
            "between the minimum and maximum.");
        }
        JSlider slider = new JSlider(minimum, maximum, defaultValue);
        _addPair(name, lbl, slider);
        slider.addChangeListener(new SliderListener(name));
    }

    /** Get the current value in the entry with the given name
     *  and return as a boolean.  If the entry is not a checkbox,
     *  then throw an exception.
     *  @return The state of the checkbox.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry is not a
     *   checkbox.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public boolean booleanValue(String name)
            throws NoSuchElementException, IllegalArgumentException {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + "\" in the query box.");
        }
        if (result instanceof JRadioButton) {
            return ((JRadioButton)result).isSelected();
        } else {
            throw new IllegalArgumentException("Item named \"" +
            name + "\" is not a radio button, and hence does not have "
            + "a boolean value.");
        }
    }

    /** Get the current value in the entry with the given name
     *  and return as a double value.  If the entry is not a line,
     *  then throw an exception.  If the value of the entry is not
     *  a double, then throw an exception.
     *  @return The value currently in the entry as a double.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception NumberFormatException If the value of the entry cannot
     *   be converted to a double.  This is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry is not a
     *   line.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public double doubleValue(String name)
            throws IllegalArgumentException, NoSuchElementException,
            NumberFormatException {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + " \" in the query box.");
        }
        if (result instanceof JTextField) {
            return (new Double(((JTextField)result).getText())).doubleValue();
        } else {
            throw new IllegalArgumentException("Item named \"" +
            name + "\" is not a text line, and hence cannot be converted to "
            + "a double value.");
        }
    }

    /** Return the preferred size, since it usually does not make
     *  sense to stretch a query box.  Currently (JDK 1.3), only BoxLayout
     *  pays any attention to this.
     *  @return The maximum desired size.
     */
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /** Get the current value in the entry with the given name
     *  and return as an integer.  If the entry is not a line,
     *  choice, or slider, then throw an exception.
     *  If it is a choice or radio button, then return the
     *  index of the selected item.
     *  @return The value currently in the entry as an integer.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception NumberFormatException If the value of the entry cannot
     *   be converted to an integer.  This is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry is not a
     *   choice, line, or slider.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public int intValue(String name)
            throws IllegalArgumentException, NoSuchElementException,
            NumberFormatException {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + " \" in the query box.");
        }
        if (result instanceof JTextField) {
            return (new Integer(((JTextField)result).getText())).intValue();
        } else if (result instanceof JSlider) {
            return ((JSlider)result).getValue();
        } else if (result instanceof JComboBox) {
            return ((JComboBox)result).getSelectedIndex();
        } else if (result instanceof JRadioButton[]) {
            // Regrettably, ButtonGroup gives no way to determine
            // which button is selected, so we have to search...
            JRadioButton[] buttons = (JRadioButton[])result;
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].isSelected()) {
                    return i;
                }
            }
            // In theory, we shouldn't get here, but the compiler
            // is unhappy without a return.
            return -1;
        } else {
            throw new IllegalArgumentException("Item named \"" +
            name + "\" is not a text line or slider, and hence "
            + "cannot be converted to "
            + "an integer value.");
        }
    }

    /** Remove a listener.  If the listener has not been added, then
     *  do nothing.
     *  @param listener The listener to remove.
     */
    public void removeQueryListener(QueryListener listener) {
        if(_listeners == null) return;
        _listeners.remove(listener);
    }

    /** Set the value in the entry with the given name.
     *  The second argument must be a string that can be parsed to the
     *  proper type for the given entry, or an exception is thrown.
     *  @param name The name used to identify the entry (when calling get).
     *  @param value The value to set the entry to.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the value does not parse
     *   to the appropriate type.
     */
    public void set(String name, String value)
            throws NoSuchElementException, IllegalArgumentException {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
                    name + " \" in the query box.");
        }
        // FIXME: Surely there is a better way to do this...
        // We should define a set of inner classes, one for each entry type.
        // Currently, this has to be updated each time a new entry type
        // is added.
        if (result instanceof JTextField) {
            ((JTextField)result).setText(value);
        } else if (result instanceof JTextArea) {
            ((JTextArea)result).setText(value);
        } else if (result instanceof JRadioButton) {
            Boolean flag = new Boolean(value);
            setBoolean(name, flag.booleanValue());
        } else if (result instanceof JSlider) {
            Integer parsed = new Integer(value);
            ((JSlider)result).setValue(parsed.intValue());
        } else if (result instanceof JComboBox) {
            ((JComboBox)result).setSelectedItem(value);
        } else if (result instanceof JRadioButton[]) {
            // Regrettably, ButtonGroup gives no way to determine
            // which button is selected, so we have to search...
            JRadioButton[] buttons = (JRadioButton[])result;
            for (int i = 0; i < buttons.length; i++) {
                if (value.equals(buttons[i].getText())) {
                    buttons[i].setSelected(true);
                } else {
                    buttons[i].setSelected(false);
                }
            }
        } else {
            throw new IllegalArgumentException("Query class cannot set"
            + " a string representation for entries of type "
            + result.getClass());
        }
    }

    /** Set the background color for all the widgets.
     *  @param color The background color.
     */
    public void setBackground(Color color) {
        super.setBackground(color);
        _background = color;
        // Set the background of any components that already exist.
        Component[] components = getComponents();
        for (int i = 0; i < components.length; i++) {
            if (!(components[i] instanceof JTextField)) {
                components[i].setBackground(_background);
            }
        }
    }

    /** Set the current value in the entry with the given name.
     *  If the entry is not a checkbox, then throw an exception.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry is not a
     *   checkbox.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public void setBoolean(String name, boolean value)
            throws NoSuchElementException, IllegalArgumentException {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + "\" in the query box.");
        }
        if (result instanceof JRadioButton) {
            ((JRadioButton)result).setSelected(value);
        } else {
            throw new IllegalArgumentException("Item named \"" +
            name + "\" is not a radio button, and hence does not have "
            + "a boolean value.");
        }
    }

    /** Set the displayed text of an entry that has been added using
     *  addDisplay.
     *  @param name The name of the entry.
     *  @param value The string to display.
     *  @exception NoSuchElementException If there is no entry with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry is not a
     *   display.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public void setDisplay(String name, String value)
            throws  NoSuchElementException, IllegalArgumentException {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + " \" in the query box.");
        }
        if (result instanceof JTextArea) {
            JTextArea label = (JTextArea)result;
            label.setText(value);
        } else {
            throw new IllegalArgumentException("Item named \"" +
            name + "\" is not a display, and hence cannot be set using "
            + "setDisplay().");
        }
    }

    /** For line, display, check box, slider, radio button, or choice
     *  entries made, if the second argument is false, then it will
     *  be disabled.
     *  @param name The name of the entry.
     *  @param value If false, disables the entry.
     */
    public void setEnabled(String name, boolean value) {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + " \" in the query box.");
        }
        if(result instanceof JComponent) {
            ((JComponent)result).setEnabled(value);
        } else if(result instanceof JRadioButton[]) {
            JRadioButton[] buttons = (JRadioButton[])result;
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setEnabled(value);
            }
        }
    }

    /** Set the displayed text of an item that has been added using
     *  addLine.
     *  @param name The name of the entry.
     *  @param value The string to display.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry is not a
     *   display.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public void setLine(String name, String value) {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + " \" in the query box.");
        }
        if (result instanceof JTextField) {
            JTextField line = (JTextField)result;
            line.setText(value);
        } else {
            throw new IllegalArgumentException("Item named \"" +
            name + "\" is not a line, and hence cannot be set using "
            + "setLine().");
        }
    }

    /** Set the position of an item that has been added using
     *  addSlider.
     *  @param name The name of the entry.
     *  @param value The value to set the slider position.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry is not a
     *   slider.  This is a runtime exception, so it
     *   need not be declared explicitly.
     */
    public void setSlider(String name, int value) {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
            name + " \" in the query box.");
        }
        if (result instanceof JSlider) {
            JSlider theSlider = (JSlider)result;
	    // Set the new slider position.
            theSlider.setValue(value);
        } else {
            throw new IllegalArgumentException("Item named \"" +
            name + "\" is not a slider, and hence cannot be set using "
            + "setSlider().");
        }
    }


    /** Specify the preferred width to be used for entry boxes created
     *  in using addLine().  If this is called multiple times, then
     *  it only affects subsequent calls.
     *
     *  @param characters The preferred width.
     */
    public void setTextWidth(int characters) {
        _width = characters;
    }

    /** Specify a tool tip to appear when the mouse lingers over the label.
     *  @param name The name of the entry.
     *  @param tip The text of the tool tip.
     */
    public void setToolTip(String name, String tip) {
        JLabel label = (JLabel)_labels.get(name);
        if (label != null) {
            label.setToolTipText(tip);
        }
    }

    /** Get the current value in the entry with the given name,
     *  and return as a String.  All entry types support this.
     *  @return The value currently in the entry as a String.
     *  @exception NoSuchElementException If there is no item with the
     *   specified name.  Note that this is a runtime exception, so it
     *   need not be declared explicitly.
     *  @exception IllegalArgumentException If the entry type does not
     *   have a string representation (this should not be thrown).
     */
    public String stringValue(String name)
            throws NoSuchElementException, IllegalArgumentException {
        Object result = _entries.get(name);
        if(result == null) {
            throw new NoSuchElementException("No item named \"" +
                    name + " \" in the query box.");
        }
        // FIXME: Surely there is a better way to do this...
        // We should define a set of inner classes, one for each entry type.
        // Currently, this has to be updated each time a new entry type
        // is added.
        if (result instanceof JTextField) {
            return ((JTextField)result).getText();
        } else if (result instanceof JTextArea) {
            return ((JTextArea)result).getText();
        } else if (result instanceof JRadioButton) {
            JRadioButton radioButton = (JRadioButton)result;
            if (radioButton.isSelected()) {
                return "true";
            } else {
                return "false";
            }
        } else if (result instanceof JSlider) {
            return "" + ((JSlider)result).getValue();
        } else if (result instanceof JComboBox) {
            return (String)(((JComboBox)result).getSelectedItem());
        } else if (result instanceof JRadioButton[]) {
            // Regrettably, ButtonGroup gives no way to determine
            // which button is selected, so we have to search...
            JRadioButton[] buttons = (JRadioButton[])result;
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].isSelected()) {
                    return buttons[i].getText();
                }
            }
            // In theory, we shouldn't get here, but the compiler
            // is unhappy without a return.
            return "";
        } else {
            throw new IllegalArgumentException("Query class cannot generate"
            + " a string representation for entries of type "
            + result.getClass());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The default width of entries created with addLine(). */
    public static final int DEFAULT_ENTRY_WIDTH = 12;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a label and a widget to the panel.
     *  @param name The name of the entry.
     *  @param label The label.
     *  @param widget The interactive entry to the right of the label.
     */
    protected void _addPair(String name, JLabel label, Component widget) {
        // Surely there is a better layout manager in swing...
        // Note that Box and BoxLayout do not work because they do not
        // support gridded layout.
        _constraints.gridwidth = 1;
        _grid.setConstraints(label, _constraints);
        add(label);
        _constraints.gridwidth = GridBagConstraints.REMAINDER;
        _grid.setConstraints(widget, _constraints);
        add(widget);
        _entries.put(name, widget);
        _labels.put(name, label);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The background color as set by setBackground().
     *  This defaults to white.
     */
    protected Color _background = Color.white;

    /** Layout control. */
    protected GridBagLayout _grid;

    /** Standard constraints for use with _grid. */
    protected GridBagConstraints _constraints;

    /** List of registered listeners. */
    protected Vector _listeners;

    ///////////////////////////////////////////////////////////////////
    ////                         friendly methods                  ////

    // Notify all registered listeners that something changed.
    void _notifyListeners(String name) {
        if(_listeners != null) {
            Enumeration listeners = _listeners.elements();
            while(listeners.hasMoreElements()) {
                QueryListener queueListener =
                    (QueryListener)(listeners.nextElement());
                queueListener.changed(name);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The hashtable of items in the query.
    private Map _entries = new HashMap();

    // The hashtable of labels in the query.
    private Map _labels = new HashMap();

    // The width of the text boxes.
    private int _width = DEFAULT_ENTRY_WIDTH;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for "line" and radio button entries.
     */
    class QueryActionListener implements ActionListener {
        public QueryActionListener(String name) {
            _name = name;
        }
        /** Call all registered QueryListeners. */
        public void actionPerformed(ActionEvent e) {
            _notifyListeners(_name);
        }
        private String _name;
    }

    /** Listener for line entries, for when they lose the focus.
     */
    class QueryFocusListener implements FocusListener {
        public QueryFocusListener(String name) {
            _name = name;
        }
        public void focusGained(FocusEvent e) {
            // Nothing to do.
        }
        public void focusLost(FocusEvent e) {
            _notifyListeners(_name);
        }
        private String _name;
    }

    /** Listener for "CheckBox" entries.
     */
    class QueryItemListener implements ItemListener {
        public QueryItemListener(String name) {
            _name = name;
        }

        /** Call all registered QueryListeners. */
        public void itemStateChanged(ItemEvent e) {
            _notifyListeners(_name);
        }
        private String _name;
    }

    /** Listener for changes in slider.
     */
    class SliderListener implements ChangeListener {
        public SliderListener(String name) {
            _name = name;
        }
        /** Call all registered QueryListeners. */
        public void stateChanged(ChangeEvent event) {
            _notifyListeners(_name);
        }
        private String _name;
    }
}
