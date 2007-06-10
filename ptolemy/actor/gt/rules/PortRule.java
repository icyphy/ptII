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

    public String getValues() {
        return _portName + FIELD_SEPARATOR + _portType + FIELD_SEPARATOR
                + _input + FIELD_SEPARATOR + _output + FIELD_SEPARATOR
                + _multiport;
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
        _portName = _getFirstField(values);
        _portType = _getNextField();
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
