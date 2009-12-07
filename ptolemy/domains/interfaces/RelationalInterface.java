/** Representation of an interface that relates its inputs and outputs.
 * 
 */
package ptolemy.domains.interfaces;

import java.util.HashSet;
import java.util.Set;

/** Representation of an interface that relates its inputs and outputs,
 *  in the style of the EMSOFT 2009 paper "On Relational Interfaces."
 *  
 *  @author Ben Lickly
 *
 */
public class RelationalInterface {

    public RelationalInterface(Set<String> inputs, Set<String> outputs, String contract) {
        // Inputs and outputs must be disjoint.
        for (String inputPort : inputs) {
            assert (!outputs.contains(inputPort));
        }
        _inputPorts = inputs;
        _outputPorts = outputs;
        _contract = contract;
    }

    public String getContract() {
        return _contract;
    }

    public String getYicesInput() {
        StringBuffer yicesInput = new StringBuffer();

        for (String inputPort : _inputPorts) {
            yicesInput.append("(define " + inputPort + "::int)\n");
        }
        for (String outputPort : _outputPorts) {
            yicesInput.append("(define " + outputPort + "::int)\n");
        }

        yicesInput.append("(assert " + _contract +")\n");

        return yicesInput.toString();
    }

    private Set<String> _inputPorts = new HashSet<String>();
    private Set<String> _outputPorts = new HashSet<String>();
    private String _contract;
}
