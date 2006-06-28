package ptolemy.actor.ptalon;

import java.util.ArrayList;

/**
 * A class to store a set of parameter names and assoicated
 * values.  It can also permit updating these values.
 * @author acataldo
 *
 */
public class ParameterValues {

    /**
     * Create a new ParameterValues list.
     *
     */
    public ParameterValues() {
        _list = new ArrayList<String[]>();
    }
        
    /**
     * Get the value associated with the parameter.
     * @param parameterName The parameter name.
     * @return The value or null if no value exists
     * for the given parameter.
     */
    public String getValue(String parameterName) {
        String value = null;
        for (int i = 0; i < _list.size(); i++) {
            if (_list.get(i)[0].equals(parameterName)) {
                value = _list.get(i)[1];
                break;
            }
        }
        return value;
    }
    
    /**
     * Associate the specified value with the given parameter
     * name.
     * @param parameterName The parameter name.
     * @param value The value.
     */
    public void setValue(String parameterName, String value) {
        for (int i = 0; i < _list.size(); i++) {
            if (_list.get(i)[0].equals(parameterName)) {
                _list.get(i)[1] = value;
                return;
            }
        }
        _list.add(new String[] {parameterName, value});
    }
    
    private ArrayList<String[]> _list;

}
