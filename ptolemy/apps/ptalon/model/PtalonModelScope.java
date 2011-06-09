package ptolemy.apps.ptalon.model;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

public class PtalonModelScope implements ParserScope {
    public PtalonModelScope(Hashtable<String, Parameter> parameters) {
        _parameters = parameters;
    }

    private Hashtable<String, Parameter> _parameters;

    public String uniqueName(String prefix) {
        if (!_parameters.containsKey(prefix) && !_variables.containsKey(prefix)) {
            return prefix;
        }
        int numberOfOccurances = 0;
        String result = prefix + (numberOfOccurances++);
        while (_parameters.containsKey(result)
                || _variables.containsKey(result)) {
            result = prefix + (numberOfOccurances++);
        }
        return result;
    }

    public void addVariable(String name, Token value) {
        _variables.put(name, value);
    }

    public void removeName(String name) {
        _variables.remove(name);
    }

    private Hashtable<String, Token> _variables = new Hashtable<String, Token>();

    public Token get(String name) throws IllegalActionException {
        if (_parameters.containsKey(name)) {
            return _parameters.get(name).getToken();
        } else if (_variables.containsKey(name)) {
            return _variables.get(name);
        }
        throw new IllegalActionException("Key not found");
    }

    public Type getType(String name) throws IllegalActionException {
        if (_parameters.containsKey(name)) {
            return _parameters.get(name).getType();
        } else if (_variables.containsKey(name)) {
            return _variables.get(name).getType();
        }
        throw new IllegalActionException("Key not found");
    }

    public InequalityTerm getTypeTerm(String name)
            throws IllegalActionException {
        if (_parameters.containsKey(name)) {
            return _parameters.get(name).getTypeTerm();
        } else if (_variables.containsKey(name)) {
            return new TypeConstant(_variables.get(name).getType());
        }
        return null;
    }

    public Set identifierSet() throws IllegalActionException {
        HashSet<String> keys = new HashSet<String>();
        for (String key : _parameters.keySet()) {
            keys.add(key);
        }
        for (String key : _variables.keySet()) {
            keys.add(key);
        }
        return keys;
    }

}
