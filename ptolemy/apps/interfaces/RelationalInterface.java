/** Representation of an interface that relates its inputs and outputs.
 *
 */
package ptolemy.apps.interfaces;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;

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
     *  @throws InterfaceCompositionException If there feedback connection
     *   is invalid.  This can only be because the input port is non-Moore.
     */
    public void addFeedback(final Set<Connection> connections)
            throws InterfaceCompositionException {
        Set<String> newConstraints = new HashSet<String>();
        newConstraints.add(_contract);
        for (Connection connection : connections) {
            if (_mooreInputs().contains(connection._inputPort)) {
                _inputPorts.remove(connection._inputPort);
                _outputPorts.add(connection._inputPort);
                newConstraints.add("(= " + connection._inputPort
                        + " " + connection._outputPort + ")");
            } else {
                throw new InterfaceCompositionException(
                        "Cannot add feedback to a non-Moore input port");
            }
        }
        _contract = LispExpression.conjunction(newConstraints);
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
                + ") " + rhs._inContract() + ")";
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

    /** Return the contract of the input assumption of this interface.
     *
     *  @return The input assumption.
     */
    private String _inContract() {
        final StringBuffer result = new StringBuffer("(exists (");
        for (final String output : _outputPorts) {
            result.append(output + "::int ");
        }
        result.append(") " + _contract + ")");
        return result.toString();
    }

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

/** Exception thrown when there an interface is unable to add composition
 *  or feedback.
 *  @author Ben Lickly
 */
class InterfaceCompositionException extends IllegalActionException {
    /** Construct an exception with a detail message.
     *  @param detail The message.
     */
    public InterfaceCompositionException(String detail) {
        super(detail);
    }
}
