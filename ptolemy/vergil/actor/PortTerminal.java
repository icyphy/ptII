/* A terminal for ports that supports multiports. */
package ptolemy.vergil.actor;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.PortSite;
import diva.canvas.Figure;
import diva.canvas.connector.TerminalFigure;

//////////////////////////////////////////////////////////////////////////
//// PortTerminal

/**
 A terminal figure for ports that supports multiports.
 In particular, this figure provides a method to determine
 the "order index" of a link to the port. When multiple relations
 are linked to a port, the order in which they are linked matters.
 The provided method returns the position within that order.
 <p>
 When this is constructed, a figure is specified for the port,
 and properties of this figure, such as its bounds, whether it
 intersects other objects, etc., are determined by that figure.
 The extra decorations added to support multiple connections
 are not treated as part of the figure.

 @author Edward A. Lee
 @version $Id$
 @see PortSite
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class PortTerminal extends TerminalFigure {
    /** Construct a port terminal with the specified figure as
     *  the port figure.
     *  @param port The port.
     *  @param figure The associated figure.
     *  @param normal The normal direction.
     *  @param inside True if this is external port and the terminal represents
     *   inside connections.
     */
    public PortTerminal(IOPort port, Figure figure, double normal,
            boolean inside) {
        super(figure);

        _connectSite = new PortConnectSite(figure, this, 0, normal);
        _port = port;
        _inside = inside;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of links to relations that this port has.
     *  @return The size of the inside or outside relations list of
     *   the port.
     */
    public int getNumberOfLinks() {
        List relations;

        if (_inside) {
            relations = _port.insideRelationList();
        } else {
            relations = _port.linkedRelationList();
        }

        return relations.size();
    }

    /** Return the order index of the connection represented
     *  by the specified connector. That is, return 0 if it is
     *  the first connection, 1 if it is the second, etc.
     *  If the connector is not known, then return -1.
     *  @return The order index of the connection.
     */
    public int getOrderIndex(LinkManhattanConnector connector) {
        Link link = connector.getLink();
        ComponentRelation relation = link.getRelation();
        List relations;

        if (_inside) {
            relations = _port.insideRelationList();
        } else {
            relations = _port.linkedRelationList();
        }

        return relations.indexOf(relation);
    }

    /** Return the port specified in the constructor.
     *  @return The port for which this is a terminal.
     */
    public IOPort getPort() {
        return _port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** True if the terminal is an external port, and connections
     *  represent inside connections.
     */
    private boolean _inside;

    /** The port that owns this terminal. */
    private IOPort _port;
}
