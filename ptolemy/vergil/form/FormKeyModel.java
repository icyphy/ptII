/* A model for the keys in a port/parameter/property form

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
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/** FormKeyModel maintains a vector of key cell flyweights that define the permitted options for the key cvolumn in a parameter form.
 *
@author Edward D. Willink
@version $Id$
*/ 
class FormKeyModel
{
    /** Construct an empty key model. */
    public FormKeyModel() { super(); }
    /** Add aKey to the key model. */
    public void add(ControlKey aKey) { key_list.add(aKey); key_table.put(aKey.toString(), aKey); }
    /** Create a JComboBox comprising the list of keys. */
    public JComboBox createComboBox()
    {
        JComboBox comboBox = new JComboBox();
        Enumeration i = key_list.elements();
        while (i.hasMoreElements())
           comboBox.addItem(((ControlKey)i.nextElement()).toString());
        return comboBox;
    }
    /** Return the list array of key names. */
    public ControlKey get(String aString) { return (ControlKey)key_table.get(aString); }
    /** Return the list array of key names. */
    public Vector getKeys() { return key_list; }
// Private data
    /** The key names in combo box order. */
    private final Vector key_list = new Vector();
    /** The key names for fasty lookup. */
    private final Hashtable key_table = new Hashtable();
}

/** ControlKey defines the name of the permitted values of the control-key combo-box that defines the syntax of a FormRow in a ParameterForm. 
 A list of ControlKey is maintained by FormKeyModel which in turn paramterises the ControlCell that defines the behaviour of the control column. */
abstract class ControlKey
{
    /** Return the MoML to change oldKey into this key. */
    public abstract String momlChanges(ControlKey oldKey);
    /** Return the MoML delete keyword for this control key syntax. */
    public abstract String momlDeleteKey();
    /** Return the MoML keyword for this control key syntax. */
    public abstract String momlKey();
    /** Return the key_value as the string equivalent. */
    public String toString() { return _key_value; }
// Protected methods
    /** Comnstruct a control key value for a parameter form row with keyValue.
     * @param keyValue The control key value.
     */
    protected ControlKey(String keyValue) { _key_value = keyValue; }
// Private data
    /** Textual value of the selection key. */
    final private String _key_value;
}

/** PortKey defines the behaviour of a port-like control keyword such as in or out[] in the key-column. */
class PortKey extends ControlKey
{
    /** Construct a port-like keyword aString, for the isInput, isOutput, isMultiport state. */
    public PortKey(String keyText, boolean isInput, boolean isOutput, boolean isMultiport)
        { super(keyText); _is_input = isInput; _is_output = isOutput; _is_multiport = isMultiport; }
    /** Return the MoML to change oldKey into this key. */
    public String momlChanges(ControlKey oldKey)
    {
        PortKey oldPortKey = (oldKey != null) && (oldKey.getClass() == getClass()) ? (PortKey)oldKey : null;
        StringBuffer momlText = new StringBuffer();
        if ((oldPortKey == null) || (_is_input != oldPortKey._is_input))
        {
            momlText.append("\n  <property name=\"input\" value=\"");
            momlText.append(_is_input);
            momlText.append("\"/>");
        }
        if ((oldPortKey == null) || (_is_output != oldPortKey._is_output))
        {
            momlText.append("\n  <property name=\"output\" value=\"");
            momlText.append(_is_output);
            momlText.append("\"/>");
        }
        if ((oldPortKey == null) || (_is_multiport != oldPortKey._is_multiport))
        {
            momlText.append("\n  <property name=\"multiport\" value=\"");
            momlText.append(_is_multiport);
            momlText.append("\"/>");
        }
        return momlText.toString();
    }
    /** Return the MoML delete keyword for this control key syntax. */
    public String momlDeleteKey() { return "deletePort"; }
    /** Return the MoML keyword for this control key syntax. */
    public String momlKey() { return "port"; }
// Private data.
    private final boolean _is_input;
    private final boolean _is_output;
    private final boolean _is_multiport;
}

/** PortKey defines the behaviour of a parameter-like control keyword in the key-column. */
class ParameterKey extends ControlKey
{
    /** Construct a parameter-like keyword aString. */
    public ParameterKey(String aString) { super(aString); }
    /** Return the MoML to change oldKey into this key. */
    public String momlChanges(ControlKey oldKey) { return ""; }
    /** Return the MoML delete keyword for this control key syntax. */
    public String momlDeleteKey() { return "deleteProperty"; }
    /** Return the MoML keyword for this control key syntax. */
    public String momlKey() { return "property"; }
}  
