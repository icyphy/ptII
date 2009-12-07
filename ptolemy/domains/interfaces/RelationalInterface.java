/** Representation of an interface that relates its inputs and outputs.
 * 
 */
package ptolemy.domains.interfaces;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.kernel.Relation;

/** Representation of an interface that relates its inputs and outputs,
 *  in the style of the EMSOFT 2009 paper "On Relational Interfaces."
 *  
 *  @author Ben Lickly
 *
 */
public class RelationalInterface {
    
    public RelationalInterface(List<IOPort> inputPortList,
            List<IOPort> outputPortList, String contract) {
        Set<String> inputs = new HashSet<String>();
        Iterator<IOPort> ports = inputPortList.iterator();
        while (ports.hasNext()) {
            inputs.add(ports.next().getName());
        }
        
        Set<String> outputs = new HashSet<String>();
        ports = outputPortList.iterator();
        while (ports.hasNext()) {
            outputs.add(ports.next().getName());
        }
        
        _init(inputs, outputs, contract);
    }

    public RelationalInterface(Set<String> inputs, Set<String> outputs, String contract) {
        _init(inputs, outputs, contract);
    }

    public RelationalInterface cascadeComposeWith(RelationalInterface rhs,
            Set<Connection> connections) {
        Set<String> newInputs = new HashSet<String>();
        newInputs.addAll(_inputPorts);
        newInputs.addAll(rhs._inputPorts);
        for (Connection c : connections) {
            newInputs.remove(c._inputPort);
        }
        Set<String> newOutputs = new HashSet<String>();
        newOutputs.addAll(_outputPorts);
        newOutputs.addAll(rhs._outputPorts);
        for (Connection c : connections) {
            newOutputs.add(c._inputPort);
        }
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
    
    /** Construct a RelationalInterface with the given inputs port names,
     *  output port names, and contract.
     *  
     *  @param inputs A set of the names of input ports.
     *  @param outputs A set of the names of output ports.
     *  @param contract A Yices-compatible string of the contract.
     */
    private void _init(Set<String> inputs, Set<String> outputs, String contract) {
        // Inputs and outputs must be disjoint.
        for (String inputPort : inputs) {
            assert (!outputs.contains(inputPort));
        }
        _inputPorts = inputs;
        _outputPorts = outputs;
        _contract = contract;
    }

    private Set<String> _inputPorts = new HashSet<String>();
    private Set<String> _outputPorts = new HashSet<String>();
    private String _contract;
}

/** A class that represents a connection between two ports.
 * 
 *  @author Ben Lickly
 */
class Connection {
    /** Construct a connection from the given output port
     *  to the given input port.
     */
    Connection(String outputPort, String inputPort) {
        _outputPort = outputPort;
        _inputPort = inputPort;
    }
    
    public static Set<Connection> connectionsFromRelations(Relation r) {
        Set<Connection> connections = new HashSet<Connection>();
        IOPort outputPort = null;
        for (Object o : r.linkedPortList()) {
            if (!(o instanceof IOPort)) continue;
            IOPort p = (IOPort) o;
            if (p.isOutput()) {
                assert (outputPort == null); // FIXME: Change to exception
                outputPort = p;
            }
        }
        assert (outputPort != null); //FIXME: Change to exception
        for (Object o : r.linkedPortList(outputPort)) {
            if (!(o instanceof IOPort)) continue;
            IOPort p = (IOPort) o;
            connections.add(new Connection(outputPort.getName(), p.getName()));
        }   
        return connections;
    }
 
    /** The start of the connection.
     */
    public String _outputPort;
    
    /** The end of the connection.
     */
    public String _inputPort;
}