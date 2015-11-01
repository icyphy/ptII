/* Manhattan Geometry Link

 Copyright (c) 2006-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 */

package ptolemy.vergil.actor;

import java.util.ArrayList;

import ptolemy.actor.IOPort;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.Link;
import diva.canvas.Site;
import diva.canvas.connector.BasicManhattanRouter;
import diva.canvas.connector.ManhattanConnector;

///////////////////////////////////////////////////////////////////
//// LinkManhattanConnector

/**
 An extension to BasicManhattanRouter supporting links to multiports.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
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
    ////                         public methods                    ////

    /** Override the base class to return a different site for each
     *  connection to a multiport.
     *  @return The connection site.
     */
    @Override
    public Site getHeadSite() {
        Site result = super.getHeadSite();
        if (_headPort == null || !_headPort.isMultiport()) {
            return result;
        }
        if (result instanceof PortConnectSite) {
            PortTerminal terminal = ((PortConnectSite) result).getTerminal();
            int orderIndex = terminal.getOrderIndex(this);

            if (orderIndex >= 0) {
                // This should not create a new site if it has already
                // created one!  Record the ones created and return them.
                if (_headSites == null) {
                    _headSites = new ArrayList();
                }
                if (_headSites.size() > orderIndex) {
                    if (_headSites.get(orderIndex) == null) {
                        result = new PortConnectSite(result.getFigure(),
                                terminal, orderIndex + 1, result.getNormal());
                    } else {
                        result = (Site) _headSites.get(orderIndex);
                    }
                    _headSites.set(orderIndex, result);
                } else {
                    result = new PortConnectSite(result.getFigure(), terminal,
                            orderIndex + 1, result.getNormal());
                    while (_headSites.size() < orderIndex) {
                        _headSites.add(null);
                    }
                    _headSites.add(result);
                }
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
    @Override
    public Site getTailSite() {
        Site result = super.getTailSite();
        if (_tailPort == null || !_tailPort.isMultiport()) {
            return result;
        }
        if (result instanceof PortConnectSite) {
            PortTerminal terminal = ((PortConnectSite) result).getTerminal();
            int orderIndex = terminal.getOrderIndex(this);

            if (orderIndex >= 0) {
                // This should not create a new site if it has already
                // created one!  Record the ones created and return them.
                if (_tailSites == null) {
                    _tailSites = new ArrayList();
                }
                if (_tailSites.size() > orderIndex) {
                    if (_tailSites.get(orderIndex) == null) {
                        result = new PortConnectSite(result.getFigure(),
                                terminal, orderIndex + 1, result.getNormal());
                    } else {
                        result = (Site) _tailSites.get(orderIndex);
                    }
                    _tailSites.set(orderIndex, result);
                } else {
                    result = new PortConnectSite(result.getFigure(), terminal,
                            orderIndex + 1, result.getNormal());
                    while (_tailSites.size() < orderIndex) {
                        _tailSites.add(null);
                    }
                    _tailSites.add(result);
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** The port at the head, if there is one and it's an IOPort. */
    private IOPort _headPort = null;

    /** Head sites, in case this is a multiport. */
    private ArrayList _headSites = null;

    /** The link. */
    private Link _link;

    /** Specialized router. */
    private static BasicManhattanRouter _router = new BasicManhattanRouter();

    /** The port at the tail, if there is one and it's an IOPort. */
    private IOPort _tailPort = null;

    /** Tail sites, in case this is a multiport. */
    private ArrayList _tailSites = null;
}
