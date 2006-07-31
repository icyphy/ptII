package ptolemy.actor.ptalon;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;


/**
 * This helper class is used by the Ptalon interpreter to
 * store data used by the Ptalon actor.
 * @author acataldo
 * @see PtalonActor
 *
 */
public class InterpreterData {

    /**
     * Create an interpreter data object
     *
     */
    public InterpreterData() {
        _parameters = new ArrayList<String>();
        _ports = new ArrayList<String>();
        _relations = new ArrayList<String>();
    }
    
    /**
     * Add a parameter name to the list of parameters.
     * @param name The name to add.
     * @throws PtalonScopeException If this name has already been 
     * added as a parameter, port, or relation.
     */
    public void addParameter (String name) throws PtalonScopeException {
        if (_parameters.contains(name))
        
        _parameters.add(name);
    }

    /**
     * Returns true if the given string corresponds to a parameter.
     * @param name The name to test.
     * @return True if the name is a parameter.
     */
    public boolean isParameter(String name) {
        for (String p :_parameters) {
            if (p.equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if the given string corresponds to a port.
     * @param name The name to test.
     * @return True if the name is a port.
     */
    public boolean isPort(String name) {
        for (String p : _ports) {
            if (p.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given string corresponds to a relation.
     * @param name The name to test.
     * @return True if the name is a relation.
     */
    public boolean isRelation(String name) {
        for (String r : _relations) {
            if (r.equals(name)) {
                return true;
            }
        }
        return false;
    }    
    
    /**
     * Respective lists of all paramters, inports, outports,
     * and relations generated in the actor being generated.
     */
    private ArrayList<String> _parameters;
    private ArrayList<String> _ports;
    private ArrayList<String> _relations;
    

}
