/** Representation of a connection between two ports.
 * 
 */
package ptolemy.apps.interfaces;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.kernel.Relation;

/** Representation of a connection between two ports.
 * 
 *  @author Ben Lickly
 */
public class Connection {
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
    
    /** Return the string representation of the connection.
     * i.e "(port1, port2)" 
     * 
     * @return The string representation.
     */
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
