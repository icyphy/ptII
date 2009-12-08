/** Representation of an interface that relates its inputs and outputs.
 * 
 */
package ptolemy.apps.interfaces;

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
    
    /** Construct a new interface given lists of ports and a contract.
     * 
     *  This is a convenience method that simply extracts the names of
     *  ports and uses then uses the other constructor strategy.
     *  
     *  @param inputPortList The input ports.
     *  @param outputPortList The output ports.
     *  @param contract The contract.
     */
    public RelationalInterface(final List<IOPort> inputPortList,
            final List<IOPort> outputPortList, final String contract) {
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

    /** Construct a new interface given a set of input variables, a set of
     *  output variables, and a contract.
     *  
     *  @param inputs The input variables.
     *  @param outputs The output variables.
     *  @param contract The contract.
     */
    public RelationalInterface(final Set<String> inputs,
            final Set<String> outputs, final String contract) {
        _init(inputs, outputs, contract);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Add a the given feedback connection to this interface.
     * 
     *  FIXME: This the only type of composition that modifies
     *  the original interface. We should change this.
     * 
     *  @param connection The feedback connection.
     */
    public void addFeedback(final Connection connection) {
        if (_mooreInputs().contains(connection._inputPort)) {
            _inputPorts.remove(connection._inputPort);
            _outputPorts.remove(connection._outputPort);
            _contract = "(and " + _contract + " (== " + connection._inputPort
                    + " " + connection._outputPort + "))";
        } else {
            assert (false); //FIXME: Throw exception
        }
    }

    /** Return an interface that results from the cascade composition of
     *  this interface and the given interface.
     *  
     *  Note that this is not commutative.  The outputs of this
     *  interface must be connected to the inputs of the given interface.
     * 
     *  @param rhs The interface to compose with.
     *  @param connections The connections from this to rhs.
     *  @return The comosition's interface.
     */
    public RelationalInterface cascadeComposeWith(
            final RelationalInterface rhs,
            final Set<Connection> connections) {
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
        
        Set<String> constraints = new HashSet<String>();
        constraints.add(_contract);
        constraints.add(rhs._contract);
        for (Connection c : connections) {
            constraints.add("(== " + c._inputPort + " " + c._outputPort + ")");
        }
        // FIXME: Fix up contract
        // String Phi = "(implies (and " + _contract + " " + _connectionConstraints + ") " + rhs.inContract(); 
        // newContract = "(and " + newContract + " " + _connected +")";
        // Y = "("
        // for (port : connections U outputs) {
        //  Y += port.name + "::int "
        // }
        // Y += ")"
        // newContract = "(and " + newContract + " (forall " + _Y + " " + Phi + ")"; 
        return new RelationalInterface(newInputs, newOutputs, null);
    }

    /** Return a string representation of the interface contract.
     * 
     *  @return The contract.
     */
    public String getContract() {
        return _contract;
    }

    /** Return a string that Yices can check for satisfiability.
     * 
     *  @return A string in the yices input expression language.
     */
    public String getYicesInput() {
        final StringBuffer yicesInput = new StringBuffer();

        for (final String inputPort : _inputPorts) {
            yicesInput.append("(define " + inputPort + "::int)\n");
        }
        for (final String outputPort : _outputPorts) {
            yicesInput.append("(define " + outputPort + "::int)\n");
        }

        yicesInput.append("(assert " + _contract +")\n");

        return yicesInput.toString();
    }
    
    /** Return an interface that results from the parallel composition of
     *  this interface and the given interface.
     * 
     *  @param rhs The interface to compose with.
     *  @return The comosition's interface.
     */
    public RelationalInterface parallelComposeWith(
            final RelationalInterface rhs) {
        Set<String> newInputs = new HashSet<String>();
        newInputs.addAll(_inputPorts);
        newInputs.addAll(rhs._inputPorts);
        Set<String> newOutputs = new HashSet<String>();
        newOutputs.addAll(_outputPorts);
        newOutputs.addAll(rhs._outputPorts);
        String newContract = "(and " + _contract + " " + rhs._contract + ")";
        return new RelationalInterface(newInputs, newOutputs, newContract);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Construct a RelationalInterface with the given inputs port names,
     *  output port names, and contract.
     *  
     *  @param inputs A set of the names of input variables.
     *  @param outputs A set of the names of output variables.
     *  @param contract A Yices-compatible string of the contract.
     */
    private void _init(final Set<String> inputs,
            final Set<String> outputs, final String contract) {
        // Inputs and outputs must be disjoint.
        for (String inputPort : inputs) {
            assert (!outputs.contains(inputPort));
        }
        _inputPorts = inputs;
        _outputPorts = outputs;
        _contract = contract;
    }
    

    /** Return the subset of the input variables that are Moore.
     *  These are just the input variables that do not appear in the contract.
     *  
     *  @return The Moore inputs.
     */
    private Set<String> _mooreInputs() {
        final Set<String> mooreInputs = new HashSet<String>();
        for (final String port : _inputPorts) {
            // want to check for "\bport\b" regex, but this is simpler
            if (!_contract.contains(" " + port + " ")
                    && !_contract.contains(" " + port + ")")) {
                mooreInputs.add(port);
            }
        }
        return mooreInputs;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /** The set of input variables.
     */
    private Set<String> _inputPorts = new HashSet<String>();

    /** The set of output variables.
     */
    private Set<String> _outputPorts = new HashSet<String>();
    
    /** The contract of the interface.
     */
    private String _contract;
}

/** A class that represents a connection between two ports.
 * 
 *  @author Ben Lickly
 */
class Connection {
    /** Construct a connection from the given output port
     *  to the given input port.
     *  @param outputPort The name of the output port. 
     *  @param inputPort The name of the input port.
     */
    Connection(final String outputPort, final String inputPort) {
        _outputPort = outputPort;
        _inputPort = inputPort;
    }
    
    /** Return the set of connections represented by a relation.
     * 
     *  This is a set because relations can connect multiple ports,
     *  but a connection is between two ports only.
     *  
     *  @param r The relation to convert.
     *  @return The set of connections that r represents.
     */
    public static Set<Connection> connectionsFromRelations(final Relation r) {
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