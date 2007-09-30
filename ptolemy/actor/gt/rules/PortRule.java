/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.actor.gt.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.Rule;
import ptolemy.actor.gt.RuleAttribute;
import ptolemy.actor.gt.RuleList;
import ptolemy.actor.gt.RuleValidationException;
import ptolemy.data.type.Type;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;

public class PortRule extends Rule {

    public PortRule() {
        this("");
    }

    public PortRule(String values) {
        this(null, null, false, false, false);
        setValues(values);
    }

    public PortRule(String portName, String portType, boolean input,
            boolean output, boolean multiport) {
        super(5);
        _portName = portName;
        _portType = portType;
        _input = input;
        _output = output;
        _multiport = multiport;
    }

    public String getPortID(RuleList list) {
        int position = list.indexOf(this);
        return "Rule" + (position + 1);
    }

    public String getPortName() {
        return _portName;
    }

    public String getPortType() {
        return _portType;
    }

    public RuleAttribute[] getRuleAttributes() {
        return _ATTRIBUTES;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _portName;
        case 1:
            return _portType;
        case 2:
            return _input;
        case 3:
            return _output;
        case 4:
            return _multiport;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _portName);
        _encodeStringField(buffer, 1, _portType);
        _encodeBooleanField(buffer, 2, _input);
        _encodeBooleanField(buffer, 3, _output);
        _encodeBooleanField(buffer, 4, _multiport);
        return buffer.toString();
    }

    public boolean isInput() {
        return _input;
    }

    public boolean isInputEnabled() {
        return isEnabled(2);
    }

    public boolean isMultiport() {
        return _multiport;
    }

    public boolean isMultiportEnabled() {
        return isEnabled(4);
    }

    public boolean isOutput() {
        return _output;
    }

    public boolean isOutputEnabled() {
        return isEnabled(3);
    }

    public boolean isPortNameEnabled() {
        return isEnabled(0);
    }

    public boolean isPortTypeEnabled() {
        return isEnabled(1);
    }

    public NamedObjMatchResult match(NamedObj object) {
        // Check the input/output/multiport type
        if (object instanceof IOPort) {
            IOPort port = (IOPort) object;
            if (isInputEnabled() && _input != port.isInput()) {
                return NamedObjMatchResult.NOT_MATCH;
            } else if (isOutputEnabled() && _output != port.isOutput()) {
                return NamedObjMatchResult.NOT_MATCH;
            } else if (isMultiportEnabled()
                    && _multiport != port.isMultiport()) {
                return NamedObjMatchResult.NOT_MATCH;
            }
        } else if (object instanceof Port) {
            if (isInputEnabled() || isOutputEnabled() || isMultiportEnabled()) {
                return NamedObjMatchResult.NOT_MATCH;
            }
        } else {
            return NamedObjMatchResult.UNAPPLICABLE;
        }

        // Check port name
        if (isPortNameEnabled()) {
            Pattern pattern = Pattern.compile(_portName);
            Matcher matcher = pattern.matcher(object.getName());
            if (!matcher.matches()) {
                return NamedObjMatchResult.NOT_MATCH;
            }
        }
        
        // Check port type
        if (isPortTypeEnabled()) {
            if (object instanceof TypedIOPort) {
                Type type = ((TypedIOPort) object).getType();
                
            }
        }
        
        return NamedObjMatchResult.MATCH;
    }

    public void setInputEnabled(boolean enabled) {
        setEnabled(2, enabled);
    }

    public void setMultiportEnabled(boolean enabled) {
        setEnabled(4, enabled);
    }

    public void setOutputEnabled(boolean enabled) {
        setEnabled(3, enabled);
    }

    public void setPortNameEnabled(boolean enabled) {
        setEnabled(0, enabled);
    }

    public void setPortTypeEnabled(boolean enabled) {
        setEnabled(1, enabled);
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _portName = (String) value;
            break;
        case 1:
            _portType = (String) value;
            break;
        case 2:
            _input = ((Boolean) value).booleanValue();
            break;
        case 3:
            _output = ((Boolean) value).booleanValue();
            break;
        case 4:
            _multiport = ((Boolean) value).booleanValue();
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _portName = _decodeStringField(0, fieldIterator);
        _portType = _decodeStringField(1, fieldIterator);
        _input = _decodeBooleanField(2, fieldIterator);
        _output = _decodeBooleanField(3, fieldIterator);
        _multiport = _decodeBooleanField(4, fieldIterator);
    }

    public void validate() throws RuleValidationException {
        if (isPortNameEnabled() && _portName.equals("")) {
            throw new RuleValidationException("Port name must not be empty.");
        }
        try {
            Pattern.compile(_portName);
        } catch (PatternSyntaxException e) {
            throw new RuleValidationException("Regular expression \""
                    + _portName + "\" cannot be compiled.", e);
        }
        if (isPortTypeEnabled() && _portType.equals("")) {
            throw new RuleValidationException("Port type must not be empty.");
        }
        if (!((isInputEnabled() && _input) ^ (isOutputEnabled() && _output))) {
            throw new RuleValidationException("A port should be either an "
                    + "input port or an output port.");
        }
    }

    private static final RuleAttribute[] _ATTRIBUTES = {
        new RuleAttribute(RuleAttribute.STRING, "name", true),
        new RuleAttribute(RuleAttribute.STRING, "type"),
        new RuleAttribute(RuleAttribute.BOOLEAN, "input"),
        new RuleAttribute(RuleAttribute.BOOLEAN, "output"),
        new RuleAttribute(RuleAttribute.BOOLEAN, "multi")
    };

    private boolean _input;

    private boolean _multiport;

    private boolean _output;

    private String _portName;

    private String _portType;
}
