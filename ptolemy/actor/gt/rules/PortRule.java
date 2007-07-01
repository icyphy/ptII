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

import ptolemy.actor.gt.Rule;
import ptolemy.actor.gt.RuleAttribute;
import ptolemy.actor.gt.RuleValidationException;

public class PortRule extends Rule {

    public PortRule() {
        this("");
    }

    public PortRule(String values) {
        setValues(values);
    }

    public PortRule(String portName, String portType, boolean input,
            boolean output, boolean multiport) {
        _portName = portName;
        _portType = portType;
        _input = input;
        _output = output;
        _multiport = multiport;
    }

    public Object getAttributeValue(int index) {
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

    public RuleAttribute[] getAttributes() {
        return _ATTRIBUTES;
    }

    public String getPortName() {
        return _portName;
    }

    public String getPortType() {
        return _portType;
    }

    public String getValues() {
        return escapeStringAttribute(_portName) + FIELD_SEPARATOR
                + escapeStringAttribute(_portType) + FIELD_SEPARATOR
                + _input + FIELD_SEPARATOR + _output + FIELD_SEPARATOR
                + _multiport;
    }

    public boolean isInput() {
        return _input;
    }

    public boolean isMultiport() {
        return _multiport;
    }

    public boolean isOutput() {
        return _output;
    }

    public void setAttributeValue(int index, Object value) {
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
        _portName = unescapeStringAttribute(_getFirstField(values));
        _portType = unescapeStringAttribute(_getNextField());
        _input = _getNextField().equals("true") ? true : false;
        _output = _getNextField().equals("true") ? true : false;
        _multiport = _getLastField().equals("true") ? true : false;
    }

    public void validate() throws RuleValidationException {
        if (_portName.equals("")) {
            throw new RuleValidationException("Port name must not be empty.");
        }
        if (_portType.equals("")) {
            throw new RuleValidationException("Port type must not be empty.");
        }
        if (!(_input ^ _output)) {
            throw new RuleValidationException("A port should be either an "
                    + "input port or an output port.");
        }
    }

    private static final RuleAttribute[] _ATTRIBUTES = {
        new RuleAttribute(RuleAttribute.STRING, "Port Name"),
        new RuleAttribute(RuleAttribute.STRING, "Port Type"),
        new RuleAttribute(RuleAttribute.BOOLEAN, "Input"),
        new RuleAttribute(RuleAttribute.BOOLEAN, "Output"),
        new RuleAttribute(RuleAttribute.BOOLEAN, "Multiport")
    };

    private boolean _input;

    private boolean _multiport;

    private boolean _output;

    private String _portName;

    private String _portType;
}
