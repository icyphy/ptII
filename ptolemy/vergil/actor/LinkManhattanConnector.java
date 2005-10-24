package ptolemy.vergil.actor;

import ptolemy.actor.IOPort;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.Link;
import diva.canvas.Site;
import diva.canvas.connector.BasicManhattanRouter;
import diva.canvas.connector.ManhattanConnector;

//////////////////////////////////////////////////////////////////////////
//// LinkManhattanConnector

/**
 An extension to BasicManhattanRouter supporting links to multiports.

 @author Edward A. Lee
 @version $Id$
@since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class LinkManhattanConnector extends ManhattanConnector {
    /** Construct a new connector with the given tail and head
     *  for the specified link. The head and tail sites may be
     *  representative sites for multiport, in which case they are
     *  not necessarily the ones returned by getHeadSite() or
     *  getTailSite().  Those methods will return new sites
     *  as needed to ensure that each each connection is to its
     *  own site.
     *  @param tail The tail site.
     *  @param head The head site.
     *  @param link The link.
     */
    public LinkManhattanConnector(Site tail, Site head, Link link) {
        super(tail, head, _router);

        Object headObject = link.getHead();

        if (headObject instanceof IOPort) {
            _headPort = (IOPort) headObject;
        } else if (headObject instanceof Location) {
            // If this is an external port, the object might be an
            // instance of Location contained by the port.
            NamedObj container = ((Location) headObject).getContainer();

            if (container instanceof IOPort) {
                _headPort = (IOPort) container;
            }
        }

        Object tailObject = link.getTail();

        if (tailObject instanceof IOPort) {
            _tailPort = (IOPort) tailObject;
        } else if (tailObject instanceof Location) {
            // If this is an external port, the object might be an
            // instance of Location contained by the port.
            NamedObj container = ((Location) tailObject).getContainer();

            if (container instanceof IOPort) {
                _tailPort = (IOPort) container;
            }
        }

        _link = link;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Override the base class to return a different site for each
     *  connection to a multiport.
     *  @return The connection site.
     */
    public Site getHeadSite() {
        if ((_headPort == null) || !_headPort.isMultiport()) {
            return super.getHeadSite();
        }

        Site result = super.getHeadSite();

        if (result instanceof PortConnectSite) {
            PortTerminal terminal = ((PortConnectSite) result).getTerminal();
            int orderIndex = terminal.getOrderIndex(this);

            if (orderIndex >= 0) {
                return new PortConnectSite(result.getFigure(), terminal,
                        orderIndex + 1, result.getNormal());
            }
        }

        return result;
    }

    /** Return the associated link.
     *  @return The associated link.
     */
    public Link getLink() {
        return _link;
    }

    /** Override the base class to return a different site for each
     *  connection to a multiport.
     *  @return The connection site.
     */
    public Site getTailSite() {
        if ((_tailPort == null) || !_tailPort.isMultiport()) {
            return super.getTailSite();
        }

        Site result = super.getTailSite();

        if (result instanceof PortConnectSite) {
            PortTerminal terminal = ((PortConnectSite) result).getTerminal();
            int orderIndex = terminal.getOrderIndex(this);

            if (orderIndex >= 0) {
                return new PortConnectSite(result.getFigure(), terminal,
                        orderIndex + 1, result.getNormal());
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** The port at the head, if there is one and it's an IOPort. */
    private IOPort _headPort = null;

    /** The link. */
    private Link _link;

    /** Specialized router. */
    private static BasicManhattanRouter _router = new BasicManhattanRouter();

    /** The port at the tail, if there is one and it's an IOPort. */
    private IOPort _tailPort = null;
}
