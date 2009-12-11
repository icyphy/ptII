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
        final Set<String> inputs = new HashSet<String>();
        Iterator<IOPort> ports = inputPortList.iterator();
        while (ports.hasNext()) {
            inputs.add(ports.next().getName());
        }

        final Set<String> outputs = new HashSet<String>();
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
     *  @param connections The feedback connections.
     */
    public void addFeedback(final Set<Connection> connections) {
        // FIXME: Deal with multiple connections
        Connection connection = connections.iterator().next();
        if (_mooreInputs().contains(connection._inputPort)) {
            _inputPorts.remove(connection._inputPort);
            _outputPorts.add(connection._inputPort);
            _contract = "(and " + _contract + " (= " + connection._inputPort
                    + " " + connection._outputPort + "))";
        } else {
            assert false; //FIXME: Throw exception
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
            final RelationalInterface rhs, final Set<Connection> connections) {
        final Set<String> newInputs = new HashSet<String>();
        newInputs.addAll(_inputPorts);
        newInputs.addAll(rhs._inputPorts);
        for (final Connection c : connections) {
            newInputs.remove(c._inputPort);
        }
        final Set<String> newOutputs = new HashSet<String>();
        newOutputs.addAll(_outputPorts);
        newOutputs.addAll(rhs._outputPorts);
        for (final Connection c : connections) {
            newOutputs.add(c._inputPort);
        }

        // Build up the contract for cascade composition.
        // This is a translation of Definition 10 (composition by connection)
        // in the EMSOFT 2009 paper "On Relational Interfaces"
        final String connectionContracts = Connection.getContract(connections);
        final Set<String> newConstraints = new HashSet<String>();
        newConstraints.add(_contract);
        newConstraints.add(rhs._contract);
        newConstraints.add(connectionContracts);
        Set<String> quantifiedOutputs = new HashSet<String>();
        for (final Connection c : connections) {
            quantifiedOutputs.add(c._inputPort + "::int");
            quantifiedOutputs.add(c._outputPort + "::int");
        }
        for (final String firstInterfaceOutputVariable : _outputPorts) {
            quantifiedOutputs.add(firstInterfaceOutputVariable + "::int");
        }
        final String y = LispExpression.node("", quantifiedOutputs);
        final String phi = "(=> (and " + _contract + " " + connectionContracts
                + ") " + rhs.inContract() + ")";
        newConstraints.add(" (forall " + y.toString() + " " + phi + ")");
        return new RelationalInterface(newInputs, newOutputs,
                LispExpression.conjunction(newConstraints));
    }

    /** Return a string representation of the interface contract.
     * 
     *  @return The contract.
     */
    public String getContract() {
        return _contract;
    }
    
    /** Return a set of variables in the interface.
     *  @return A set of all variables used in the interface.
     */
    public Set<String> getVariables() {
        final Set<String> variables = new HashSet<String>();
        variables.addAll(_inputPorts);
        variables.addAll(_outputPorts);
        return variables;
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

        yicesInput.append("(assert " + _contract + ")\n");

        return yicesInput.toString();
    }

    /** Return the contract of the input assumption of this interface.
     * 
     *  @return The input assumption.
     */
    private String inContract() {
        final StringBuffer result = new StringBuffer("(exists (");
        for (final String output : _outputPorts) {
            result.append(output + "::int ");
        }
        result.append(") " + _contract + ")");
        return result.toString();
    }

    /** Return an interface that results from the parallel composition of
     *  this interface and the given interface.
     * 
     *  @param rhs The interface to compose with.
     *  @return The comosition's interface.
     */
    public RelationalInterface parallelComposeWith(final RelationalInterface rhs) {
        final Set<String> newInputs = new HashSet<String>();
        newInputs.addAll(_inputPorts);
        newInputs.addAll(rhs._inputPorts);
        final Set<String> newOutputs = new HashSet<String>();
        newOutputs.addAll(_outputPorts);
        newOutputs.addAll(rhs._outputPorts);
        final String newContract = "(and " + _contract + " " + rhs._contract
                + ")";
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
    private void _init(final Set<String> inputs, final Set<String> outputs,
            final String contract) {
        // Inputs and outputs must be disjoint.
        for (final String inputPort : inputs) {
            assert !outputs.contains(inputPort);
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
        final Set<Connection> connections = new HashSet<Connection>();
        IOPort outputPort = null;
        for (final Object o : r.linkedPortList()) {
            if (!(o instanceof IOPort)) {
                continue;
            }
            final IOPort p = (IOPort) o;
            if (p.isOutput()) {
                assert outputPort == null; // FIXME: Change to exception
                outputPort = p;
            }
        }
        assert outputPort != null; //FIXME: Change to exception
        for (final Object o : r.linkedPortList(outputPort)) {
            if (!(o instanceof IOPort)) {
                continue;
            }
            final IOPort p = (IOPort) o;
            connections.add(new Connection(outputPort.getName(), p.getName()));
        }
        return connections;
    }

    /** Return the contract specifying the equality caused by a set of
     *  connections.
     *  
     *  @param connections The set of connections.
     *  @return The contract.
     */
    public static String getContract(final Set<Connection> connections) {
        final Set<String> contracts = new HashSet<String>();
        for (final Connection c : connections) {
            contracts.add("(= " + c._inputPort + " " + c._outputPort + ")");
        }
        return LispExpression.conjunction(contracts);
    }
    
    public String toString() {
        return "(" + _outputPort + ", " + _inputPort + ")";
    }

    /** The start of the connection.
     */
    public String _outputPort;

    /** The end of the connection.
     */
    public String _inputPort;
}
