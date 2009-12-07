/** Representation of an interface that relates its inputs and outputs.
 * 
 */
package ptolemy.domains.interfaces;

import java.util.HashSet;
import java.util.Set;

import ptolemy.kernel.Relation;

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

    public RelationalInterface cascadeComposeWith(RelationalInterface rhs,
            Set<Relation> connections) {
        Set<String> newInputs = new HashSet<String>();
        newInputs.addAll(_inputPorts);
        newInputs.addAll(rhs._inputPorts);
        // FIXME: Remove connected ports
        //newInputs.remove(...);
        Set<String> newOutputs = new HashSet<String>();
        newOutputs.addAll(_outputPorts);
        newOutputs.addAll(rhs._outputPorts);
        // FIXME: Add connected ports
        //newOutputs.add(...);
        String newContract = "(and " + _contract + " " + rhs._contract + ")";
        // FIXME: Fix up contract
        // newContract = "(and " + newContract + " " + _connected +")";
        // Y = "("
        // for (port : connections U outputs) {
        //  Y += port.name + "::int "
        // }
        // Y += ")"
        // newContract = "(and " + newContract + " (forall " + _Y + " " + _PHI + ")"; 
        return new RelationalInterface(newInputs, newOutputs, newContract);
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
    
    public RelationalInterface parallelComposeWith(RelationalInterface rhs) {
        Set<String> newInputs = new HashSet<String>();
        newInputs.addAll(_inputPorts);
        newInputs.addAll(rhs._inputPorts);
        Set<String> newOutputs = new HashSet<String>();
        newOutputs.addAll(_outputPorts);
        newOutputs.addAll(rhs._outputPorts);
        String newContract = "(and " + _contract + " " + rhs._contract + ")";
        return new RelationalInterface(newInputs, newOutputs, newContract);
    }

    private Set<String> _inputPorts = new HashSet<String>();
    private Set<String> _outputPorts = new HashSet<String>();
    private String _contract;
}
