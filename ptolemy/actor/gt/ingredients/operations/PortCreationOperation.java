/* An operation to create a port.

@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.gt.ingredients.operations;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.Replacement;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.util.PtolemyExpressionString;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// PortCreationOperation

/**
 An operation to create a port.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PortCreationOperation extends Operation {

    /** Construct an operation within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public PortCreationOperation(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct an operation within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public PortCreationOperation(GTIngredientList owner, String values) {
        this(owner, null, null, null, true, false, false, true);
        setValues(values);
    }

    /** Construct an operation within the given list as its owner and with the
     *  given value to each of its elements..
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param portName Value of the portName element.
     *  @param portClass Value of the portClass element.
     *  @param portType Value of the portType element.
     *  @param input Value of the input element.
     *  @param output Value of the output element.
     *  @param multiport Value of the multiport element.
     *  @param autoRename Value of the autoRename element.
     */
    public PortCreationOperation(GTIngredientList owner, String portName,
            String portClass, String portType, boolean input, boolean output,
            boolean multiport, boolean autoRename) {
        super(owner, 7);

        NamedObj container = owner.getOwner().getContainer();
        _portName = portName;
        _portClass = portClass;
        _portType = new PtolemyExpressionString(container, portType);
        _input = input;
        _output = output;
        _multiport = multiport;
        _autoRename = autoRename;
    }

    /** Get the change request to update the object in the host model.
     *
     *  @param pattern The pattern of the transformation rule.
     *  @param replacement The replacement of the transformation rule.
     *  @param matchResult The match result.
     *  @param patternObject The object in the pattern, or null.
     *  @param replacementObject The object in the replacement that corresponds
     *   to the object in the pattern.
     *  @param hostObject The object in the host model corresponding to the
     *   object in the replacement.
     *  @return The change request.
     *  @exception IllegalActionException If error occurs in generating the
     *   change request.
     */
    @Override
    public ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            NamedObj patternObject, NamedObj replacementObject,
            NamedObj hostObject) throws IllegalActionException {
        StringBuffer moml = new StringBuffer();
        moml.append("<port name=\"");
        moml.append(_portName);
        moml.append("\" class=\"");
        if (!isPortClassEnabled()) {
            moml.append("ptolemy.actor.TypedIOPort");
        } else {
            moml.append(_portClass);
        }
        moml.append("\">\n");
        moml.append("  <property name=\"_type\" ");
        moml.append("class=\"ptolemy.actor.TypeAttribute\" value=\"");
        if (!isPortTypeEnabled()) {
            moml.append("general");
        } else {
            moml.append(_portType.get());
        }
        moml.append("\"/>\n");
        if (_input) {
            moml.append("  <property name=\"input\"/>\n");
        }
        if (_output) {
            moml.append("  <property name=\"output\"/>\n");
        }
        if (_multiport) {
            moml.append("  <property name=\"multiport\"/>\n");
        }
        moml.append("</port>\n");
        if (_autoRename) {
            moml.insert(0, "<group name=\"auto\">\n");
            moml.append("</group>\n");
        }
        return new MoMLChangeRequest(this, hostObject, moml.toString(), null);
    }

    /** Get the array of elements defined in this GTIngredient.
     *
     *  @return The array of elements.
     */
    @Override
    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    /** Get the value of the index-th elements.
     *
     *  @param index The index.
     *  @return The value.
     *  @see #setValue(int, Object)
     */
    @Override
    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _portName;
        case 1:
            return _portClass;
        case 2:
            return _portType;
        case 3:
            return _input;
        case 4:
            return _output;
        case 5:
            return _multiport;
        case 6:
            return _autoRename;
        default:
            return null;
        }
    }

    /** Get a string that describes the values of all the elements.
     *
     *  @return A string that describes the values of all the elements.
     *  @see #setValues(String)
     */
    @Override
    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _portName);
        _encodeStringField(buffer, 1, _portClass);
        _encodeStringField(buffer, 2, _portType.get());
        _encodeBooleanField(buffer, 3, _input);
        _encodeBooleanField(buffer, 4, _output);
        _encodeBooleanField(buffer, 5, _multiport);
        _encodeBooleanField(buffer, 6, _autoRename);
        return buffer.toString();
    }

    /** Check whether this GTIngredient is applicable to the object.
     *
     *  @param object The object.
     *  @return true if this GTIngredient is applicable; false otherwise.
     */
    @Override
    public boolean isApplicable(NamedObj object) {
        return super.isApplicable(object) && object instanceof ComponentEntity
                && !(object instanceof State);
    }

    /** Return whether the portClass element is enabled.
     *
     *  @return true if the portClass element is enabled.
     */
    public boolean isPortClassEnabled() {
        return isEnabled(1);
    }

    /** Return whether the portType element is enabled.
     *
     *  @return true if the portType element is enabled.
     */
    public boolean isPortTypeEnabled() {
        return isEnabled(2);
    }

    /** Set the value of the index-th element.
     *
     *  @param index The index.
     *  @param value The value.
     *  @see #getValue(int)
     */
    @Override
    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _portName = (String) value;
            break;
        case 1:
            _portClass = (String) value;
            break;
        case 2:
            _portType.set((String) value);
            break;
        case 3:
            _input = ((Boolean) value).booleanValue();
            break;
        case 4:
            _output = ((Boolean) value).booleanValue();
            break;
        case 5:
            _multiport = ((Boolean) value).booleanValue();
            break;
        case 6:
            _autoRename = ((Boolean) value).booleanValue();
        }
    }

    /** Set the values of all the elements with a string that describes them.
     *
     *  @param values A string that describes the new values of all the
     *   elements.
     *  @see #getValues()
     */
    @Override
    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _portName = _decodeStringField(0, fieldIterator);
        _portClass = _decodeStringField(1, fieldIterator);
        _portType.set(_decodeStringField(2, fieldIterator));
        _input = _decodeBooleanField(3, fieldIterator);
        _output = _decodeBooleanField(4, fieldIterator);
        _multiport = _decodeBooleanField(5, fieldIterator);
        _autoRename = _decodeBooleanField(6, fieldIterator);
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
        if (_portName.equals("")) {
            throw new ValidationException("Port name must not be empty.");
        }

        if (isPortClassEnabled()) {
            try {
                if (!Port.class.isAssignableFrom(Class.forName(_portClass))) {
                    throw new ValidationException("Port class must be the "
                            + "name of a class that is a subclass of "
                            + "ptolemy.kernel.Port.");
                }
            } catch (ClassNotFoundException e) {
                throw new ValidationException("Port class " + _portClass
                        + "not found.");
            }
        }

        if (isPortTypeEnabled()) {
            try {
                _portType.getToken();
            } catch (IllegalActionException e) {
                throw new ValidationException("Type expression \"" + _portType
                        + "\" cannot be parsed.", e);
            }
        }
    }

    /** The elements.
     */
    private static final OperationElement[] _ELEMENTS = {
        new StringOperationElement("name", false, false),
        new StringOperationElement("class", true, false),
        new StringOperationElement("type", true, false),
        new BooleanOperationElement("input", false),
        new BooleanOperationElement("output", false),
        new BooleanOperationElement("multiport", false),
        new BooleanOperationElement("autoRename", false) };

    /** Value of the autoRename element.
     */
    private boolean _autoRename;

    /** Value of the input element.
     */
    private boolean _input;

    /** Value of the multiport element.
     */
    private boolean _multiport;

    /** Value of the output element.
     */
    private boolean _output;

    /** Value of the portClass element.
     */
    private String _portClass;

    /** Value of the portName element.
     */
    private String _portName;

    /** Value of the portType element.
     */
    private PtolemyExpressionString _portType;

}
