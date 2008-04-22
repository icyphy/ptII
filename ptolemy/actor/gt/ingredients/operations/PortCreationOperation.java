/*

@Copyright (c) 2008 The Regents of the University of California.
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
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PortCreationOperation extends Operation {

    public PortCreationOperation(GTIngredientList owner) {
        this(owner, "");
    }

    public PortCreationOperation(GTIngredientList owner, String values) {
        this(owner, null, null, null, true, false, false, true);
        setValues(values);
    }

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

    public ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            Entity patternEntity, Entity replacementEntity, Entity hostEntity)
            throws IllegalActionException {
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
        return new MoMLChangeRequest(this, hostEntity, moml.toString(), null);
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

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

    public boolean isPortClassEnabled() {
        return isEnabled(1);
    }

    public boolean isPortTypeEnabled() {
        return isEnabled(2);
    }

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

    public void validate() throws ValidationException {
        if (_portName.equals("")) {
            throw new ValidationException("Port name must not be empty.");
        }

        if (isPortClassEnabled()) {
            try {
                if (!Port.class.isAssignableFrom(Class.forName(_portClass))) {
                    throw new ValidationException("Port class must be the " +
                            "name of a class that is a subclass of " +
                            "ptolemy.kernel.Port.");
                }
            } catch (ClassNotFoundException e) {
                throw new ValidationException("Port class " + _portClass +
                        "not found.");
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

    private static final OperationElement[] _ELEMENTS = {
        new StringOperationElement("name", false, false),
        new StringOperationElement("class", true, false),
        new StringOperationElement("type", true, false),
        new BooleanOperationElement("input", false),
        new BooleanOperationElement("output", false),
        new BooleanOperationElement("multiport", false),
        new BooleanOperationElement("autoRename", false)
    };

    private boolean _autoRename;

    private boolean _input;

    private boolean _multiport;

    private boolean _output;

    private String _portClass;

    private String _portName;

    private PtolemyExpressionString _portType;

}
